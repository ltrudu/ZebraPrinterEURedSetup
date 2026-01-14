package com.zebra.zebraprintereuredsetup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.zebra.zebraprintereuredsetup.Constants.SETTINGS_DEFAULT_NEW_PASSWORD;
import static com.zebra.zebraprintereuredsetup.Constants.DEFAULT_PASSWORD_HTTP;

public class SettingsFragment extends Fragment {

    private static final int MIN_NEW_PASSWORD_LENGTH = 14;
    private static final int MIN_HTTP_ADMIN_PASSWORD_LENGTH = 20;
    private static final long SAVE_DEBOUNCE_MS = 300;

    private TextInputLayout textInputLayoutNewPassword;
    private TextInputLayout textInputLayoutHttpAdminPassword;
    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextHttpAdminPassword;
    private EditText editTextAuthPassword;
    private TextView textViewStatus;
    private AutoCompleteTextView autoCompleteLanguage;

    // Checkboxes for enabling/disabling sections
    private CheckBox checkboxChangePasswordEnabled;
    private CheckBox checkboxHttpAdminEnabled;

    // Protected Mode checkbox
    private CheckBox checkboxProtectedModeAllowed;

    // EURed Configuration checkboxes
    private CheckBox checkboxFirmwareDownload;
    private CheckBox checkboxTcpEnable;
    private CheckBox checkboxLpdEnable;
    private CheckBox checkboxHttpsEnable;
    private CheckBox checkboxFtpEnable;
    private CheckBox checkboxSnmpEnable;
    private CheckBox checkboxWlanEnable;
    private CheckBox checkboxUsbMirrorEnable;
    private CheckBox checkboxZbiEnable;

    // Bluetooth Discoverable
    private CheckBox checkboxBluetoothDiscoverableEnabled;
    private Spinner spinnerBluetoothDiscoverable;
    private LinearLayout contentBluetoothDiscoverable;

    // Setvar Wlan Enable
    private CheckBox checkboxSetvarWlanEnableEnabled;
    private Spinner spinnerSetvarWlanEnable;
    private LinearLayout contentSetvarWlanEnable;

    // Setvar IP HTTP Enable
    private CheckBox checkboxSetvarIpHttpEnableEnabled;
    private Spinner spinnerSetvarIpHttpEnable;
    private LinearLayout contentSetvarIpHttpEnable;

    // Display Password Level
    private CheckBox checkboxDisplayPasswordLevelEnabled;
    private Spinner spinnerDisplayPasswordLevel;
    private LinearLayout contentDisplayPasswordLevel;

    // Display Password Current
    private CheckBox checkboxDisplayPasswordCurrentEnabled;
    private EditText editTextPasswordCurrent;
    private LinearLayout contentDisplayPasswordCurrent;

    // Device Prompted Network Reset
    private CheckBox checkboxDevicePromptedNetworkResetEnabled;
    private Spinner spinnerDevicePromptedNetworkReset;
    private LinearLayout contentDevicePromptedNetworkReset;

    // Content containers for showing/hiding
    private LinearLayout contentChangePassword;
    private LinearLayout contentHttpAdminPassword;

    // Clear buttons for password fields
    private Button buttonClearOldPassword;
    private Button buttonPrefillNewPassword;
    private Button buttonPrefillHttpAdminPassword;
    private Button buttonClearAuthPassword;
    private Button buttonClearPasswordCurrent;

    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private final Runnable saveRunnable = this::saveValuesWithStatus;

    // File operations buttons
    private MaterialButton buttonImportSettings;
    private MaterialButton buttonExportSettings;

    // Activity result launchers
    private ActivityResultLauncher<Intent> importSettingsLauncher;
    private ActivityResultLauncher<Intent> exportSettingsLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupActivityResultLaunchers() {
        // Import settings launcher
        importSettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importSettingsFromUri(uri);
                    }
                }
            }
        );

        // Export settings launcher
        exportSettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportSettingsToUri(uri);
                    }
                }
            }
        );
    }

    private void setupViews(View view) {
        // Password input layouts and fields
        textInputLayoutNewPassword = view.findViewById(R.id.textInputLayoutNewPassword);
        textInputLayoutHttpAdminPassword = view.findViewById(R.id.textInputLayoutHttpAdminPassword);
        editTextOldPassword = view.findViewById(R.id.editTextOldPassword);
        editTextNewPassword = view.findViewById(R.id.editTextNewPassword);
        editTextHttpAdminPassword = view.findViewById(R.id.editTextHttpAdminPassword);
        editTextAuthPassword = view.findViewById(R.id.editTextAuthPassword);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        autoCompleteLanguage = view.findViewById(R.id.autoCompleteLanguage);

        // Checkboxes
        checkboxChangePasswordEnabled = view.findViewById(R.id.checkboxChangePasswordEnabled);
        checkboxHttpAdminEnabled = view.findViewById(R.id.checkboxHttpAdminEnabled);

        // Content containers
        contentChangePassword = view.findViewById(R.id.contentChangePassword);
        contentHttpAdminPassword = view.findViewById(R.id.contentHttpAdminPassword);

        // Protected Mode checkbox
        checkboxProtectedModeAllowed = view.findViewById(R.id.checkboxProtectedModeAllowed);

        // EURed Configuration checkboxes
        checkboxFirmwareDownload = view.findViewById(R.id.checkboxFirmwareDownload);
        checkboxTcpEnable = view.findViewById(R.id.checkboxTcpEnable);
        checkboxLpdEnable = view.findViewById(R.id.checkboxLpdEnable);
        checkboxHttpsEnable = view.findViewById(R.id.checkboxHttpsEnable);
        checkboxFtpEnable = view.findViewById(R.id.checkboxFtpEnable);
        checkboxSnmpEnable = view.findViewById(R.id.checkboxSnmpEnable);
        checkboxWlanEnable = view.findViewById(R.id.checkboxWlanEnable);
        checkboxUsbMirrorEnable = view.findViewById(R.id.checkboxUsbMirrorEnable);
        checkboxZbiEnable = view.findViewById(R.id.checkboxZbiEnable);

        // Bluetooth Discoverable
        checkboxBluetoothDiscoverableEnabled = view.findViewById(R.id.checkboxBluetoothDiscoverableEnabled);
        spinnerBluetoothDiscoverable = view.findViewById(R.id.spinnerBluetoothDiscoverable);
        contentBluetoothDiscoverable = view.findViewById(R.id.contentBluetoothDiscoverable);

        // Setup Bluetooth Discoverable spinner (Yes/No)
        String[] bluetoothDiscoverableOptions = {
            getString(R.string.option_yes),
            getString(R.string.option_no)
        };
        ArrayAdapter<String> bluetoothDiscoverableAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, bluetoothDiscoverableOptions);
        bluetoothDiscoverableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBluetoothDiscoverable.setAdapter(bluetoothDiscoverableAdapter);

        // Setvar Wlan Enable
        checkboxSetvarWlanEnableEnabled = view.findViewById(R.id.checkboxSetvarWlanEnableEnabled);
        spinnerSetvarWlanEnable = view.findViewById(R.id.spinnerSetvarWlanEnable);
        contentSetvarWlanEnable = view.findViewById(R.id.contentSetvarWlanEnable);

        // Setup Setvar Wlan Enable spinner (On/Off)
        String[] wlanEnableOptions = {
            getString(R.string.option_on),
            getString(R.string.option_off)
        };
        ArrayAdapter<String> wlanEnableAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, wlanEnableOptions);
        wlanEnableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSetvarWlanEnable.setAdapter(wlanEnableAdapter);

        // Setvar IP HTTP Enable
        checkboxSetvarIpHttpEnableEnabled = view.findViewById(R.id.checkboxSetvarIpHttpEnableEnabled);
        spinnerSetvarIpHttpEnable = view.findViewById(R.id.spinnerSetvarIpHttpEnable);
        contentSetvarIpHttpEnable = view.findViewById(R.id.contentSetvarIpHttpEnable);

        // Setup Setvar IP HTTP Enable spinner (On/Off)
        String[] ipHttpEnableOptions = {
            getString(R.string.option_on),
            getString(R.string.option_off)
        };
        ArrayAdapter<String> ipHttpEnableAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, ipHttpEnableOptions);
        ipHttpEnableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSetvarIpHttpEnable.setAdapter(ipHttpEnableAdapter);

        // Display Password Level
        checkboxDisplayPasswordLevelEnabled = view.findViewById(R.id.checkboxDisplayPasswordLevelEnabled);
        spinnerDisplayPasswordLevel = view.findViewById(R.id.spinnerDisplayPasswordLevel);
        contentDisplayPasswordLevel = view.findViewById(R.id.contentDisplayPasswordLevel);

        // Setup Display Password Level spinner (All, None, Selected)
        String[] passwordLevelOptions = {
            getString(R.string.option_all),
            getString(R.string.option_none),
            getString(R.string.option_selected)
        };
        ArrayAdapter<String> passwordLevelAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, passwordLevelOptions);
        passwordLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisplayPasswordLevel.setAdapter(passwordLevelAdapter);

        // Display Password Current
        checkboxDisplayPasswordCurrentEnabled = view.findViewById(R.id.checkboxDisplayPasswordCurrentEnabled);
        editTextPasswordCurrent = view.findViewById(R.id.editTextPasswordCurrent);
        contentDisplayPasswordCurrent = view.findViewById(R.id.contentDisplayPasswordCurrent);

        // Device Prompted Network Reset
        checkboxDevicePromptedNetworkResetEnabled = view.findViewById(R.id.checkboxDevicePromptedNetworkResetEnabled);
        spinnerDevicePromptedNetworkReset = view.findViewById(R.id.spinnerDevicePromptedNetworkReset);
        contentDevicePromptedNetworkReset = view.findViewById(R.id.contentDevicePromptedNetworkReset);

        // Setup Device Prompted Network Reset spinner
        String[] networkResetOptions = {
            getString(R.string.option_yes),
            getString(R.string.option_no)
        };
        ArrayAdapter<String> networkResetAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, networkResetOptions);
        networkResetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevicePromptedNetworkReset.setAdapter(networkResetAdapter);

        // Setup language dropdown
        setupLanguageDropdown();

        buttonClearOldPassword = view.findViewById(R.id.buttonClearOldPassword);
        buttonPrefillNewPassword = view.findViewById(R.id.buttonPrefillNewPassword);
        buttonPrefillHttpAdminPassword = view.findViewById(R.id.buttonPrefillHttpAdminPassword);
        Button buttonScanOldPassword = view.findViewById(R.id.buttonScanOldPassword);
        Button buttonScanNewPassword = view.findViewById(R.id.buttonScanNewPassword);
        Button buttonScanHttpAdminPassword = view.findViewById(R.id.buttonScanHttpAdminPassword);
        buttonClearAuthPassword = view.findViewById(R.id.buttonClearAuthPassword);
        Button buttonScanAuthPassword = view.findViewById(R.id.buttonScanAuthPassword);
        buttonClearPasswordCurrent = view.findViewById(R.id.buttonClearPasswordCurrent);
        Button buttonScanPasswordCurrent = view.findViewById(R.id.buttonScanPasswordCurrent);

        // Load saved values
        editTextOldPassword.setText(SettingsHelper.getOldAdminpassword(requireContext()));
        editTextNewPassword.setText(SettingsHelper.getNewAdminpassword(requireContext()));
        editTextHttpAdminPassword.setText(SettingsHelper.getHttpadminpasswordKey(requireContext()));
        editTextAuthPassword.setText(SettingsHelper.getAuthPassword(requireContext()));
        editTextPasswordCurrent.setText(SettingsHelper.getDisplayPasswordCurrent(requireContext()));

        // Set initial visibility of clear buttons based on content
        updateClearButtonsVisibility();

        // Load and apply Display Password Current checkbox state
        boolean displayPasswordCurrentEnabled = SettingsHelper.getDisplayPasswordCurrentEnabled(requireContext());
        checkboxDisplayPasswordCurrentEnabled.setChecked(displayPasswordCurrentEnabled);
        contentDisplayPasswordCurrent.setVisibility(displayPasswordCurrentEnabled ? View.VISIBLE : View.GONE);

        // Display Password Current checkbox listener
        checkboxDisplayPasswordCurrentEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentDisplayPasswordCurrent.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveDisplayPasswordCurrentEnabled(requireContext(), isChecked);
        });

        // Load and apply Device Prompted Network Reset state
        boolean devicePromptedNetworkResetEnabled = SettingsHelper.getDevicePromptedNetworkResetEnabled(requireContext());
        checkboxDevicePromptedNetworkResetEnabled.setChecked(devicePromptedNetworkResetEnabled);
        contentDevicePromptedNetworkReset.setVisibility(devicePromptedNetworkResetEnabled ? View.VISIBLE : View.GONE);

        // Set spinner selection based on saved value (true=Yes=0, false=No=1)
        boolean savedNetworkResetValue = SettingsHelper.getDevicePromptedNetworkReset(requireContext());
        spinnerDevicePromptedNetworkReset.setSelection(savedNetworkResetValue ? 0 : 1);

        // Device Prompted Network Reset checkbox listener
        checkboxDevicePromptedNetworkResetEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentDevicePromptedNetworkReset.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveDevicePromptedNetworkResetEnabled(requireContext(), isChecked);
        });

        // Device Prompted Network Reset spinner listener (Yes=true, No=false)
        spinnerDevicePromptedNetworkReset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                boolean value = (position == 0); // Yes=true, No=false
                SettingsHelper.saveDevicePromptedNetworkReset(requireContext(), value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load and apply checkbox states
        boolean changePasswordEnabled = SettingsHelper.getChangePasswordEnabled(requireContext());
        boolean httpAdminEnabled = SettingsHelper.getHttpAdminEnabled(requireContext());
        checkboxChangePasswordEnabled.setChecked(changePasswordEnabled);
        checkboxHttpAdminEnabled.setChecked(httpAdminEnabled);
        contentChangePassword.setVisibility(changePasswordEnabled ? View.VISIBLE : View.GONE);
        contentHttpAdminPassword.setVisibility(httpAdminEnabled ? View.VISIBLE : View.GONE);

        // Checkbox listeners to toggle content visibility
        checkboxChangePasswordEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentChangePassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveChangePasswordEnabled(requireContext(), isChecked);
        });

        checkboxHttpAdminEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentHttpAdminPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveHttpAdminEnabled(requireContext(), isChecked);
        });

        // Load and apply Protected Mode checkbox state
        checkboxProtectedModeAllowed.setChecked(SettingsHelper.getProtectedModeAllowed(requireContext()));
        checkboxProtectedModeAllowed.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveProtectedModeAllowed(requireContext(), isChecked));

        // Load and apply EURed Configuration checkbox states
        checkboxFirmwareDownload.setChecked(SettingsHelper.getEuredFirmwareDownload(requireContext()));
        checkboxTcpEnable.setChecked(SettingsHelper.getEuredTcpEnable(requireContext()));
        checkboxLpdEnable.setChecked(SettingsHelper.getEuredLpdEnable(requireContext()));
        checkboxHttpsEnable.setChecked(SettingsHelper.getEuredHttpsEnable(requireContext()));
        checkboxFtpEnable.setChecked(SettingsHelper.getEuredFtpEnable(requireContext()));
        checkboxSnmpEnable.setChecked(SettingsHelper.getEuredSnmpEnable(requireContext()));
        checkboxWlanEnable.setChecked(SettingsHelper.getEuredWlanEnable(requireContext()));
        checkboxUsbMirrorEnable.setChecked(SettingsHelper.getEuredUsbMirrorEnable(requireContext()));
        checkboxZbiEnable.setChecked(SettingsHelper.getEuredZbiEnable(requireContext()));

        // EURed Configuration checkbox listeners
        checkboxFirmwareDownload.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredFirmwareDownload(requireContext(), isChecked));
        checkboxTcpEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredTcpEnable(requireContext(), isChecked));
        checkboxLpdEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredLpdEnable(requireContext(), isChecked));
        checkboxHttpsEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredHttpsEnable(requireContext(), isChecked));
        checkboxFtpEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredFtpEnable(requireContext(), isChecked));
        checkboxSnmpEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredSnmpEnable(requireContext(), isChecked));
        checkboxWlanEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredWlanEnable(requireContext(), isChecked));
        checkboxUsbMirrorEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredUsbMirrorEnable(requireContext(), isChecked));
        checkboxZbiEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveEuredZbiEnable(requireContext(), isChecked));

        // Load and apply Bluetooth Discoverable state
        boolean bluetoothDiscoverableEnabled = SettingsHelper.getBluetoothDiscoverableEnabled(requireContext());
        checkboxBluetoothDiscoverableEnabled.setChecked(bluetoothDiscoverableEnabled);
        contentBluetoothDiscoverable.setVisibility(bluetoothDiscoverableEnabled ? View.VISIBLE : View.GONE);

        // Set spinner selection based on saved value (true=Yes=0, false=No=1)
        boolean savedBluetoothDiscoverable = SettingsHelper.getBluetoothDiscoverable(requireContext());
        spinnerBluetoothDiscoverable.setSelection(savedBluetoothDiscoverable ? 0 : 1);

        // Bluetooth Discoverable checkbox listener
        checkboxBluetoothDiscoverableEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentBluetoothDiscoverable.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveBluetoothDiscoverableEnabled(requireContext(), isChecked);
        });

        // Bluetooth Discoverable spinner listener (Yes=true, No=false)
        spinnerBluetoothDiscoverable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                boolean value = (position == 0); // Yes=true, No=false
                SettingsHelper.saveBluetoothDiscoverable(requireContext(), value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load and apply Setvar Wlan Enable state
        boolean setvarWlanEnableEnabled = SettingsHelper.getSetvarWlanEnableEnabled(requireContext());
        checkboxSetvarWlanEnableEnabled.setChecked(setvarWlanEnableEnabled);
        contentSetvarWlanEnable.setVisibility(setvarWlanEnableEnabled ? View.VISIBLE : View.GONE);

        // Set spinner selection based on saved value (true=On=0, false=Off=1)
        boolean savedWlanEnable = SettingsHelper.getSetvarWlanEnable(requireContext());
        spinnerSetvarWlanEnable.setSelection(savedWlanEnable ? 0 : 1);

        // Setvar Wlan Enable checkbox listener
        checkboxSetvarWlanEnableEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentSetvarWlanEnable.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveSetvarWlanEnableEnabled(requireContext(), isChecked);
        });

        // Setvar Wlan Enable spinner listener (On=true, Off=false)
        spinnerSetvarWlanEnable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                boolean value = (position == 0); // On=true, Off=false
                SettingsHelper.saveSetvarWlanEnable(requireContext(), value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load and apply Setvar IP HTTP Enable state
        boolean setvarIpHttpEnableEnabled = SettingsHelper.getSetvarIpHttpEnableEnabled(requireContext());
        checkboxSetvarIpHttpEnableEnabled.setChecked(setvarIpHttpEnableEnabled);
        contentSetvarIpHttpEnable.setVisibility(setvarIpHttpEnableEnabled ? View.VISIBLE : View.GONE);

        // Set spinner selection based on saved value (true=On=0, false=Off=1)
        boolean savedIpHttpEnable = SettingsHelper.getSetvarIpHttpEnable(requireContext());
        spinnerSetvarIpHttpEnable.setSelection(savedIpHttpEnable ? 0 : 1);

        // Setvar IP HTTP Enable checkbox listener
        checkboxSetvarIpHttpEnableEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentSetvarIpHttpEnable.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveSetvarIpHttpEnableEnabled(requireContext(), isChecked);
        });

        // Setvar IP HTTP Enable spinner listener (On=true, Off=false)
        spinnerSetvarIpHttpEnable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                boolean value = (position == 0); // On=true, Off=false
                SettingsHelper.saveSetvarIpHttpEnable(requireContext(), value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Load and apply Display Password Level state
        boolean displayPasswordLevelEnabled = SettingsHelper.getDisplayPasswordLevelEnabled(requireContext());
        checkboxDisplayPasswordLevelEnabled.setChecked(displayPasswordLevelEnabled);
        contentDisplayPasswordLevel.setVisibility(displayPasswordLevelEnabled ? View.VISIBLE : View.GONE);

        // Set spinner selection based on saved value (All=0, None=1, Selected=2)
        String savedPasswordLevel = SettingsHelper.getDisplayPasswordLevel(requireContext());
        int passwordLevelIndex = 1; // Default to None
        if (savedPasswordLevel.equals("All")) passwordLevelIndex = 0;
        else if (savedPasswordLevel.equals("None")) passwordLevelIndex = 1;
        else if (savedPasswordLevel.equals("Selected")) passwordLevelIndex = 2;
        spinnerDisplayPasswordLevel.setSelection(passwordLevelIndex);

        // Display Password Level checkbox listener
        checkboxDisplayPasswordLevelEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contentDisplayPasswordLevel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            SettingsHelper.saveDisplayPasswordLevelEnabled(requireContext(), isChecked);
        });

        // Display Password Level spinner listener
        spinnerDisplayPasswordLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                String value;
                switch (position) {
                    case 0: value = "All"; break;
                    case 2: value = "Selected"; break;
                    default: value = "None"; break;
                }
                SettingsHelper.saveDisplayPasswordLevel(requireContext(), value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Clear buttons - tap to clear, long press to reset to default
        buttonClearOldPassword.setOnClickListener(v -> editTextOldPassword.setText(""));
        buttonClearOldPassword.setOnLongClickListener(v -> {
            editTextOldPassword.setText(Constants.SETTINGS_DEFAULT_OLD_PASSWORD);
            return true;
        });

        buttonPrefillNewPassword.setOnClickListener(v -> editTextNewPassword.setText(""));
        buttonPrefillNewPassword.setOnLongClickListener(v -> {
            editTextNewPassword.setText(SETTINGS_DEFAULT_NEW_PASSWORD);
            return true;
        });

        buttonPrefillHttpAdminPassword.setOnClickListener(v -> editTextHttpAdminPassword.setText(""));
        buttonPrefillHttpAdminPassword.setOnLongClickListener(v -> {
            editTextHttpAdminPassword.setText(DEFAULT_PASSWORD_HTTP);
            return true;
        });

        buttonClearAuthPassword.setOnClickListener(v -> editTextAuthPassword.setText(""));
        buttonClearAuthPassword.setOnLongClickListener(v -> {
            editTextAuthPassword.setText(Constants.SETTINGS_DEFAULT_PASSWORD);
            return true;
        });

        buttonClearPasswordCurrent.setOnClickListener(v -> editTextPasswordCurrent.setText(""));
        buttonClearPasswordCurrent.setOnLongClickListener(v -> {
            editTextPasswordCurrent.setText(Constants.DEFAULT_DISPLAY_PASSWORD_CURRENT);
            return true;
        });

        // Scan buttons - open barcode scanner via MainActivity
        buttonScanOldPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_OLD_PASSWORD));
        buttonScanNewPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_NEW_PASSWORD));
        buttonScanHttpAdminPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_HTTP_ADMIN_PASSWORD));
        buttonScanAuthPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_AUTH_PASSWORD));
        buttonScanPasswordCurrent.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_PASSWORD_CURRENT));

        // Save values on text change (debounced)
        TextWatcher saveWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswords();
                updateClearButtonsVisibility();
                debounceSave();
            }
        };

        editTextOldPassword.addTextChangedListener(saveWatcher);
        editTextNewPassword.addTextChangedListener(saveWatcher);
        editTextHttpAdminPassword.addTextChangedListener(saveWatcher);
        editTextAuthPassword.addTextChangedListener(saveWatcher);
        editTextPasswordCurrent.addTextChangedListener(saveWatcher);

        validatePasswords();

        // File operations buttons
        buttonImportSettings = view.findViewById(R.id.buttonImportSettings);
        buttonExportSettings = view.findViewById(R.id.buttonExportSettings);

        buttonImportSettings.setOnClickListener(v -> openImportDialog());
        buttonExportSettings.setOnClickListener(v -> openExportDialog());

        // Setup barcode result listener
        setupBarcodeResultListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancel any pending debounced save and save immediately
        saveHandler.removeCallbacks(saveRunnable);
        saveValues();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending callbacks to prevent crashes
        saveHandler.removeCallbacks(saveRunnable);
    }

    private void debounceSave() {
        if (!isAdded()) {
            return;
        }
        saveHandler.removeCallbacks(saveRunnable);
        // Show saving status
        setStatus(getString(R.string.status_saving_settings), requireContext().getColor(android.R.color.holo_orange_light));
        saveHandler.postDelayed(saveRunnable, SAVE_DEBOUNCE_MS);
    }

    private void setStatus(String message, int color) {
        if (textViewStatus != null) {
            textViewStatus.setText(message);
            textViewStatus.setTextColor(color);
        }
    }

    private void validatePasswords() {
        String newPassword = editTextNewPassword.getText().toString();
        String httpAdminPassword = editTextHttpAdminPassword.getText().toString();

        if (newPassword.length() >= MIN_NEW_PASSWORD_LENGTH) {
            textInputLayoutNewPassword.setError(null);
        } else {
            int remaining = MIN_NEW_PASSWORD_LENGTH - newPassword.length();
            textInputLayoutNewPassword.setError(getString(R.string.error_min_characters_required, MIN_NEW_PASSWORD_LENGTH, remaining));
        }

        if (httpAdminPassword.length() >= MIN_HTTP_ADMIN_PASSWORD_LENGTH) {
            textInputLayoutHttpAdminPassword.setError(null);
        } else {
            int remaining = MIN_HTTP_ADMIN_PASSWORD_LENGTH - httpAdminPassword.length();
            textInputLayoutHttpAdminPassword.setError(getString(R.string.error_min_characters_required, MIN_HTTP_ADMIN_PASSWORD_LENGTH, remaining));
        }
    }

    private void updateClearButtonsVisibility() {
        buttonClearOldPassword.setVisibility(
                editTextOldPassword.getText().length() > 0 ? View.VISIBLE : View.GONE);
        buttonPrefillNewPassword.setVisibility(
                editTextNewPassword.getText().length() > 0 ? View.VISIBLE : View.GONE);
        buttonPrefillHttpAdminPassword.setVisibility(
                editTextHttpAdminPassword.getText().length() > 0 ? View.VISIBLE : View.GONE);
        buttonClearAuthPassword.setVisibility(
                editTextAuthPassword.getText().length() > 0 ? View.VISIBLE : View.GONE);
        buttonClearPasswordCurrent.setVisibility(
                editTextPasswordCurrent.getText().length() > 0 ? View.VISIBLE : View.GONE);
    }

    private void saveValuesWithStatus() {
        if (!isAdded()) {
            return;
        }
        if (saveValues()) {
            setStatus(getString(R.string.status_settings_saved), requireContext().getColor(android.R.color.holo_green_dark));
        } else {
            setStatus(getString(R.string.status_settings_invalid), requireContext().getColor(android.R.color.holo_red_dark));
        }
    }

    private boolean saveValues() {
        if (!isAdded()) {
            return false;
        }
        String newPassword = editTextNewPassword.getText().toString();
        String httpAdminPassword = editTextHttpAdminPassword.getText().toString();

        // Only save if passwords meet minimum length requirements
        if (newPassword.length() >= MIN_NEW_PASSWORD_LENGTH &&
            httpAdminPassword.length() >= MIN_HTTP_ADMIN_PASSWORD_LENGTH) {
            SettingsHelper.saveOldAdminPassword(requireContext(), editTextOldPassword.getText().toString());
            SettingsHelper.saveNewAdminPassword(requireContext(), newPassword);
            SettingsHelper.saveHttpadminpasswordKey(requireContext(), httpAdminPassword);
            SettingsHelper.saveAuthPassword(requireContext(), editTextAuthPassword.getText().toString());
            SettingsHelper.saveDisplayPasswordCurrent(requireContext(), editTextPasswordCurrent.getText().toString());
            return true;
        }
        return false;
    }

    private void openBarcodeScanner(int targetField) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openBarcodeScannerFromSettings(targetField);
        }
    }

    private void setupLanguageDropdown() {
        String[] languageNames = LocaleHelper.getLanguageDisplayNames(requireContext());

        // Use non-filtering adapter to show all items always
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languageNames
        ) {
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = languageNames;
                        results.count = languageNames.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        autoCompleteLanguage.setAdapter(adapter);

        // Set current selection
        String currentLanguage = SettingsHelper.getLanguage(requireContext());
        int currentIndex = LocaleHelper.getLanguageIndex(currentLanguage);
        autoCompleteLanguage.setText(languageNames[currentIndex], false);

        // Handle selection
        autoCompleteLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLanguageCode = LocaleHelper.LANGUAGE_CODES[position];
            SettingsHelper.saveLanguage(requireContext(), selectedLanguageCode);

            // Recreate activity to apply new locale
            if (getActivity() != null) {
                getActivity().recreate();
            }
        });
    }

    private void setupBarcodeResultListener() {
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
                "settings_barcode_result",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String barcode = result.getString(BarcodeScannerFragment.RESULT_BARCODE);
                    int targetField = result.getInt(BarcodeScannerFragment.RESULT_TARGET_FIELD);

                    if (barcode != null) {
                        switch (targetField) {
                            case BarcodeScannerFragment.FIELD_OLD_PASSWORD:
                                editTextOldPassword.setText(barcode);
                                break;
                            case BarcodeScannerFragment.FIELD_NEW_PASSWORD:
                                editTextNewPassword.setText(barcode);
                                break;
                            case BarcodeScannerFragment.FIELD_HTTP_ADMIN_PASSWORD:
                                editTextHttpAdminPassword.setText(barcode);
                                break;
                            case BarcodeScannerFragment.FIELD_AUTH_PASSWORD:
                                editTextAuthPassword.setText(barcode);
                                break;
                            case BarcodeScannerFragment.FIELD_PASSWORD_CURRENT:
                                editTextPasswordCurrent.setText(barcode);
                                break;
                        }
                    }
                });
    }

    private void openImportDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/json", "text/plain", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Try to open in Documents folder
        Uri documentsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsUri);

        importSettingsLauncher.launch(intent);
    }

    private void openExportDialog() {
        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "ZEURED_" + timestamp + ".json";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        // Try to open in Documents folder
        Uri documentsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsUri);

        exportSettingsLauncher.launch(intent);
    }

    private void importSettingsFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                JSONObject json = new JSONObject(stringBuilder.toString());
                applySettingsFromJson(json);
                Toast.makeText(requireContext(), R.string.status_settings_imported, Toast.LENGTH_SHORT).show();

                // Recreate activity to apply all imported settings
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_import_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void exportSettingsToUri(Uri uri) {
        try {
            JSONObject json = createSettingsJson();
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(json.toString(2).getBytes());
                outputStream.close();
                Toast.makeText(requireContext(), R.string.status_settings_exported, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_export_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private JSONObject createSettingsJson() throws Exception {
        JSONObject json = new JSONObject();

        // Passwords
        json.put("oldAdminPassword", SettingsHelper.getOldAdminpassword(requireContext()));
        json.put("newAdminPassword", SettingsHelper.getNewAdminpassword(requireContext()));
        json.put("httpAdminPassword", SettingsHelper.getHttpadminpasswordKey(requireContext()));
        json.put("authPassword", SettingsHelper.getAuthPassword(requireContext()));
        json.put("displayPasswordCurrent", SettingsHelper.getDisplayPasswordCurrent(requireContext()));

        // Checkbox states
        json.put("changePasswordEnabled", SettingsHelper.getChangePasswordEnabled(requireContext()));
        json.put("httpAdminEnabled", SettingsHelper.getHttpAdminEnabled(requireContext()));

        // Language
        json.put("language", SettingsHelper.getLanguage(requireContext()));

        // Connectivity
        json.put("connectivityType", SettingsHelper.getConnectivityType(requireContext()));
        json.put("bluetoothAddress", SettingsHelper.getBluetoothAddress(requireContext()));

        // Protected Mode
        json.put("protectedModeAllowed", SettingsHelper.getProtectedModeAllowed(requireContext()));

        // EURed Configuration
        json.put("firmwareDownload", SettingsHelper.getEuredFirmwareDownload(requireContext()));
        json.put("tcpEnable", SettingsHelper.getEuredTcpEnable(requireContext()));
        json.put("lpdEnable", SettingsHelper.getEuredLpdEnable(requireContext()));
        json.put("httpsEnable", SettingsHelper.getEuredHttpsEnable(requireContext()));
        json.put("ftpEnable", SettingsHelper.getEuredFtpEnable(requireContext()));
        json.put("snmpEnable", SettingsHelper.getEuredSnmpEnable(requireContext()));
        json.put("wlanEnable", SettingsHelper.getEuredWlanEnable(requireContext()));
        json.put("usbMirrorEnable", SettingsHelper.getEuredUsbMirrorEnable(requireContext()));
        json.put("zbiEnable", SettingsHelper.getEuredZbiEnable(requireContext()));

        // Bluetooth Discoverable
        json.put("bluetoothDiscoverableEnabled", SettingsHelper.getBluetoothDiscoverableEnabled(requireContext()));
        json.put("bluetoothDiscoverable", SettingsHelper.getBluetoothDiscoverable(requireContext()));

        // Setvar Wlan Enable
        json.put("setvarWlanEnableEnabled", SettingsHelper.getSetvarWlanEnableEnabled(requireContext()));
        json.put("setvarWlanEnable", SettingsHelper.getSetvarWlanEnable(requireContext()));

        // Setvar IP HTTP Enable
        json.put("setvarIpHttpEnableEnabled", SettingsHelper.getSetvarIpHttpEnableEnabled(requireContext()));
        json.put("setvarIpHttpEnable", SettingsHelper.getSetvarIpHttpEnable(requireContext()));

        // Display Password Level
        json.put("displayPasswordLevelEnabled", SettingsHelper.getDisplayPasswordLevelEnabled(requireContext()));
        json.put("displayPasswordLevel", SettingsHelper.getDisplayPasswordLevel(requireContext()));

        // Display Password Current
        json.put("displayPasswordCurrentEnabled", SettingsHelper.getDisplayPasswordCurrentEnabled(requireContext()));

        // Device Prompted Network Reset
        json.put("devicePromptedNetworkResetEnabled", SettingsHelper.getDevicePromptedNetworkResetEnabled(requireContext()));
        json.put("devicePromptedNetworkReset", SettingsHelper.getDevicePromptedNetworkReset(requireContext()));

        return json;
    }

    private void applySettingsFromJson(JSONObject json) throws Exception {
        // Passwords
        if (json.has("oldAdminPassword")) {
            SettingsHelper.saveOldAdminPassword(requireContext(), json.getString("oldAdminPassword"));
        }
        if (json.has("newAdminPassword")) {
            SettingsHelper.saveNewAdminPassword(requireContext(), json.getString("newAdminPassword"));
        }
        if (json.has("httpAdminPassword")) {
            SettingsHelper.saveHttpadminpasswordKey(requireContext(), json.getString("httpAdminPassword"));
        }
        if (json.has("authPassword")) {
            SettingsHelper.saveAuthPassword(requireContext(), json.getString("authPassword"));
        }
        if (json.has("displayPasswordCurrent")) {
            SettingsHelper.saveDisplayPasswordCurrent(requireContext(), json.getString("displayPasswordCurrent"));
        }

        // Checkbox states
        if (json.has("changePasswordEnabled")) {
            SettingsHelper.saveChangePasswordEnabled(requireContext(), json.getBoolean("changePasswordEnabled"));
        }
        if (json.has("httpAdminEnabled")) {
            SettingsHelper.saveHttpAdminEnabled(requireContext(), json.getBoolean("httpAdminEnabled"));
        }

        // Language
        if (json.has("language")) {
            SettingsHelper.saveLanguage(requireContext(), json.getString("language"));
        }

        // Connectivity
        if (json.has("connectivityType")) {
            SettingsHelper.saveConnectivityType(requireContext(), json.getInt("connectivityType"));
        }
        if (json.has("bluetoothAddress")) {
            SettingsHelper.saveBluetoothAddress(requireContext(), json.getString("bluetoothAddress"));
        }

        // Protected Mode
        if (json.has("protectedModeAllowed")) {
            SettingsHelper.saveProtectedModeAllowed(requireContext(), json.getBoolean("protectedModeAllowed"));
        }

        // EURed Configuration
        if (json.has("firmwareDownload")) {
            SettingsHelper.saveEuredFirmwareDownload(requireContext(), json.getBoolean("firmwareDownload"));
        }
        if (json.has("tcpEnable")) {
            SettingsHelper.saveEuredTcpEnable(requireContext(), json.getBoolean("tcpEnable"));
        }
        if (json.has("lpdEnable")) {
            SettingsHelper.saveEuredLpdEnable(requireContext(), json.getBoolean("lpdEnable"));
        }
        if (json.has("httpsEnable")) {
            SettingsHelper.saveEuredHttpsEnable(requireContext(), json.getBoolean("httpsEnable"));
        }
        if (json.has("ftpEnable")) {
            SettingsHelper.saveEuredFtpEnable(requireContext(), json.getBoolean("ftpEnable"));
        }
        if (json.has("snmpEnable")) {
            SettingsHelper.saveEuredSnmpEnable(requireContext(), json.getBoolean("snmpEnable"));
        }
        if (json.has("wlanEnable")) {
            SettingsHelper.saveEuredWlanEnable(requireContext(), json.getBoolean("wlanEnable"));
        }
        if (json.has("usbMirrorEnable")) {
            SettingsHelper.saveEuredUsbMirrorEnable(requireContext(), json.getBoolean("usbMirrorEnable"));
        }
        if (json.has("zbiEnable")) {
            SettingsHelper.saveEuredZbiEnable(requireContext(), json.getBoolean("zbiEnable"));
        }

        // Bluetooth Discoverable
        if (json.has("bluetoothDiscoverableEnabled")) {
            SettingsHelper.saveBluetoothDiscoverableEnabled(requireContext(), json.getBoolean("bluetoothDiscoverableEnabled"));
        }
        if (json.has("bluetoothDiscoverable")) {
            SettingsHelper.saveBluetoothDiscoverable(requireContext(), json.getBoolean("bluetoothDiscoverable"));
        }

        // Setvar Wlan Enable
        if (json.has("setvarWlanEnableEnabled")) {
            SettingsHelper.saveSetvarWlanEnableEnabled(requireContext(), json.getBoolean("setvarWlanEnableEnabled"));
        }
        if (json.has("setvarWlanEnable")) {
            SettingsHelper.saveSetvarWlanEnable(requireContext(), json.getBoolean("setvarWlanEnable"));
        }

        // Setvar IP HTTP Enable
        if (json.has("setvarIpHttpEnableEnabled")) {
            SettingsHelper.saveSetvarIpHttpEnableEnabled(requireContext(), json.getBoolean("setvarIpHttpEnableEnabled"));
        }
        if (json.has("setvarIpHttpEnable")) {
            SettingsHelper.saveSetvarIpHttpEnable(requireContext(), json.getBoolean("setvarIpHttpEnable"));
        }

        // Display Password Level
        if (json.has("displayPasswordLevelEnabled")) {
            SettingsHelper.saveDisplayPasswordLevelEnabled(requireContext(), json.getBoolean("displayPasswordLevelEnabled"));
        }
        if (json.has("displayPasswordLevel")) {
            SettingsHelper.saveDisplayPasswordLevel(requireContext(), json.getString("displayPasswordLevel"));
        }

        // Display Password Current
        if (json.has("displayPasswordCurrentEnabled")) {
            SettingsHelper.saveDisplayPasswordCurrentEnabled(requireContext(), json.getBoolean("displayPasswordCurrentEnabled"));
        }

        // Device Prompted Network Reset
        if (json.has("devicePromptedNetworkResetEnabled")) {
            SettingsHelper.saveDevicePromptedNetworkResetEnabled(requireContext(), json.getBoolean("devicePromptedNetworkResetEnabled"));
        }
        if (json.has("devicePromptedNetworkReset")) {
            SettingsHelper.saveDevicePromptedNetworkReset(requireContext(), json.getBoolean("devicePromptedNetworkReset"));
        }
    }
}
