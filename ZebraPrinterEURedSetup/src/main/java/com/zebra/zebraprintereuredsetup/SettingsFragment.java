package com.zebra.zebraprintereuredsetup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView textViewStatus;
    private AutoCompleteTextView autoCompleteLanguage;

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
        textViewStatus = view.findViewById(R.id.textViewStatus);
        autoCompleteLanguage = view.findViewById(R.id.autoCompleteLanguage);

        // Setup language dropdown
        setupLanguageDropdown();

        // File operations buttons
        buttonImportSettings = view.findViewById(R.id.buttonImportSettings);
        buttonExportSettings = view.findViewById(R.id.buttonExportSettings);

        buttonImportSettings.setOnClickListener(v -> openImportDialog());
        buttonExportSettings.setOnClickListener(v -> openExportDialog());
    }

    private void setStatus(String message, int color) {
        if (textViewStatus != null) {
            textViewStatus.setText(message);
            textViewStatus.setTextColor(color);
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
                setStatus(getString(R.string.status_settings_imported), requireContext().getColor(android.R.color.holo_green_dark));

                // Recreate activity to apply all imported settings
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_import_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            setStatus(getString(R.string.error_import_settings_failed, e.getMessage()), requireContext().getColor(android.R.color.holo_red_dark));
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
                setStatus(getString(R.string.status_settings_exported), requireContext().getColor(android.R.color.holo_green_dark));
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_export_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            setStatus(getString(R.string.error_export_settings_failed, e.getMessage()), requireContext().getColor(android.R.color.holo_red_dark));
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
