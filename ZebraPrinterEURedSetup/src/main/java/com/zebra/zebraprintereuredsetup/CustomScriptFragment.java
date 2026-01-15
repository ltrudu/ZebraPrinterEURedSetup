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
import android.view.ViewParent;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;

import org.json.JSONArray;
import org.json.JSONObject;

public class CustomScriptFragment extends Fragment {

    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    private TextInputLayout textInputLayoutMacAddress;
    private EditText editTextMacAddress;
    private TextInputLayout textInputLayoutScript;
    private EditText editTextScript;
    private MaterialButton buttonSendScript;
    private MaterialCheckBox checkBoxShowDocumentation;
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

    // Autocomplete suggestions
    private MaterialCardView cardSuggestions;
    private RecyclerView recyclerViewSuggestions;
    private CommandSuggestionAdapter suggestionAdapter;
    private final Handler suggestionHandler = new Handler(Looper.getMainLooper());
    private Runnable suggestionRunnable;
    private static final long SUGGESTION_DEBOUNCE_MS = 200;
    private static final int MIN_CHARS_FOR_SUGGESTION = 2;
    private boolean isInsertingSuggestion = false;

    // Syntax highlighting
    private ScriptAnalyzer scriptAnalyzer;
    private ScriptHighlighter scriptHighlighter;
    private final Handler highlightHandler = new Handler(Looper.getMainLooper());
    private Runnable highlightRunnable;
    private static final long HIGHLIGHT_DEBOUNCE_MS = 300; // Faster response time
    private boolean isHighlightingEnabled = true;
    private boolean isUpdatingFromHighlight = false;

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
        setupAutoComplete(view);
        loadSuggestions();
        setupSyntaxHighlighting();
        loadDocumentationForHighlighting();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        suggestionHandler.removeCallbacksAndMessages(null);
        highlightHandler.removeCallbacksAndMessages(null);
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

        // Setup toolbar with back navigation
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarCustomScript);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        textInputLayoutMacAddress = view.findViewById(R.id.textInputLayoutMacAddress);
        editTextMacAddress = view.findViewById(R.id.editTextMacAddress);
        textInputLayoutScript = view.findViewById(R.id.textInputLayoutScript);
        editTextScript = view.findViewById(R.id.editTextScript);
        buttonSendScript = view.findViewById(R.id.buttonSendScript);
        checkBoxShowDocumentation = view.findViewById(R.id.checkBoxShowDocumentation);

        // Enable vertical scrolling inside the fixed-size EditText
        editTextScript.setMaxLines(Integer.MAX_VALUE);
        editTextScript.setVerticalScrollBarEnabled(true);
        editTextScript.setNestedScrollingEnabled(true);
        editTextScript.setOnTouchListener((v, event) -> {
            // Prevent all parent views from intercepting touch events while scrolling
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                parent = parent.getParent();
            }
            return false;
        });
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

        // Use last folder or default to Documents
        String lastUri = SettingsHelper.getLastImportFolderUri(requireContext());
        Uri initialUri;
        if (lastUri != null) {
            initialUri = Uri.parse(lastUri);
        } else {
            initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        }
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

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

        // Use last folder or default to Documents
        String lastUri = SettingsHelper.getLastExportFolderUri(requireContext());
        Uri initialUri;
        if (lastUri != null) {
            initialUri = Uri.parse(lastUri);
        } else {
            initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Documents");
        }
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

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

                // Save the folder URI for next time
                Uri parentUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri));
                SettingsHelper.saveLastImportFolderUri(requireContext(), uri.toString());
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

                // Save the folder URI for next time
                SettingsHelper.saveLastExportFolderUri(requireContext(), uri.toString());
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.error_export_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    // ===== Autocomplete Methods =====

    private void setupAutoComplete(View view) {
        cardSuggestions = view.findViewById(R.id.cardSuggestions);
        recyclerViewSuggestions = view.findViewById(R.id.recyclerViewSuggestions);

        suggestionAdapter = new CommandSuggestionAdapter(requireContext());
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        // Handle suggestion click
        suggestionAdapter.setOnSuggestionClickListener(suggestion -> {
            insertSuggestion(suggestion);
            hideSuggestions();
        });

        // Add text watcher with debounce for suggestions and highlighting
        editTextScript.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isInsertingSuggestion || isUpdatingFromHighlight) return;
                debounceSuggestion();
                debounceHighlighting();
            }
        });

        // Enable clickable spans on EditText
        editTextScript.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void loadSuggestions() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                InputStream is = requireContext().getAssets().open("zebra_documentation.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();

                List<CommandSuggestion> suggestions = CommandSuggestion.parseFromJson(sb.toString());

                requireActivity().runOnUiThread(() -> {
                    suggestionAdapter.setSuggestions(suggestions);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void debounceSuggestion() {
        if (suggestionRunnable != null) {
            suggestionHandler.removeCallbacks(suggestionRunnable);
        }
        suggestionRunnable = this::updateSuggestions;
        suggestionHandler.postDelayed(suggestionRunnable, SUGGESTION_DEBOUNCE_MS);
    }

    private void updateSuggestions() {
        String currentWord = getCurrentWord();
        String lineContext = getCurrentLineContext();

        // Special handling for SGD command building
        if (currentWord.equals("!")) {
            // User typed "!", suggest "! U1"
            suggestionAdapter.setSpecialSuggestions(createSgdPrefixSuggestions());
            showSuggestions();
            return;
        }

        // Check if we're after "! U1" or "!U1"
        String trimmedContext = lineContext.trim().toLowerCase();
        if (trimmedContext.matches("!\\s*u1\\s*$") || currentWord.equalsIgnoreCase("u1")) {
            // User typed "! U1", suggest setvar/getvar/do
            suggestionAdapter.setSpecialSuggestions(createSgdActionSuggestions());
            showSuggestions();
            return;
        }

        // Check if we're inside quotes after getvar/setvar/do - suggest SGD commands
        if (trimmedContext.matches("!\\s*u1\\s+(getvar|setvar|do)\\s+\"[^\"]*$")) {
            // Filter SGD commands only
            if (currentWord.length() >= MIN_CHARS_FOR_SUGGESTION) {
                suggestionAdapter.filterSgdOnly(currentWord);
                if (suggestionAdapter.hasSuggestions()) {
                    showSuggestions();
                } else {
                    hideSuggestions();
                }
                return;
            }
        }

        // Normal suggestion filtering
        if (currentWord.length() >= MIN_CHARS_FOR_SUGGESTION) {
            suggestionAdapter.filter(currentWord);
            if (suggestionAdapter.hasSuggestions()) {
                showSuggestions();
            } else {
                hideSuggestions();
            }
        } else {
            hideSuggestions();
        }
    }

    private String getCurrentLineContext() {
        int cursorPos = editTextScript.getSelectionStart();
        String text = editTextScript.getText().toString();

        // Handle invalid cursor position or empty text
        if (cursorPos <= 0 || text.isEmpty() || cursorPos > text.length()) {
            return "";
        }

        // Find the start of the current line
        int lineStart = cursorPos - 1;
        while (lineStart >= 0 && text.charAt(lineStart) != '\n') {
            lineStart--;
        }
        lineStart++;

        // Safety check for bounds
        if (lineStart < 0 || lineStart > cursorPos || cursorPos > text.length()) {
            return "";
        }

        return text.substring(lineStart, cursorPos);
    }

    private java.util.List<CommandSuggestion> createSgdPrefixSuggestions() {
        java.util.List<CommandSuggestion> suggestions = new java.util.ArrayList<>();
        suggestions.add(new CommandSuggestion("! U1", "SGD Command Prefix", "! U1 ", "SGD"));
        return suggestions;
    }

    private java.util.List<CommandSuggestion> createSgdActionSuggestions() {
        java.util.List<CommandSuggestion> suggestions = new java.util.ArrayList<>();
        suggestions.add(new CommandSuggestion("setvar", "Set variable value", "setvar \"", "SGD"));
        suggestions.add(new CommandSuggestion("getvar", "Get variable value", "getvar \"", "SGD"));
        suggestions.add(new CommandSuggestion("do", "Execute action", "do \"", "SGD"));
        return suggestions;
    }

    private String getCurrentWord() {
        int cursorPos = editTextScript.getSelectionStart();
        String text = editTextScript.getText().toString();

        // Handle invalid cursor position or empty text
        if (cursorPos <= 0 || text.isEmpty() || cursorPos > text.length()) {
            return "";
        }

        // Find the start of the current word
        // Stop at whitespace, newline, or opening quote (for SGD commands)
        int wordStart = cursorPos - 1;
        while (wordStart >= 0) {
            char c = text.charAt(wordStart);
            if (Character.isWhitespace(c) || c == '\n' || c == '"') {
                break;
            }
            wordStart--;
        }
        wordStart++;

        // Safety check for bounds
        if (wordStart < 0 || wordStart > cursorPos || cursorPos > text.length()) {
            return "";
        }

        // Extract the current word
        if (wordStart < cursorPos) {
            return text.substring(wordStart, cursorPos);
        }
        return "";
    }

    private void insertSuggestion(CommandSuggestion suggestion) {
        isInsertingSuggestion = true;

        int cursorPos = editTextScript.getSelectionStart();
        String text = editTextScript.getText().toString();
        String insertText = suggestion.getInsertText();
        String lineContext = getCurrentLineContext();

        // Special case: inserting SGD action (setvar/getvar/do) after "! U1"
        String trimmedContext = lineContext.trim().toLowerCase();
        if ((suggestion.getCommand().equals("setvar") ||
             suggestion.getCommand().equals("getvar") ||
             suggestion.getCommand().equals("do")) &&
            trimmedContext.matches("!\\s*u1\\s*$")) {
            // Just append the action, don't replace anything
            String newText = text.substring(0, cursorPos) + insertText + text.substring(cursorPos);
            editTextScript.setText(newText);
            editTextScript.setSelection(cursorPos + insertText.length());
            isInsertingSuggestion = false;
            return;
        }

        // Special case: inserting SGD command inside quotes after getvar/setvar/do
        if (trimmedContext.matches("!\\s*u1\\s+(getvar|setvar|do)\\s+\"[^\"]*$") &&
            "SGD".equalsIgnoreCase(suggestion.getType())) {
            // Progressive SGD suggestion - just append the next segment
            // insertText contains only the new part to add (e.g., ".11ac." or ".enable")
            String segmentToAdd = insertText;

            // Check if this is the final segment (no more sub-paths)
            // If name is "..." there are more segments, otherwise it's the final one
            boolean isFinalSegment = !"...".equals(suggestion.getName());

            String newText;
            int newCursorPos;

            if (isFinalSegment) {
                // Final segment - add closing quote
                newText = text.substring(0, cursorPos) + segmentToAdd + "\"" + text.substring(cursorPos);
                newCursorPos = cursorPos + segmentToAdd.length() + 1;
            } else {
                // More segments to come - don't add closing quote
                newText = text.substring(0, cursorPos) + segmentToAdd + text.substring(cursorPos);
                newCursorPos = cursorPos + segmentToAdd.length();
            }

            editTextScript.setText(newText);
            editTextScript.setSelection(newCursorPos);
            isInsertingSuggestion = false;

            // If there are more segments, show suggestions again
            if (!isFinalSegment) {
                suggestionHandler.postDelayed(this::updateSuggestions, 50);
            }
            return;
        }

        // For SGD hierarchical suggestions, append at cursor instead of replacing
        // This applies when:
        // 1. insertText starts with "." (first level: "wlan" -> ".11n.")
        // 2. OR insertText doesn't contain spaces and doesn't start with "!" (subsequent levels: "wlan.11n." -> "20mhz_only")
        if ("SGD".equalsIgnoreCase(suggestion.getType()) &&
            !insertText.startsWith("!") &&
            !insertText.contains(" ")) {
            String newText = text.substring(0, cursorPos) + insertText + text.substring(cursorPos);
            editTextScript.setText(newText);
            editTextScript.setSelection(cursorPos + insertText.length());
            isInsertingSuggestion = false;

            // If there are more segments (insertText ends with "." or name is "..."), show suggestions again
            if (insertText.endsWith(".") || "...".equals(suggestion.getName())) {
                // Small delay to let the text update complete
                suggestionHandler.postDelayed(this::updateSuggestions, 50);
            }
            return;
        }

        // Handle snippets with cursor positioning
        if (suggestion.isSnippet()) {
            // Find the start of the current word
            int wordStart = cursorPos - 1;
            while (wordStart >= 0) {
                char c = text.charAt(wordStart);
                if (Character.isWhitespace(c) || c == '\n' || c == '"') {
                    break;
                }
                wordStart--;
            }
            wordStart++;

            // Insert snippet and position cursor at the designated position
            String newText = text.substring(0, wordStart) + insertText + text.substring(cursorPos);
            editTextScript.setText(newText);
            // Position cursor at the snippet's cursor offset
            int cursorOffset = suggestion.getCursorOffset();
            editTextScript.setSelection(wordStart + cursorOffset);
            isInsertingSuggestion = false;
            return;
        }

        // Find the start of the current word
        int wordStart = cursorPos - 1;
        while (wordStart >= 0) {
            char c = text.charAt(wordStart);
            if (Character.isWhitespace(c) || c == '\n' || c == '"') {
                break;
            }
            wordStart--;
        }
        wordStart++;

        // Replace the current word with the suggestion
        String newText = text.substring(0, wordStart) + insertText + text.substring(cursorPos);
        editTextScript.setText(newText);

        // Move cursor to end of inserted text
        editTextScript.setSelection(wordStart + insertText.length());

        isInsertingSuggestion = false;
    }

    private void showSuggestions() {
        if (cardSuggestions != null) {
            cardSuggestions.setVisibility(View.VISIBLE);
        }
    }

    private void hideSuggestions() {
        if (cardSuggestions != null) {
            cardSuggestions.setVisibility(View.GONE);
        }
    }

    // ===== Syntax Highlighting Methods =====

    private void setupSyntaxHighlighting() {
        scriptAnalyzer = new ScriptAnalyzer();
        scriptHighlighter = new ScriptHighlighter(requireContext());
        scriptHighlighter.setScriptAnalyzer(scriptAnalyzer);

        scriptHighlighter.setOnCommandClickListener((foundCommand, documentation) -> {
            // Only show documentation if checkbox is checked
            if (checkBoxShowDocumentation == null || !checkBoxShowDocumentation.isChecked()) {
                return;
            }

            // Show documentation bottom sheet
            if (documentation != null) {
                CommandDocumentationBottomSheet bottomSheet =
                        CommandDocumentationBottomSheet.newInstance(documentation);
                bottomSheet.show(getChildFragmentManager(), "command_doc");
            } else {
                // Show a basic sheet for commands not in documentation
                CommandDocumentationBottomSheet bottomSheet =
                        CommandDocumentationBottomSheet.newInstance(
                                foundCommand.commandText,
                                foundCommand.type.name());
                bottomSheet.show(getChildFragmentManager(), "command_doc");
            }
        });
    }

    private void loadDocumentationForHighlighting() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                InputStream is = requireContext().getAssets().open("zebra_documentation.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();

                // Parse documentation commands
                java.util.List<DocumentationCommand> commands = new java.util.ArrayList<>();
                JSONObject jsonObject = new JSONObject(sb.toString());

                // Parse ZPL commands
                JSONArray zplArray = jsonObject.optJSONArray("zpl_commands");
                if (zplArray != null) {
                    for (int i = 0; i < zplArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zplArray.getJSONObject(i)));
                    }
                }

                // Parse ZBI commands
                JSONArray zbiArray = jsonObject.optJSONArray("zbi_commands");
                if (zbiArray != null) {
                    for (int i = 0; i < zbiArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zbiArray.getJSONObject(i)));
                    }
                }

                // Parse SGD commands
                JSONArray sgdArray = jsonObject.optJSONArray("sgd_commands");
                if (sgdArray != null) {
                    for (int i = 0; i < sgdArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(sgdArray.getJSONObject(i)));
                    }
                }

                final java.util.List<DocumentationCommand> finalCommands = commands;
                requireActivity().runOnUiThread(() -> {
                    scriptAnalyzer.setCommandDatabase(finalCommands);
                    // Perform initial highlighting if there's text
                    if (editTextScript.getText().length() > 0) {
                        performHighlighting();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void debounceHighlighting() {
        if (!isHighlightingEnabled) return;

        if (highlightRunnable != null) {
            highlightHandler.removeCallbacks(highlightRunnable);
        }
        highlightRunnable = this::performHighlighting;
        highlightHandler.postDelayed(highlightRunnable, HIGHLIGHT_DEBOUNCE_MS);
    }

    private void performHighlighting() {
        if (scriptAnalyzer == null || !scriptAnalyzer.isInitialized()) return;

        String scriptText = editTextScript.getText().toString();
        if (scriptText.isEmpty()) return;

        // Run analysis in background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            java.util.List<ScriptAnalyzer.FoundCommand> foundCommands =
                    scriptAnalyzer.analyzeScript(scriptText);

            requireActivity().runOnUiThread(() -> {
                isUpdatingFromHighlight = true;
                scriptHighlighter.applyHighlighting(editTextScript, foundCommands);
                isUpdatingFromHighlight = false;
            });
        });
    }
}
