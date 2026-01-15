package com.zebra.zebraprintereuredsetup;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private FrameLayout navHostFragment;

    private static final String KEY_CURRENT_NAV_ITEM = "current_nav_item";

    // Current fragment tracking
    private Fragment currentFragment;
    private int currentNavItemId = R.id.nav_home;

    // Barcode scanner for Settings fragment
    private boolean isScanningFromSettings = false;
    private int settingsTargetField = -1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupViews();
        checkAndRequestPermissions();

        // Restore or set default navigation
        if (savedInstanceState != null) {
            currentNavItemId = savedInstanceState.getInt(KEY_CURRENT_NAV_ITEM, R.id.nav_home);
        }

        // Always load the appropriate fragment
        loadFragmentForNavItem(currentNavItemId);
        navigationView.setCheckedItem(currentNavItemId);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_NAV_ITEM, currentNavItemId);
    }

    private void setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        navHostFragment = findViewById(R.id.nav_host_fragment);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Setup drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation listener
        navigationView.setNavigationItemSelectedListener(this);

        // Hide/show Advanced menu based on advanced mode setting
        updateAdvancedMenuVisibility();

        // Setup barcode result listener for Settings fragment
        setupSettingsBarcodeResultListener();
    }

    /**
     * Update the visibility of the Advanced menu item based on the advanced mode setting.
     */
    private void updateAdvancedMenuVisibility() {
        MenuItem advancedItem = navigationView.getMenu().findItem(R.id.nav_advanced);
        if (advancedItem != null) {
            advancedItem.setVisible(SettingsHelper.getAdvancedModeEnabled(this));
        }
    }

    /**
     * Called from AboutFragment when advanced mode is enabled.
     */
    public void refreshNavigationDrawer() {
        updateAdvancedMenuVisibility();
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    private void loadFragmentForNavItem(int navItemId) {
        if (navItemId == R.id.nav_home) {
            loadFragment(new HomeFragment());
        } else if (navItemId == R.id.nav_custom_script) {
            loadFragment(new CustomScriptFragment());
        } else if (navItemId == R.id.nav_script_documentation) {
            loadFragment(new ScriptDocumentationFragment());
        } else if (navItemId == R.id.nav_advanced) {
            loadFragment(new AdvancedFragment());
        } else if (navItemId == R.id.nav_settings) {
            loadFragment(new SettingsFragment());
        } else if (navItemId == R.id.nav_about) {
            loadFragment(new AboutFragment());
        } else {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId != currentNavItemId) {
            currentNavItemId = itemId;
            loadFragmentForNavItem(itemId);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentFragment instanceof HomeFragment) {
            if (((HomeFragment) currentFragment).handleBackPress()) {
                return;
            }
            super.onBackPressed();
        } else if (currentFragment instanceof CustomScriptFragment) {
            if (((CustomScriptFragment) currentFragment).handleBackPress()) {
                return;
            }
            super.onBackPressed();
        } else if (currentFragment instanceof AdvancedFragment) {
            if (((AdvancedFragment) currentFragment).handleBackPress()) {
                return;
            }
            super.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // Called from SettingsFragment to open barcode scanner
    public void openBarcodeScannerFromSettings(int targetField) {
        isScanningFromSettings = true;
        settingsTargetField = targetField;

        BarcodeScannerFragment scannerFragment = BarcodeScannerFragment.newInstance(targetField);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, scannerFragment)
                .addToBackStack("settings_scanner")
                .commit();
    }

    private void setupSettingsBarcodeResultListener() {
        getSupportFragmentManager().setFragmentResultListener(
                BarcodeScannerFragment.RESULT_KEY,
                this,
                (requestKey, result) -> {
                    if (isScanningFromSettings) {
                        String barcode = result.getString(BarcodeScannerFragment.RESULT_BARCODE);
                        int targetField = result.getInt(BarcodeScannerFragment.RESULT_TARGET_FIELD);

                        // Pass result to Settings fragment
                        Bundle settingsResult = new Bundle();
                        settingsResult.putString(BarcodeScannerFragment.RESULT_BARCODE, barcode);
                        settingsResult.putInt(BarcodeScannerFragment.RESULT_TARGET_FIELD, targetField);
                        getSupportFragmentManager().setFragmentResult("settings_barcode_result", settingsResult);

                        isScanningFromSettings = false;
                        settingsTargetField = -1;
                    }
                });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0 && isScanningFromSettings) {
                // User cancelled, reload settings fragment
                loadFragment(new SettingsFragment());
                navigationView.setCheckedItem(R.id.nav_settings);
                currentNavItemId = R.id.nav_settings;
                isScanningFromSettings = false;
            }
        });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        // Location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // Bluetooth permissions (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            initializeApplication();
        } else {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    private void onPermissionsResult(Map<String, Boolean> results) {
        boolean allGranted = true;
        for (Boolean granted : results.values()) {
            if (!granted) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            initializeApplication();
        } else {
            showPermissionExplanationDialog();
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permissions_required_title)
                .setMessage(R.string.permissions_required_message)
                .setPositiveButton(R.string.button_grant_permissions, (dialog, which) -> checkAndRequestPermissions())
                .setCancelable(false)
                .show();
    }

    private void initializeApplication() {
        // Application is ready - all permissions granted
    }
}
