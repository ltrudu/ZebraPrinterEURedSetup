package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

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

    // Bluetooth Discoverable checkbox
    private CheckBox checkboxBluetoothDiscoverable;

    // Content containers for showing/hiding
    private LinearLayout contentChangePassword;
    private LinearLayout contentHttpAdminPassword;

    // Clear buttons for password fields
    private Button buttonClearOldPassword;
    private Button buttonPrefillNewPassword;
    private Button buttonPrefillHttpAdminPassword;
    private Button buttonClearAuthPassword;

    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private final Runnable saveRunnable = this::saveValuesWithStatus;

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

        // Bluetooth Discoverable checkbox
        checkboxBluetoothDiscoverable = view.findViewById(R.id.checkboxBluetoothDiscoverable);

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

        // Load saved values
        editTextOldPassword.setText(SettingsHelper.getOldAdminpassword(requireContext()));
        editTextNewPassword.setText(SettingsHelper.getNewAdminpassword(requireContext()));
        editTextHttpAdminPassword.setText(SettingsHelper.getHttpadminpasswordKey(requireContext()));
        editTextAuthPassword.setText(SettingsHelper.getAuthPassword(requireContext()));

        // Set initial visibility of clear buttons based on content
        updateClearButtonsVisibility();

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

        // Load and apply Bluetooth Discoverable checkbox state
        checkboxBluetoothDiscoverable.setChecked(SettingsHelper.getBluetoothDiscoverable(requireContext()));
        checkboxBluetoothDiscoverable.setOnCheckedChangeListener((buttonView, isChecked) ->
            SettingsHelper.saveBluetoothDiscoverable(requireContext(), isChecked));

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

        // Scan buttons - open barcode scanner via MainActivity
        buttonScanOldPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_OLD_PASSWORD));
        buttonScanNewPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_NEW_PASSWORD));
        buttonScanHttpAdminPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_HTTP_ADMIN_PASSWORD));
        buttonScanAuthPassword.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_AUTH_PASSWORD));

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

        validatePasswords();

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
                        }
                    }
                });
    }
}
