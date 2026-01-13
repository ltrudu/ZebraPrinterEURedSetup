package com.zebra.zebraprintereuredsetup;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class HomeFragment extends Fragment {

    private static final int MIN_NEW_PASSWORD_LENGTH = 14;
    private static final int MIN_HTTP_ADMIN_PASSWORD_LENGTH = 20;
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private TextInputLayout textInputLayoutMacAddress;
    private EditText editTextMacAddress;
    private MaterialButton buttonSetupPasswordBluetooth;
    private MaterialButton buttonTestLabel;
    private TextView textViewStatus;
    private boolean isFormattingMacAddress = false;

    private Spinner spinnerConnectivityType;
    private LinearLayout bleFieldsContainer;
    private View cardTest;
    private int currentConnectivityType = Constants.CONNECTIVITY_TYPE_USB;

    private FragmentContainerView fragmentContainer;
    private View mainContent;

    private PrinterHelper printerHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        printerHelper = new PrinterHelper();
        setupViews(view);
        setupBarcodeResultListener();
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);

        textInputLayoutMacAddress = view.findViewById(R.id.textInputLayoutMacAddress);
        editTextMacAddress = view.findViewById(R.id.editTextMacAddress);
        buttonSetupPasswordBluetooth = view.findViewById(R.id.buttonSetupPasswordBluetooth);
        buttonTestLabel = view.findViewById(R.id.buttonTestLabel);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        // Connectivity type spinner
        spinnerConnectivityType = view.findViewById(R.id.spinnerConnectivityType);
        bleFieldsContainer = view.findViewById(R.id.bleFieldsContainer);
        cardTest = view.findViewById(R.id.cardTest);

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
            String oldPassword = SettingsHelper.getOldAdminpassword(requireContext());
            String newPassword = SettingsHelper.getNewAdminpassword(requireContext());
            String httpAdminPassword = SettingsHelper.getHttpadminpasswordKey(requireContext());

            // Validate password lengths
            if (newPassword.length() < MIN_NEW_PASSWORD_LENGTH) {
                setStatus(getString(R.string.error_min_characters_required, MIN_NEW_PASSWORD_LENGTH, MIN_NEW_PASSWORD_LENGTH - newPassword.length()),
                    requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }

            if (httpAdminPassword.length() < MIN_HTTP_ADMIN_PASSWORD_LENGTH) {
                setStatus(getString(R.string.error_min_characters_required, MIN_HTTP_ADMIN_PASSWORD_LENGTH, MIN_HTTP_ADMIN_PASSWORD_LENGTH - httpAdminPassword.length()),
                    requireContext().getColor(android.R.color.holo_red_dark));
                return;
            }

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

        // Setup button - depends on MAC validity for BLE, always enabled for USB
        buttonSetupPasswordBluetooth.setEnabled(isFormValid);
        if (isFormValid) {
            buttonSetupPasswordBluetooth.setStrokeWidth(0);
        } else {
            buttonSetupPasswordBluetooth.setStrokeColor(ColorStateList.valueOf(redColor));
            buttonSetupPasswordBluetooth.setStrokeWidth(4);
        }

        // Test Label button - only depends on MAC address validity for BLE
        buttonTestLabel.setEnabled(isFormValid);
        if (isFormValid) {
            buttonTestLabel.setStrokeWidth(0);
        } else {
            buttonTestLabel.setStrokeColor(ColorStateList.valueOf(redColor));
            buttonTestLabel.setStrokeWidth(4);
        }
    }

    private boolean isValidMacAddress(String macAddress) {
        return MAC_ADDRESS_PATTERN.matcher(macAddress).matches();
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
        fragmentContainer.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
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

    public boolean handleBackPress() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }
}
