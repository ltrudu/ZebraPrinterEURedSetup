package com.zebra.zebraprintereuredsetup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, HomeFragment.NavigationCallback {

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ImageButton toolbarButtonMenu;
    private ImageButton toolbarButtonBack;
    private TextView toolbarTitle;
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

        setLightStatusBar(this);

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
        toolbarButtonMenu = findViewById(R.id.toolbarButtonMenu);
        toolbarButtonBack = findViewById(R.id.toolbarButtonBack);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        navHostFragment = findViewById(R.id.nav_host_fragment);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup menu button click listener to open drawer
        toolbarButtonMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Setup back button click listener
        toolbarButtonBack.setOnClickListener(v -> onBackPressed());

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

    /**
     * Sets the toolbar title.
     * @param title The title to display
     */
    public void setToolbarTitle(String title) {
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }

    /**
     * Shows or hides the back arrow in the toolbar.
     * The drawer/hamburger icon always remains visible.
     * The back arrow appears next to it when navigating to child fragments.
     * @param show true to show back arrow, false to hide it
     */
    public void showBackArrow(boolean show) {
        // Show/hide the back button next to the drawer icon
        toolbarButtonBack.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Opens the EURed Settings Fragment directly.
     * Used to navigate to script settings from other fragments.
     */
    public void openEURedSettingsFragment() {
        EURedSettingsFragment fragment = new EURedSettingsFragment();
        currentFragment = fragment;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack("eured_settings");
        transaction.commit();
    }

    /**
     * Configures the toolbar based on the fragment type.
     * If fragment implements ToolbarConfigurable, use its settings.
     * Otherwise, use default app name and hamburger menu.
     */
    private void configureToolbarForFragment(Fragment fragment) {
        if (fragment instanceof ToolbarConfigurable) {
            ToolbarConfigurable config = (ToolbarConfigurable) fragment;
            // Use custom title if available, otherwise use resource ID
            String customTitle = config.getToolbarTitle();
            if (customTitle != null && !customTitle.isEmpty()) {
                setToolbarTitle(customTitle);
            } else {
                setToolbarTitle(getString(config.getToolbarTitleResId()));
            }
            showBackArrow(config.showBackButton());
        } else {
            // Default for Home/Settings/About/Advanced - use app name, show hamburger
            setToolbarTitle(getString(R.string.app_name));
            showBackArrow(false);
        }
    }

    private void loadFragment(Fragment fragment) {
        loadFragment(fragment, false);
    }

    private void loadFragment(Fragment fragment, boolean withAnimation) {
        loadFragment(fragment, withAnimation, false);
    }

    private void loadFragment(Fragment fragment, boolean withAnimation, boolean reverseAnimation) {
        currentFragment = fragment;

        // Configure toolbar based on fragment type
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (withAnimation) {
            if (reverseAnimation) {
                // Reverse animation for going back (slide in from left, slide out to right)
                transaction.setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            } else {
                // Forward animation (slide in from right, slide out to left)
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            }
        }
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    private void loadFragmentForNavItem(int navItemId) {
        loadFragmentForNavItemWithAnimation(navItemId, false);
    }

    private void loadFragmentForNavItemWithAnimation(int navItemId, boolean withAnimation) {
        if (navItemId == R.id.nav_home) {
            loadFragment(new HomeFragment(), withAnimation);
        } else if (navItemId == R.id.nav_eu_red) {
            loadFragment(new EURedFragment(), withAnimation);
        } else if (navItemId == R.id.nav_custom_script) {
            loadFragment(new CustomScriptFragment(), withAnimation);
        } else if (navItemId == R.id.nav_script_documentation) {
            loadFragment(new ScriptDocumentationFragment(), withAnimation);
        } else if (navItemId == R.id.nav_advanced) {
            loadFragment(new AdvancedFragment(), withAnimation);
        } else if (navItemId == R.id.nav_settings) {
            loadFragment(new SettingsFragment(), withAnimation);
        } else if (navItemId == R.id.nav_about) {
            loadFragment(new AboutFragment(), withAnimation);
        } else {
            loadFragment(new HomeFragment(), withAnimation);
        }
    }

    @Override
    public void navigateTo(int navItemId) {
        currentNavItemId = navItemId;
        // Use animation when navigating from Home to main features
        boolean useAnimation = (navItemId == R.id.nav_eu_red ||
                                navItemId == R.id.nav_custom_script ||
                                navItemId == R.id.nav_script_documentation ||
                                navItemId == R.id.nav_advanced);
        loadFragmentForNavItemWithAnimation(navItemId, useAnimation);
        // Don't check item if it's not in the drawer menu
        if (navItemId == R.id.nav_home || navItemId == R.id.nav_advanced ||
            navItemId == R.id.nav_settings || navItemId == R.id.nav_about) {
            navigationView.setCheckedItem(navItemId);
        }
    }

    @Override
    public void openCustomScriptWithContent(String scriptContent) {
        // Create a CustomScriptFragment with pre-filled content
        CustomScriptFragment fragment = CustomScriptFragment.newInstanceWithScript(scriptContent);
        currentFragment = fragment;
        currentNavItemId = R.id.nav_custom_script;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    @Override
    public void openCustomScriptWithContentAndEntryId(String scriptContent, String entryId) {
        // Create a CustomScriptFragment with pre-filled content and entry ID for saving changes
        CustomScriptFragment fragment = CustomScriptFragment.newInstanceWithScriptAndEntryId(scriptContent, entryId);
        currentFragment = fragment;
        currentNavItemId = R.id.nav_custom_script;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    @Override
    public void openScriptEditorForResult(String initialScript) {
        // Create a CustomScriptFragment in editor mode for creating a new custom entry
        CustomScriptFragment fragment = CustomScriptFragment.newInstanceForEditing(initialScript);
        currentFragment = fragment;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack("script_editor");
        transaction.commit();
    }

    @Override
    public void openCustomScriptViewOnly(String scriptContent, String entryTitle, String entryDescription) {
        // Create a CustomScriptFragment in view-only mode (read-only, no file operations)
        CustomScriptFragment fragment = CustomScriptFragment.newInstanceViewOnly(scriptContent, entryTitle, entryDescription);
        currentFragment = fragment;
        currentNavItemId = R.id.nav_custom_script;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    /**
     * Opens the CustomScriptFragment with pre-filled content in temporary mode.
     * Full features enabled but changes are NOT saved to the database.
     * @param scriptContent The script content to pre-fill
     */
    public void openCustomScriptTemporary(String scriptContent) {
        // Create a CustomScriptFragment with pre-filled content but NO entry ID
        // This means changes won't be saved to any database entry
        CustomScriptFragment fragment = CustomScriptFragment.newInstanceWithScript(scriptContent);
        currentFragment = fragment;
        currentNavItemId = R.id.nav_custom_script;
        configureToolbarForFragment(fragment);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
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

    private void navigateBackToHome() {
        currentNavItemId = R.id.nav_home;
        loadFragment(new HomeFragment(), true, true);
        navigationView.setCheckedItem(R.id.nav_home);
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
        } else if (currentFragment instanceof EURedFragment) {
            if (((EURedFragment) currentFragment).handleBackPress()) {
                return;
            }
            // Navigate back to Home launcher with animation
            navigateBackToHome();
        } else if (currentFragment instanceof CustomScriptFragment) {
            if (((CustomScriptFragment) currentFragment).handleBackPress()) {
                return;
            }
            // Check if there's a back stack entry (e.g., from editor mode)
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                // Navigate back to Home launcher with animation
                navigateBackToHome();
            }
        } else if (currentFragment instanceof AdvancedFragment) {
            if (((AdvancedFragment) currentFragment).handleBackPress()) {
                return;
            }
            // Navigate back to Home launcher
            navigateTo(R.id.nav_home);
        } else if (currentFragment instanceof ScriptDocumentationFragment) {
            // Navigate back to Home launcher with animation
            navigateBackToHome();
        } else if (currentFragment instanceof SettingsFragment) {
            // Navigate back to Home launcher
            navigateTo(R.id.nav_home);
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
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (isScanningFromSettings) {
                    // User cancelled scanning, reload settings fragment
                    loadFragment(new SettingsFragment());
                    navigationView.setCheckedItem(R.id.nav_settings);
                    currentNavItemId = R.id.nav_settings;
                    isScanningFromSettings = false;
                } else {
                    // Back stack is empty, update toolbar for current visible fragment
                    Fragment currentVisibleFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    if (currentVisibleFragment != null) {
                        currentFragment = currentVisibleFragment;
                        configureToolbarForFragment(currentVisibleFragment);
                    }
                }
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

    public static void setLightStatusBar(Activity activity) {
        if (activity == null) return;

        Window window = activity.getWindow();

        // 1. Set the Status Bar background color to White
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.WHITE);

        // 2. Set the Status Bar Text/Icons to Black
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Modern Way (Android 11+)
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Legacy Way (Android 6.0 to 10.0)
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }
}
