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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

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

    // Suggestions settings
    private MaterialCheckBox checkBoxUnlimitedSuggestions;
    private TextInputLayout textInputLayoutMaxSuggestions;
    private AutoCompleteTextView autoCompleteMaxSuggestions;
    private static final String[] MAX_SUGGESTIONS_VALUES = {"5", "10", "15", "20"};

    // EURed Config settings
    private MaterialCheckBox checkBoxShowEditEuredScriptCard;
    private MaterialCheckBox checkBoxShowRestorePreEuredCard;
    private MaterialCheckBox checkBoxShowSendScriptCard;
    private MaterialButton buttonOpenScriptSettings;

    // Edit Mode settings
    private MaterialCheckBox checkBoxEditModeEnabled;

    // File operations
    private MaterialCheckBox checkBoxEmbedEuredScript;
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

        // Edit Mode settings
        checkBoxEditModeEnabled = view.findViewById(R.id.checkBoxEditModeEnabled);
        setupEditModeSettings();

        // Suggestions settings
        checkBoxUnlimitedSuggestions = view.findViewById(R.id.checkBoxUnlimitedSuggestions);
        textInputLayoutMaxSuggestions = view.findViewById(R.id.textInputLayoutMaxSuggestions);
        autoCompleteMaxSuggestions = view.findViewById(R.id.autoCompleteMaxSuggestions);
        setupSuggestionsSettings();

        // EURed Config settings
        checkBoxShowEditEuredScriptCard = view.findViewById(R.id.checkBoxShowEditEuredScriptCard);
        checkBoxShowRestorePreEuredCard = view.findViewById(R.id.checkBoxShowRestorePreEuredCard);
        checkBoxShowSendScriptCard = view.findViewById(R.id.checkBoxShowSendScriptCard);
        buttonOpenScriptSettings = view.findViewById(R.id.buttonOpenScriptSettings);
        setupEuredConfigSettings();

        // File operations
        checkBoxEmbedEuredScript = view.findViewById(R.id.checkBoxEmbedEuredScript);
        buttonImportSettings = view.findViewById(R.id.buttonImportSettings);
        buttonExportSettings = view.findViewById(R.id.buttonExportSettings);
        setupFileOperations();
    }

    private void setupFileOperations() {
        // Load saved embed setting
        boolean embedEured = SettingsHelper.getEmbedEuredScript(requireContext());
        checkBoxEmbedEuredScript.setChecked(embedEured);

        // Handle checkbox changes
        checkBoxEmbedEuredScript.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveEmbedEuredScript(requireContext(), isChecked);
        });

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

    private void setupEditModeSettings() {
        // Load saved setting
        boolean editModeEnabled = SettingsHelper.getEditModeEnabled(requireContext());
        checkBoxEditModeEnabled.setChecked(editModeEnabled);

        // Handle checkbox changes
        checkBoxEditModeEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveEditModeEnabled(requireContext(), isChecked);
        });
    }

    private void setupSuggestionsSettings() {
        // Setup dropdown adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                MAX_SUGGESTIONS_VALUES
        ) {
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = MAX_SUGGESTIONS_VALUES;
                        results.count = MAX_SUGGESTIONS_VALUES.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        autoCompleteMaxSuggestions.setAdapter(adapter);

        // Load saved settings
        boolean isUnlimited = SettingsHelper.getSuggestionsUnlimited(requireContext());
        int maxSuggestions = SettingsHelper.getMaxSuggestions(requireContext());

        checkBoxUnlimitedSuggestions.setChecked(isUnlimited);
        textInputLayoutMaxSuggestions.setVisibility(isUnlimited ? View.GONE : View.VISIBLE);

        // Set current dropdown selection
        String currentValue = String.valueOf(maxSuggestions);
        autoCompleteMaxSuggestions.setText(currentValue, false);

        // Handle checkbox changes
        checkBoxUnlimitedSuggestions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveSuggestionsUnlimited(requireContext(), isChecked);
            textInputLayoutMaxSuggestions.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // Handle dropdown selection
        autoCompleteMaxSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            int selectedValue = Integer.parseInt(MAX_SUGGESTIONS_VALUES[position]);
            SettingsHelper.saveMaxSuggestions(requireContext(), selectedValue);
        });
    }

    private void setupEuredConfigSettings() {
        // Load saved settings
        boolean showEditCard = SettingsHelper.getAllowEditEuredScript(requireContext());
        checkBoxShowEditEuredScriptCard.setChecked(showEditCard);

        boolean showRestoreCard = SettingsHelper.getShowRestorePreEured(requireContext());
        checkBoxShowRestorePreEuredCard.setChecked(showRestoreCard);

        boolean showSendCard = SettingsHelper.getShowSendScriptCard(requireContext());
        checkBoxShowSendScriptCard.setChecked(showSendCard);

        // Handle checkbox changes
        checkBoxShowEditEuredScriptCard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveAllowEditEuredScript(requireContext(), isChecked);
        });

        checkBoxShowRestorePreEuredCard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveShowRestorePreEured(requireContext(), isChecked);
        });

        checkBoxShowSendScriptCard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.saveShowSendScriptCard(requireContext(), isChecked);
        });

        // Open Script Settings button
        buttonOpenScriptSettings.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openEURedSettingsFragment();
            }
        });
    }

    private void openImportDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/json", "text/plain", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Use last folder or default to Documents
        String lastUri = SettingsHelper.getLastImportFolderUri(requireContext());
        Uri initialUri;
        if (lastUri != null) {
            initialUri = Uri.parse(lastUri);
        } else {
            initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        }
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

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

        // Use last folder or default to Documents
        String lastUri = SettingsHelper.getLastExportFolderUri(requireContext());
        Uri initialUri;
        if (lastUri != null) {
            initialUri = Uri.parse(lastUri);
        } else {
            initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        }
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

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
                SettingsHelper.applySettingsJSON(requireContext(), json);

                // Update UI from saved settings
                updateUIFromSettings();

                Toast.makeText(requireContext(), R.string.status_settings_imported, Toast.LENGTH_SHORT).show();
                setStatus(getString(R.string.status_settings_imported), requireContext().getColor(android.R.color.holo_green_dark));

                // Save the folder URI for next time
                SettingsHelper.saveLastImportFolderUri(requireContext(), uri.toString());

                // Recreate activity to apply all imported settings (for language changes)
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_import_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            setStatus(getString(R.string.error_import_settings_failed, e.getMessage()), requireContext().getColor(android.R.color.holo_red_dark));
        }
    }

    private void updateUIFromSettings() {
        // Language dropdown
        String currentLanguage = SettingsHelper.getLanguage(requireContext());
        int currentIndex = LocaleHelper.getLanguageIndex(currentLanguage);
        String[] languageNames = LocaleHelper.getLanguageDisplayNames(requireContext());
        autoCompleteLanguage.setText(languageNames[currentIndex], false);

        // Edit Mode settings
        checkBoxEditModeEnabled.setChecked(SettingsHelper.getEditModeEnabled(requireContext()));

        // Suggestions settings
        boolean isUnlimited = SettingsHelper.getSuggestionsUnlimited(requireContext());
        int maxSuggestions = SettingsHelper.getMaxSuggestions(requireContext());
        checkBoxUnlimitedSuggestions.setChecked(isUnlimited);
        textInputLayoutMaxSuggestions.setVisibility(isUnlimited ? View.GONE : View.VISIBLE);
        autoCompleteMaxSuggestions.setText(String.valueOf(maxSuggestions), false);

        // EURed Config settings
        checkBoxShowEditEuredScriptCard.setChecked(SettingsHelper.getAllowEditEuredScript(requireContext()));
        checkBoxShowRestorePreEuredCard.setChecked(SettingsHelper.getShowRestorePreEured(requireContext()));
        checkBoxShowSendScriptCard.setChecked(SettingsHelper.getShowSendScriptCard(requireContext()));

        // Embed EURED Script setting
        checkBoxEmbedEuredScript.setChecked(SettingsHelper.getEmbedEuredScript(requireContext()));
    }

    private void exportSettingsToUri(Uri uri) {
        try {
            boolean embedEured = SettingsHelper.getEmbedEuredScript(requireContext());
            JSONObject json = SettingsHelper.getSettingsJSON(requireContext(), embedEured);
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(json.toString(2).getBytes());
                outputStream.close();
                Toast.makeText(requireContext(), R.string.status_settings_exported, Toast.LENGTH_SHORT).show();
                setStatus(getString(R.string.status_settings_exported), requireContext().getColor(android.R.color.holo_green_dark));

                // Save the folder URI for next time
                SettingsHelper.saveLastExportFolderUri(requireContext(), uri.toString());
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_export_settings_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            setStatus(getString(R.string.error_export_settings_failed, e.getMessage()), requireContext().getColor(android.R.color.holo_red_dark));
        }
    }
}
