package com.zebra.zebraprintereuredsetup;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class EURedFragment extends Fragment implements ToolbarConfigurable {

    private static final int MIN_NEW_PASSWORD_LENGTH = 14;
    private static final int MIN_HTTP_ADMIN_PASSWORD_LENGTH = 20;
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private TextInputLayout textInputLayoutMacAddress;
    private EditText editTextMacAddress;
    private MaterialButton buttonSetupPasswordBluetooth;
    private MaterialButton buttonTestLabel;
    private MaterialButton buttonResetPrinterSettings;
    private TextView textViewStatus;
    private boolean isFormattingMacAddress = false;

    private Spinner spinnerConnectivityType;
    private LinearLayout bleFieldsContainer;
    private View cardTest;
    private int currentConnectivityType = Constants.CONNECTIVITY_TYPE_USB;

    private FragmentContainerView fragmentContainer;
    private View mainContent;
    private MaterialButton buttonSetupScript;
    private View cardSetupScript;
    private View cardRestorePreEured;
    private View cardSetup;

    private PrinterHelper printerHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eured, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        printerHelper = new PrinterHelper();
        setupViews(view);
        setupBarcodeResultListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update visibility when returning from settings
        updateCardsVisibility();
    }

    private void updateCardsVisibility() {
        if (cardSetupScript != null) {
            boolean showEditCard = SettingsHelper.getAllowEditEuredScript(requireContext());
            cardSetupScript.setVisibility(showEditCard ? View.VISIBLE : View.GONE);
        }
        if (cardRestorePreEured != null) {
            boolean showRestoreCard = SettingsHelper.getShowRestorePreEured(requireContext());
            cardRestorePreEured.setVisibility(showRestoreCard ? View.VISIBLE : View.GONE);
        }
        if (cardSetup != null) {
            boolean showSendCard = SettingsHelper.getShowSendScriptCard(requireContext());
            cardSetup.setVisibility(showSendCard ? View.VISIBLE : View.GONE);
        }
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);

        textInputLayoutMacAddress = view.findViewById(R.id.textInputLayoutMacAddress);
        editTextMacAddress = view.findViewById(R.id.editTextMacAddress);
        buttonSetupPasswordBluetooth = view.findViewById(R.id.buttonSetupPasswordBluetooth);
        buttonTestLabel = view.findViewById(R.id.buttonTestLabel);
        buttonResetPrinterSettings = view.findViewById(R.id.buttonResetPrinterSettings);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        // Connectivity type spinner
        spinnerConnectivityType = view.findViewById(R.id.spinnerConnectivityType);
        bleFieldsContainer = view.findViewById(R.id.bleFieldsContainer);
        cardTest = view.findViewById(R.id.cardTest);
        buttonSetupScript = view.findViewById(R.id.buttonSetupScript);
        cardSetupScript = view.findViewById(R.id.cardSetupScript);
        cardRestorePreEured = view.findViewById(R.id.cardRestorePreEured);
        cardSetup = view.findViewById(R.id.cardSetup);

        // Setup Script button click listener - opens EURedSettingsFragment
        buttonSetupScript.setOnClickListener(v -> openEURedSettings());

        // Update card visibility based on settings
        updateCardsVisibility();

        // Setup connectivity type spinner
        String[] connectivityOptions = {
            getString(R.string.connectivity_bluetooth),
            getString(R.string.connectivity_usb)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, connectivityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConnectivityType.setAdapter(adapter);

        // Load saved connectivity type
        currentConnectivityType = SettingsHelper.getConnectivityType(requireContext());
        spinnerConnectivityType.setSelection(currentConnectivityType);
        updateBleFieldsVisibility();

        // Spinner selection listener
        spinnerConnectivityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                currentConnectivityType = position;
                SettingsHelper.saveConnectivityType(requireContext(), currentConnectivityType);
                updateBleFieldsVisibility();
                validateForm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        MaterialButton buttonScanBarcode = view.findViewById(R.id.buttonScanBarcode);

        // Load saved MAC address
        editTextMacAddress.setText(SettingsHelper.getBluetoothAddress(requireContext()));

        // Scan barcode button
        buttonScanBarcode.setOnClickListener(v -> openBarcodeScanner(BarcodeScannerFragment.FIELD_MAC_ADDRESS));

        // MAC address auto-formatting
        editTextMacAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormattingMacAddress) return;

                String input = s.toString();

                if (isValidMacAddress(input)) {
                    validateForm();
                    return;
                }

                String cleaned = input.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();

                if (cleaned.length() > 12) {
                    cleaned = cleaned.substring(0, 12);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < cleaned.length(); i++) {
                    if (i > 0 && i % 2 == 0) {
                        formatted.append(":");
                    }
                    formatted.append(cleaned.charAt(i));
                }

                isFormattingMacAddress = true;
                editTextMacAddress.setText(formatted.toString());
                editTextMacAddress.setSelection(formatted.length());
                isFormattingMacAddress = false;

                validateForm();
            }
        });

        // Setup button click listener
        buttonSetupPasswordBluetooth.setOnClickListener(v -> {
            // For BLE mode, validate MAC address
            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                if (!isValidMacAddress(macAddress)) {
                    textInputLayoutMacAddress.setError(getString(R.string.error_valid_mac_address_required));
                    return;
                }
            }

            // Get passwords from SettingsHelper (stored by SettingsFragment)
            String newPassword = SettingsHelper.getNewAdminpassword(requireContext());
            String httpAdminPassword = SettingsHelper.getHttpadminpasswordKey(requireContext());

            // Validate password lengths
            if (SettingsHelper.getChangePasswordEnabled(requireContext()) && newPassword.length() < MIN_NEW_PASSWORD_LENGTH) {
                setStatus(getString(R.string.error_min_characters_required, MIN_NEW_PASSWORD_LENGTH, MIN_NEW_PASSWORD_LENGTH - newPassword.length()),
                    requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }

            if (SettingsHelper.getHttpAdminEnabled(requireContext()) && httpAdminPassword.length() < MIN_HTTP_ADMIN_PASSWORD_LENGTH) {
                setStatus(getString(R.string.error_min_characters_required, MIN_HTTP_ADMIN_PASSWORD_LENGTH, MIN_HTTP_ADMIN_PASSWORD_LENGTH - httpAdminPassword.length()),
                    requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }

            // Show warning dialog before executing
            showEuredScriptWarningDialog();
        });

        // Test Label button click listener
        buttonTestLabel.setOnClickListener(v -> {
            // For BLE mode, validate MAC address
            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                if (!isValidMacAddress(macAddress)) {
                    textInputLayoutMacAddress.setError(getString(R.string.error_valid_mac_address_required));
                    return;
                }
            }

            String testZpl = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ";

            PrinterHelper.PrinterHelperCallback callback = new PrinterHelper.PrinterHelperCallback() {
                @Override
                public void OnStatus(String message, int color) {
                    requireActivity().runOnUiThread(() -> setStatus(message, color));
                }

                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        textViewStatus.setText(R.string.status_test_label_sent);
                        textViewStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                    });
                }
            };

            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                // Save MAC address
                SettingsHelper.saveBluetoothAddress(requireContext(), macAddress);
                printerHelper.sendDataToBT(requireContext(), testZpl, macAddress, callback);
            } else {
                // USB mode - use async wrapper to avoid blocking UI thread
                printerHelper.sendDataToUSB(requireContext(), testZpl, callback);
            }
        });

        // Reset Printer Settings button click listener
        buttonResetPrinterSettings.setOnClickListener(v -> {
            // For BLE mode, validate MAC address first
            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                if (!isValidMacAddress(macAddress)) {
                    textInputLayoutMacAddress.setError(getString(R.string.error_valid_mac_address_required));
                    return;
                }
            }

            // Show warning dialog
            showPreEuredWarningDialog();
        });

        validateForm();
    }

    private void updateBleFieldsVisibility() {
        if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
            bleFieldsContainer.setVisibility(View.VISIBLE);
            cardTest.setVisibility(View.VISIBLE);
        } else {
            bleFieldsContainer.setVisibility(View.GONE);
            cardTest.setVisibility(View.GONE);
        }
    }

    private void validateForm() {
        String macAddress = editTextMacAddress.getText().toString();
        boolean isMacAddressValid = isValidMacAddress(macAddress);

        // For USB mode, MAC address is not required
        boolean isFormValid = (currentConnectivityType == Constants.CONNECTIVITY_TYPE_USB) || isMacAddressValid;

        if (isMacAddressValid) {
            textInputLayoutMacAddress.setError(null);
        }

        int redColor = requireContext().getColor(android.R.color.holo_red_dark);
        int greenColor = requireContext().getColor(R.color.zebra_pantone_361);
        int strokeWidthDp = (int) (1 * getResources().getDisplayMetrics().density);
        int errorStrokeWidthDp = (int) (2 * getResources().getDisplayMetrics().density);

        // Setup button - depends on MAC validity for BLE, always enabled for USB
        buttonSetupPasswordBluetooth.setEnabled(isFormValid);
        if (isFormValid) {
            buttonSetupPasswordBluetooth.setStrokeColor(ColorStateList.valueOf(greenColor));
            buttonSetupPasswordBluetooth.setStrokeWidth(strokeWidthDp);
        } else {
            buttonSetupPasswordBluetooth.setStrokeColor(ColorStateList.valueOf(redColor));
            buttonSetupPasswordBluetooth.setStrokeWidth(errorStrokeWidthDp);
        }

        // Test Label button - only depends on MAC address validity for BLE
        buttonTestLabel.setEnabled(isFormValid);
        if (isFormValid) {
            buttonTestLabel.setStrokeColor(ColorStateList.valueOf(greenColor));
            buttonTestLabel.setStrokeWidth(strokeWidthDp);
        } else {
            buttonTestLabel.setStrokeColor(ColorStateList.valueOf(redColor));
            buttonTestLabel.setStrokeWidth(errorStrokeWidthDp);
        }
    }

    private boolean isValidMacAddress(String macAddress) {
        return MAC_ADDRESS_PATTERN.matcher(macAddress).matches();
    }

    private void showPreEuredWarningDialog() {
        String warningMessage = getString(R.string.warning_pre_eured_message) + "\n\n" +
                "• Admin Password: ZebraPassword1234\n" +
                "• Protected Mode: No\n" +
                "• device.allow_firmware_downloads: yes\n" +
                "• ip.tcp.enable: on\n" +
                "• ip.lpd.enable: on\n" +
                "• ip.https.enable: on\n" +
                "• ip.ftp.enable: on\n" +
                "• ip.snmp.enable: on\n" +
                "• wlan.enable: on\n" +
                "• usb.mirror.enable: on\n" +
                "• zbi.enable: on\n" +
                "• display.password.current: 1234\n" +
                "• display.password.level: none\n" +
                "• ip.http.enable: on\n" +
                "• ip.http.admin_password: 1234\n" +
                "• device.prompted_network_reset: yes";

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.warning_pre_eured_title)
                .setMessage(warningMessage)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_confirm, (dialogInterface, which) -> {
                    executePreEuredReset();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            // Style the confirm button with red outline and red text
            android.widget.Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (confirmButton != null) {
                confirmButton.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
            }
        });

        dialog.show();
    }

    private void executePreEuredReset() {
        PrinterHelper.PrinterHelperCallback callback = new PrinterHelper.PrinterHelperCallback() {
            @Override
            public void OnStatus(String message, int color) {
                requireActivity().runOnUiThread(() -> setStatus(message, color));
            }

            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    textViewStatus.setText(R.string.status_setup_completed);
                    textViewStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                });
            }
        };

        if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
            String macAddress = editTextMacAddress.getText().toString();
            // Save MAC address
            SettingsHelper.saveBluetoothAddress(requireContext(), macAddress);
            printerHelper.restorePreEUREDSettingsBLE(requireContext(), macAddress, callback);
        } else {
            // USB mode
            printerHelper.restorePreEURedSettingsUSB(requireContext(), callback);
        }
    }

    private void showEuredScriptWarningDialog() {
        StringBuilder warningMessage = new StringBuilder();
        warningMessage.append(getString(R.string.warning_eured_script_message)).append("\n");

        // Change Password settings
        if (SettingsHelper.getChangePasswordEnabled(requireContext())) {
            String oldPassword = SettingsHelper.getOldAdminpassword(requireContext());
            String newPassword = SettingsHelper.getNewAdminpassword(requireContext());
            warningMessage.append("\n• Old Admin Password: ").append(maskPassword(oldPassword));
            warningMessage.append("\n• New Admin Password: ").append(maskPassword(newPassword));
        }

        // HTTP Admin Password
        if (SettingsHelper.getHttpAdminEnabled(requireContext())) {
            String httpPassword = SettingsHelper.getHttpadminpasswordKey(requireContext());
            warningMessage.append("\n• HTTP Admin Password: ").append(maskPassword(httpPassword));
        }

        // Protected Mode
        boolean protectedMode = SettingsHelper.getProtectedModeAllowed(requireContext());
        warningMessage.append("\n• Protected Mode: ").append(protectedMode ? "Yes" : "No");

        // EURed Configuration settings
        warningMessage.append("\n• device.allow_firmware_downloads: ").append(SettingsHelper.getEuredFirmwareDownload(requireContext()) ? "yes" : "no");
        warningMessage.append("\n• ip.tcp.enable: ").append(SettingsHelper.getEuredTcpEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• ip.lpd.enable: ").append(SettingsHelper.getEuredLpdEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• ip.https.enable: ").append(SettingsHelper.getEuredHttpsEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• ip.ftp.enable: ").append(SettingsHelper.getEuredFtpEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• ip.snmp.enable: ").append(SettingsHelper.getEuredSnmpEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• wlan.enable: ").append(SettingsHelper.getEuredWlanEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• usb.mirror.enable: ").append(SettingsHelper.getEuredUsbMirrorEnable(requireContext()) ? "on" : "off");
        warningMessage.append("\n• zbi.enable: ").append(SettingsHelper.getEuredZbiEnable(requireContext()) ? "on" : "off");

        // Display Password Current
        if (SettingsHelper.getDisplayPasswordCurrentEnabled(requireContext())) {
            warningMessage.append("\n• display.password.current: ").append(maskPassword(SettingsHelper.getDisplayPasswordCurrent(requireContext())));
        }

        // Bluetooth Discoverable
        if (SettingsHelper.getBluetoothDiscoverableEnabled(requireContext())) {
            warningMessage.append("\n• bluetooth.discoverable: ").append(SettingsHelper.getBluetoothDiscoverable(requireContext()) ? "yes" : "no");
        }

        // Setvar Wlan Enable
        if (SettingsHelper.getSetvarWlanEnableEnabled(requireContext())) {
            warningMessage.append("\n• wlan.enable (setvar): ").append(SettingsHelper.getSetvarWlanEnable(requireContext()) ? "on" : "off");
        }

        // Setvar IP HTTP Enable
        if (SettingsHelper.getSetvarIpHttpEnableEnabled(requireContext())) {
            warningMessage.append("\n• ip.http.enable: ").append(SettingsHelper.getSetvarIpHttpEnable(requireContext()) ? "on" : "off");
        }

        // Display Password Level
        if (SettingsHelper.getDisplayPasswordLevelEnabled(requireContext())) {
            warningMessage.append("\n• display.password.level: ").append(SettingsHelper.getDisplayPasswordLevel(requireContext()));
        }

        // Device Prompted Network Reset
        if (SettingsHelper.getDevicePromptedNetworkResetEnabled(requireContext())) {
            warningMessage.append("\n• device.prompted_network_reset: ").append(SettingsHelper.getDevicePromptedNetworkReset(requireContext()) ? "yes" : "no");
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.warning_eured_script_title)
                .setMessage(warningMessage.toString())
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_confirm, (dialogInterface, which) -> {
                    executeEuredScript();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            // Style the confirm button with red text
            android.widget.Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (confirmButton != null) {
                confirmButton.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));
            }
        });

        dialog.show();
    }

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "(empty)";
        }
        return "****";
    }

    private void executeEuredScript() {
        PrinterHelper.PrinterHelperCallback callback = new PrinterHelper.PrinterHelperCallback() {
            @Override
            public void OnStatus(String message, int color) {
                requireActivity().runOnUiThread(() -> setStatus(message, color));
            }

            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    textViewStatus.setText(R.string.status_setup_completed);
                    textViewStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                });
            }
        };

        if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
            String macAddress = editTextMacAddress.getText().toString();
            // Save MAC address
            SettingsHelper.saveBluetoothAddress(requireContext(), macAddress);
            printerHelper.setupPasswordAndBluetooth(requireContext(), macAddress, callback);
        } else {
            // USB mode
            printerHelper.setupPasswordAndBluetoothUSB(requireContext(), callback);
        }
    }

    private void setStatus(String message, int color) {
        textViewStatus.setText(message);
        textViewStatus.setTextColor(color);
    }

    private void setupBarcodeResultListener() {
        getChildFragmentManager().setFragmentResultListener(
                BarcodeScannerFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String barcode = result.getString(BarcodeScannerFragment.RESULT_BARCODE);
                    int targetField = result.getInt(BarcodeScannerFragment.RESULT_TARGET_FIELD);

                    if (barcode != null && targetField == BarcodeScannerFragment.FIELD_MAC_ADDRESS) {
                        editTextMacAddress.setText(barcode);
                    }

                    showMainContent();
                });

        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                showMainContent();
            }
        });
    }

    private void showMainContent() {
        mainContent.setVisibility(View.VISIBLE);

        // Animate mainContent back in from the left
        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                fragmentContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mainContent.startAnimation(slideIn);
    }

    private void openBarcodeScanner(int targetField) {
        fragmentContainer.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);

        BarcodeScannerFragment scannerFragment = BarcodeScannerFragment.newInstance(targetField);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scannerFragment)
                .addToBackStack("scanner")
                .commit();
    }

    private void openEURedSettings() {
        fragmentContainer.setVisibility(View.VISIBLE);

        // Animate mainContent out to the left
        Animation slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mainContent.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mainContent.startAnimation(slideOut);

        EURedSettingsFragment settingsFragment = new EURedSettingsFragment();
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,   // enter
                        R.anim.slide_out_left,   // exit
                        R.anim.slide_in_left,    // popEnter
                        R.anim.slide_out_right   // popExit
                )
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack("eured_settings")
                .commit();

        // Update MainActivity toolbar for the child fragment
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setToolbarTitle(getString(settingsFragment.getToolbarTitleResId()));
        }
    }

    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            // Restore this fragment's toolbar title
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setToolbarTitle(getString(getToolbarTitleResId()));
            }
            return true;
        }
        return false;
    }

    // ToolbarConfigurable implementation
    @Override
    public int getToolbarTitleResId() {
        return R.string.launcher_eu_red_setup;
    }

    @Override
    public boolean showBackButton() {
        return true;
    }
}
