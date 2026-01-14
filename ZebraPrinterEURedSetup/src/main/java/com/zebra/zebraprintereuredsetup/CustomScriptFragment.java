package com.zebra.zebraprintereuredsetup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class CustomScriptFragment extends Fragment {

    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private TextInputLayout textInputLayoutMacAddress;
    private EditText editTextMacAddress;
    private TextInputLayout textInputLayoutScript;
    private EditText editTextScript;
    private MaterialButton buttonSendScript;
    private TextView textViewStatus;
    private boolean isFormattingMacAddress = false;

    private Spinner spinnerConnectivityType;
    private LinearLayout bleFieldsContainer;
    private int currentConnectivityType = Constants.CONNECTIVITY_TYPE_USB;

    private FragmentContainerView fragmentContainer;
    private View mainContent;

    private MaterialButton buttonImportScript;
    private MaterialButton buttonExportScript;

    private PrinterHelper printerHelper;

    private ActivityResultLauncher<Intent> importScriptLauncher;
    private ActivityResultLauncher<Intent> exportScriptLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_script, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        printerHelper = new PrinterHelper();
        setupViews(view);
        setupBarcodeResultListener();
    }

    private void setupActivityResultLaunchers() {
        // Import script launcher
        importScriptLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importScriptFromUri(uri);
                    }
                }
            }
        );

        // Export script launcher
        exportScriptLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportScriptToUri(uri);
                    }
                }
            }
        );
    }

    private void setupViews(View view) {
        fragmentContainer = view.findViewById(R.id.fragment_container);
        mainContent = view.findViewById(R.id.mainContent);

        textInputLayoutMacAddress = view.findViewById(R.id.textInputLayoutMacAddress);
        editTextMacAddress = view.findViewById(R.id.editTextMacAddress);
        textInputLayoutScript = view.findViewById(R.id.textInputLayoutScript);
        editTextScript = view.findViewById(R.id.editTextScript);
        buttonSendScript = view.findViewById(R.id.buttonSendScript);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        // Connectivity type spinner
        spinnerConnectivityType = view.findViewById(R.id.spinnerConnectivityType);
        bleFieldsContainer = view.findViewById(R.id.bleFieldsContainer);

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

        // Script text change listener
        editTextScript.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        });

        // Import/Export buttons
        buttonImportScript = view.findViewById(R.id.buttonImportScript);
        buttonExportScript = view.findViewById(R.id.buttonExportScript);

        buttonImportScript.setOnClickListener(v -> openImportDialog());
        buttonExportScript.setOnClickListener(v -> openExportDialog());

        // Send Script button click listener
        buttonSendScript.setOnClickListener(v -> {
            String script = editTextScript.getText().toString();
            if (script.isEmpty()) {
                textInputLayoutScript.setError(getString(R.string.error_script_required));
                return;
            }

            // For BLE mode, validate MAC address
            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                if (!isValidMacAddress(macAddress)) {
                    textInputLayoutMacAddress.setError(getString(R.string.error_valid_mac_address_required));
                    return;
                }
            }

            PrinterHelper.PrinterHelperCallback callback = new PrinterHelper.PrinterHelperCallback() {
                @Override
                public void OnStatus(String message, int color) {
                    requireActivity().runOnUiThread(() -> setStatus(message, color));
                }

                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        textViewStatus.setText(R.string.status_script_sent);
                        textViewStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));
                    });
                }
            };

            if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
                String macAddress = editTextMacAddress.getText().toString();
                // Save MAC address
                SettingsHelper.saveBluetoothAddress(requireContext(), macAddress);
                printerHelper.sendDataToBT(requireContext(), script, macAddress, callback);
            } else {
                // USB mode
                printerHelper.sendDataToUSB(requireContext(), script, callback);
            }
        });

        validateForm();
    }

    private void updateBleFieldsVisibility() {
        if (currentConnectivityType == Constants.CONNECTIVITY_TYPE_BLE) {
            bleFieldsContainer.setVisibility(View.VISIBLE);
        } else {
            bleFieldsContainer.setVisibility(View.GONE);
        }
    }

    private void validateForm() {
        String macAddress = editTextMacAddress.getText().toString();
        String script = editTextScript.getText().toString();
        boolean isMacAddressValid = isValidMacAddress(macAddress);
        boolean isScriptValid = !script.isEmpty();

        // For USB mode, MAC address is not required
        boolean isFormValid = isScriptValid &&
            ((currentConnectivityType == Constants.CONNECTIVITY_TYPE_USB) || isMacAddressValid);

        if (isMacAddressValid) {
            textInputLayoutMacAddress.setError(null);
        }

        if (isScriptValid) {
            textInputLayoutScript.setError(null);
        }

        int redColor = requireContext().getColor(android.R.color.holo_red_dark);

        // Send Script button
        buttonSendScript.setEnabled(isFormValid);
        if (isFormValid) {
            buttonSendScript.setStrokeWidth(0);
        } else {
            buttonSendScript.setStrokeColor(ColorStateList.valueOf(redColor));
            buttonSendScript.setStrokeWidth(4);
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

    private void openImportDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        // Try to open in Downloads folder
        Uri downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);

        importScriptLauncher.launch(intent);
    }

    private void openExportDialog() {
        String script = editTextScript.getText().toString();
        if (script.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_script_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "script_" + timestamp + ".txt";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        // Try to open in Downloads folder
        Uri downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);

        exportScriptLauncher.launch(intent);
    }

    private void importScriptFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                editTextScript.setText(stringBuilder.toString());
                Toast.makeText(requireContext(), R.string.status_script_imported, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_import_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void exportScriptToUri(Uri uri) {
        try {
            String script = editTextScript.getText().toString();
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(script.getBytes());
                outputStream.close();
                Toast.makeText(requireContext(), R.string.status_script_exported, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_export_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
