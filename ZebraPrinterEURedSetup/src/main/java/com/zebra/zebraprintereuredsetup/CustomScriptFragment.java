package com.zebra.zebraprintereuredsetup;

import android.annotation.SuppressLint;
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

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class CustomScriptFragment extends Fragment implements ToolbarConfigurable {

    private static final String ARG_SCRIPT_CONTENT = "script_content";
    private static final String ARG_ENTRY_ID = "entry_id";
    private static final String ARG_EDITOR_MODE = "editor_mode";
    private static final String ARG_VIEW_ONLY_MODE = "view_only_mode";
    private static final String ARG_ENTRY_TITLE = "entry_title";
    private static final String ARG_ENTRY_DESCRIPTION = "entry_description";
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");

    // Fragment Result keys for editor mode
    public static final String EDITOR_RESULT_KEY = "script_editor_result";
    public static final String EDITOR_RESULT_SCRIPT = "script_content";
    public static final String EDITOR_RESULT_SAVED = "saved";

    /**
     * Factory method to create a new instance with pre-filled script content.
     * @param scriptContent The script content to pre-fill
     * @return A new instance of CustomScriptFragment
     */
    public static CustomScriptFragment newInstanceWithScript(String scriptContent) {
        CustomScriptFragment fragment = new CustomScriptFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCRIPT_CONTENT, scriptContent);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Factory method to create a new instance with pre-filled script content and entry ID for updating.
     * @param scriptContent The script content to pre-fill
     * @param entryId The ID of the HomeEntry to update when saving
     * @return A new instance of CustomScriptFragment
     */
    public static CustomScriptFragment newInstanceWithScriptAndEntryId(String scriptContent, String entryId) {
        CustomScriptFragment fragment = new CustomScriptFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCRIPT_CONTENT, scriptContent);
        args.putString(ARG_ENTRY_ID, entryId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Factory method to create a new instance in editor mode.
     * Editor mode shows Cancel/Save buttons and returns the script via Fragment Result API.
     * @param initialScript Initial script content (can be null or empty)
     * @return A new instance of CustomScriptFragment in editor mode
     */
    public static CustomScriptFragment newInstanceForEditing(String initialScript) {
        CustomScriptFragment fragment = new CustomScriptFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_EDITOR_MODE, true);
        if (initialScript != null && !initialScript.isEmpty()) {
            args.putString(ARG_SCRIPT_CONTENT, initialScript);
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Factory method to create a new instance in view-only mode.
     * View-only mode makes the script read-only and hides update/file operation cards.
     * @param scriptContent The script content to display
     * @param entryTitle The title of the entry (for toolbar)
     * @param entryDescription The description of the entry (shown in a card)
     * @return A new instance of CustomScriptFragment in view-only mode
     */
    public static CustomScriptFragment newInstanceViewOnly(String scriptContent, String entryTitle, String entryDescription) {
        CustomScriptFragment fragment = new CustomScriptFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_VIEW_ONLY_MODE, true);
        if (scriptContent != null && !scriptContent.isEmpty()) {
            args.putString(ARG_SCRIPT_CONTENT, scriptContent);
        }
        if (entryTitle != null && !entryTitle.isEmpty()) {
            args.putString(ARG_ENTRY_TITLE, entryTitle);
        }
        if (entryDescription != null && !entryDescription.isEmpty()) {
            args.putString(ARG_ENTRY_DESCRIPTION, entryDescription);
        }
        fragment.setArguments(args);
        return fragment;
    }

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

    // Documentation lookup for possible values
    private java.util.Map<String, DocumentationCommand> sgdDocumentationMap = new java.util.HashMap<>();

    // Immersive mode
    private MaterialButton buttonToggleImmersive;
    private boolean isImmersiveMode = false;

    // Card resizing
    private MaterialCardView cardCustomScript;
    private ImageView dragHandle;
    private int minCardHeight;
    private float dragStartY;
    private int dragStartHeight;

    // Cards to hide in immersive mode
    private View cardConnectivity;
    private View cardStatus;
    private View cardSendData;
    private View cardFileOperations;

    // Update Launcher Script card
    private View cardUpdateLauncherScript;
    private MaterialButton buttonCancelChanges;
    private MaterialButton buttonSaveChanges;
    private String launcherEntryId = null;
    private String originalScriptContent = null;
    private com.zebra.zebraprintereuredsetup.data.repository.HomeEntryRepository homeEntryRepository;

    // Editor mode
    private boolean isEditorMode = false;
    private View cardEditorActions;
    private MaterialButton buttonEditorCancel;
    private MaterialButton buttonEditorSave;

    // View-only mode
    private boolean isViewOnlyMode = false;
    private String viewOnlyEntryTitle = null;
    private String viewOnlyEntryDescription = null;

    // Description card (for view-only mode)
    private View cardEntryInfo;
    private TextView textViewEntryTitle;
    private TextView textViewEntryDescription;

    // Script card collapse (for view-only mode)
    private View layoutScriptHeader;
    private ImageView iconScriptExpand;
    private View layoutScriptContent;
    private boolean isScriptExpanded = false;

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

        // Setup card resizing
        cardCustomScript = view.findViewById(R.id.cardCustomScript);
        dragHandle = view.findViewById(R.id.dragHandle);
        setupDragHandle();

        // Cards to hide in immersive mode
        cardConnectivity = view.findViewById(R.id.cardConnectivity);
        cardStatus = view.findViewById(R.id.cardStatus);
        cardSendData = view.findViewById(R.id.cardSendData);
        cardFileOperations = view.findViewById(R.id.cardFileOperations);

        // Update Launcher Script card
        cardUpdateLauncherScript = view.findViewById(R.id.cardUpdateLauncherScript);
        buttonCancelChanges = view.findViewById(R.id.buttonCancelChanges);
        buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges);

        // Editor mode views
        cardEditorActions = view.findViewById(R.id.cardEditorActions);
        buttonEditorCancel = view.findViewById(R.id.buttonEditorCancel);
        buttonEditorSave = view.findViewById(R.id.buttonEditorSave);

        // Entry info card (for view-only mode)
        cardEntryInfo = view.findViewById(R.id.cardEntryInfo);
        textViewEntryTitle = view.findViewById(R.id.textViewEntryTitle);
        textViewEntryDescription = view.findViewById(R.id.textViewEntryDescription);

        // Script card collapse views
        layoutScriptHeader = view.findViewById(R.id.layoutScriptHeader);
        iconScriptExpand = view.findViewById(R.id.iconScriptExpand);
        layoutScriptContent = view.findViewById(R.id.layoutScriptContent);

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

        // Check for editor mode first
        if (getArguments() != null && getArguments().getBoolean(ARG_EDITOR_MODE, false)) {
            isEditorMode = true;
            setupEditorMode();

            // Pre-fill script content if provided
            if (getArguments().containsKey(ARG_SCRIPT_CONTENT)) {
                String scriptContent = getArguments().getString(ARG_SCRIPT_CONTENT);
                if (scriptContent != null && !scriptContent.isEmpty()) {
                    editTextScript.setText(scriptContent);
                }
            }
        }
        // Check for view-only mode
        else if (getArguments() != null && getArguments().getBoolean(ARG_VIEW_ONLY_MODE, false)) {
            isViewOnlyMode = true;

            // Pre-fill script content if provided
            if (getArguments().containsKey(ARG_SCRIPT_CONTENT)) {
                String scriptContent = getArguments().getString(ARG_SCRIPT_CONTENT);
                if (scriptContent != null && !scriptContent.isEmpty()) {
                    editTextScript.setText(scriptContent);
                }
            }

            setupViewOnlyMode();
        }
        // Pre-fill script content if provided as argument (non-editor mode)
        else if (getArguments() != null && getArguments().containsKey(ARG_SCRIPT_CONTENT)) {
            String scriptContent = getArguments().getString(ARG_SCRIPT_CONTENT);
            if (scriptContent != null && !scriptContent.isEmpty()) {
                editTextScript.setText(scriptContent);
            }

            // Check if we have an entry ID (editing from launcher)
            if (getArguments().containsKey(ARG_ENTRY_ID)) {
                launcherEntryId = getArguments().getString(ARG_ENTRY_ID);
                originalScriptContent = scriptContent;

                // Initialize repository for saving changes
                homeEntryRepository = new com.zebra.zebraprintereuredsetup.data.repository.HomeEntryRepository(requireContext());

                // Setup change tracking
                setupLauncherScriptChangeTracking();
            }
        }

        textViewStatus = view.findViewById(R.id.textViewStatus);

        // Immersive mode toggle button
        buttonToggleImmersive = view.findViewById(R.id.buttonToggleImmersive);
        buttonToggleImmersive.setOnClickListener(v -> toggleImmersiveMode());

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

        // Apply suggestions limit from settings
        boolean isUnlimited = SettingsHelper.getSuggestionsUnlimited(requireContext());
        if (isUnlimited) {
            suggestionAdapter.setMaxSuggestions(-1); // unlimited
        } else {
            suggestionAdapter.setMaxSuggestions(SettingsHelper.getMaxSuggestions(requireContext()));
        }

        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        // Handle suggestion click
        suggestionAdapter.setOnSuggestionClickListener(suggestion -> {
            insertSuggestion(suggestion);
            // Don't hide suggestions here - let insertSuggestion/updateSuggestions handle it
            // This allows continuous SGD path completion
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

        // Check if we're after a completed setvar command - suggest possible values
        // Pattern: ! U1 setvar "command.path" <cursor>
        java.util.regex.Pattern setvarValuePattern = java.util.regex.Pattern.compile(
                "!\\s*u1\\s+setvar\\s+\"([^\"]+)\"\\s*$", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher setvarMatcher = setvarValuePattern.matcher(trimmedContext);
        if (setvarMatcher.find()) {
            String sgdCommand = setvarMatcher.group(1);
            List<CommandSuggestion> valueSuggestions = createValueSuggestions(sgdCommand);
            if (!valueSuggestions.isEmpty()) {
                suggestionAdapter.setSpecialSuggestions(valueSuggestions);
                showSuggestions();
                return;
            }
        }

        // Check if we're inside quotes after getvar/setvar/do - suggest SGD commands
        java.util.regex.Pattern sgdInsideQuotesPattern = java.util.regex.Pattern.compile(
                "!\\s*u1\\s+(getvar|setvar|do)\\s+\"([^\"]*)$", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher sgdInsideMatcher = sgdInsideQuotesPattern.matcher(trimmedContext);
        if (sgdInsideMatcher.find()) {
            // Extract the SGD path typed so far (everything after the opening quote)
            String sgdPath = sgdInsideMatcher.group(2).trim();

            // Filter SGD commands only - use lower threshold (1 char) for SGD context
            if (sgdPath.length() >= 1) {
                suggestionAdapter.filterSgdOnly(sgdPath);
                if (suggestionAdapter.hasSuggestions()) {
                    showSuggestions();
                } else {
                    hideSuggestions();
                }
                return;
            } else {
                // Empty path - hide suggestions until user starts typing
                hideSuggestions();
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

    private java.util.List<CommandSuggestion> createValueSuggestions(String sgdCommand) {
        java.util.List<CommandSuggestion> suggestions = new java.util.ArrayList<>();
        java.util.Set<String> addedValues = new java.util.HashSet<>();

        // Look up the command in documentation
        DocumentationCommand docCmd = sgdDocumentationMap.get(sgdCommand.toLowerCase());
        if (docCmd == null) {
            return suggestions;
        }

        // Check default value length first
        String defaultValue = docCmd.getDefaultValue();
        boolean defaultIsTooLong = defaultValue != null && !defaultValue.isEmpty()
                && !defaultValue.equalsIgnoreCase("NA") && defaultValue.length() > 10;

        // Check if any possible value is too long
        java.util.List<String> possibleValues = docCmd.getPossibleValues();
        boolean hasLongValues = defaultIsTooLong;

        if (possibleValues != null) {
            for (String value : possibleValues) {
                java.util.List<String> simpleValues = extractSimpleValues(value);
                if (simpleValues.isEmpty() && value.length() > 10) {
                    hasLongValues = true;
                    break;
                }
            }
        }

        // If any value is too long, only show "See documentation"
        if (hasLongValues) {
            // Store the SGD command for documentation lookup
            suggestions.add(new CommandSuggestion(
                    "See documentation",
                    sgdCommand,  // Store command name for lookup
                    sgdCommand,  // Also in format for lookup
                    "HINT"
            ));
            return suggestions;
        }

        // Add default value first if available and short enough
        if (defaultValue != null && !defaultValue.isEmpty() && !defaultValue.equalsIgnoreCase("NA")) {
            String insertText = " \"" + defaultValue + "\"";
            suggestions.add(new CommandSuggestion(
                    defaultValue,
                    "Default value",
                    insertText,
                    "VALUE"
            ));
            addedValues.add(defaultValue.toLowerCase());
        }

        if (possibleValues == null || possibleValues.isEmpty()) {
            return suggestions;
        }

        for (String value : possibleValues) {
            // Parse and extract simple values from the string
            java.util.List<String> simpleValues = extractSimpleValues(value);

            if (!simpleValues.isEmpty()) {
                for (String simpleValue : simpleValues) {
                    if (!addedValues.contains(simpleValue.toLowerCase())) {
                        String insertText = " \"" + simpleValue + "\"";
                        suggestions.add(new CommandSuggestion(
                                simpleValue,
                                "Possible value",
                                insertText,
                                "VALUE"
                        ));
                        addedValues.add(simpleValue.toLowerCase());
                    }
                }
            } else if (value.length() <= 10 && !addedValues.contains(value.toLowerCase())) {
                String insertText = " \"" + value + "\"";
                suggestions.add(new CommandSuggestion(
                        value,
                        "Possible value",
                        insertText,
                        "VALUE"
                ));
                addedValues.add(value.toLowerCase());
            }
        }

        return suggestions;
    }

    private java.util.List<String> extractSimpleValues(String valueString) {
        java.util.List<String> simpleValues = new java.util.ArrayList<>();

        // Pattern to match simple values: on, off, true, false, yes, no, numeric, or short values like "40-bit"
        java.util.regex.Pattern simpleValuePattern = java.util.regex.Pattern.compile(
                "\\b(on|off|true|false|yes|no|enabled|disabled|none|all|\\d+(?:-bit)?|\\d+(?:\\.\\d+)?)\\b",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );

        java.util.regex.Matcher matcher = simpleValuePattern.matcher(valueString);
        java.util.Set<String> seen = new java.util.HashSet<>();

        while (matcher.find()) {
            String value = matcher.group(1).toLowerCase();
            if (!seen.contains(value)) {
                simpleValues.add(value);
                seen.add(value);
            }
        }

        return simpleValues;
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

        // Special case: VALUE type - just append the value at cursor
        if ("VALUE".equalsIgnoreCase(suggestion.getType())) {
            String newText = text.substring(0, cursorPos) + insertText + text.substring(cursorPos);
            editTextScript.setText(newText);
            editTextScript.setSelection(cursorPos + insertText.length());
            isInsertingSuggestion = false;
            debounceHighlighting();
            hideSuggestions();
            return;
        }

        // Special case: HINT type - don't insert anything, show documentation
        if ("HINT".equalsIgnoreCase(suggestion.getType())) {
            isInsertingSuggestion = false;
            // Show documentation bottom sheet for the SGD command
            String sgdCommand = suggestion.getFormat(); // SGD command name stored in format
            if (sgdCommand != null && !sgdCommand.isEmpty()) {
                DocumentationCommand docCmd = sgdDocumentationMap.get(sgdCommand.toLowerCase());
                if (docCmd != null) {
                    CommandDocumentationBottomSheet bottomSheet =
                            CommandDocumentationBottomSheet.newInstance(docCmd);
                    bottomSheet.show(getChildFragmentManager(), "command_doc");
                }
            }
            hideSuggestions();
            return;
        }

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

            // Trigger syntax highlighting and continue suggestions
            debounceHighlighting();
            suggestionHandler.postDelayed(this::updateSuggestions, 50);
            return;
        }

        // Special case: inserting SGD command inside quotes after getvar/setvar/do
        java.util.regex.Pattern sgdQuotePattern = java.util.regex.Pattern.compile(
                "!\\s*u1\\s+(getvar|setvar|do)\\s+\"([^\"]*)$", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher sgdQuoteMatcher = sgdQuotePattern.matcher(trimmedContext);
        if (sgdQuoteMatcher.find() && "SGD".equalsIgnoreCase(suggestion.getType())) {
            // Progressive SGD suggestion - just append the next segment
            // insertText contains only the new part to add (e.g., ".11ac." or ".enable")
            String segmentToAdd = insertText;

            // Handle trailing whitespace inside quotes - find the quote position
            // and calculate insertion point after the trimmed SGD path
            String sgdPathInQuotes = sgdQuoteMatcher.group(2);
            String trimmedSgdPath = sgdPathInQuotes.trim();
            int trailingWhitespaceCount = sgdPathInQuotes.length() - trimmedSgdPath.length();

            // Adjust cursor position to be right after the trimmed SGD path
            int adjustedCursorPos = cursorPos - trailingWhitespaceCount;

            // Check if this is the final segment (no more sub-paths)
            // If name is "..." there are more segments, otherwise it's the final one
            boolean isFinalSegment = !"...".equals(suggestion.getName());

            String newText;
            int newCursorPos;

            if (isFinalSegment) {
                // Final segment - add closing quote and space for the value argument
                // Remove trailing whitespace and insert segment + quote + space
                newText = text.substring(0, adjustedCursorPos) + segmentToAdd + "\" " + text.substring(cursorPos);
                newCursorPos = adjustedCursorPos + segmentToAdd.length() + 2; // +2 for quote and space
            } else {
                // More segments to come - don't add closing quote
                // Remove trailing whitespace and insert segment
                newText = text.substring(0, adjustedCursorPos) + segmentToAdd + text.substring(cursorPos);
                newCursorPos = adjustedCursorPos + segmentToAdd.length();
            }

            editTextScript.setText(newText);
            editTextScript.setSelection(newCursorPos);
            isInsertingSuggestion = false;

            // Trigger syntax highlighting
            debounceHighlighting();

            // Always try to update suggestions (let updateSuggestions decide if there are more)
            suggestionHandler.postDelayed(this::updateSuggestions, 50);
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

            // Trigger syntax highlighting
            debounceHighlighting();

            // Always try to continue suggestions for SGD paths
            suggestionHandler.postDelayed(this::updateSuggestions, 50);
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

            // Trigger syntax highlighting and hide suggestions
            debounceHighlighting();
            hideSuggestions();
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

        // Add space after ZBI commands and completed SGD commands (with closing quote)
        String type = suggestion.getType();
        String finalInsertText = insertText;
        if ("ZBI".equalsIgnoreCase(type)) {
            finalInsertText = insertText + " ";
        } else if ("SGD".equalsIgnoreCase(type) && insertText.endsWith("\"")) {
            // Only add space after SGD if the path is complete (ends with closing quote)
            finalInsertText = insertText + " ";
        }

        // Replace the current word with the suggestion
        String newText = text.substring(0, wordStart) + finalInsertText + text.substring(cursorPos);
        editTextScript.setText(newText);

        // Move cursor to end of inserted text
        editTextScript.setSelection(wordStart + finalInsertText.length());

        isInsertingSuggestion = false;

        // Trigger syntax highlighting
        debounceHighlighting();

        // For incomplete SGD commands (no closing quote), continue showing suggestions
        // Otherwise hide suggestions
        if ("SGD".equalsIgnoreCase(type) && !insertText.endsWith("\"")) {
            suggestionHandler.postDelayed(this::updateSuggestions, 50);
        } else {
            hideSuggestions();
        }
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
                        DocumentationCommand sgdCmd = DocumentationCommand.fromJson(sgdArray.getJSONObject(i));
                        commands.add(sgdCmd);
                        // Also add to map for value lookup
                        if (sgdCmd.getCommand() != null) {
                            sgdDocumentationMap.put(sgdCmd.getCommand().toLowerCase(), sgdCmd);
                        }
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

    // ===== Immersive Mode Methods =====

    private void toggleImmersiveMode() {
        if (isImmersiveMode) {
            exitImmersiveMode();
        } else {
            enterImmersiveMode();
        }
    }

    private void enterImmersiveMode() {
        if (getActivity() == null || getActivity().getWindow() == null) return;

        isImmersiveMode = true;
        buttonToggleImmersive.setIconResource(R.drawable.ic_fullscreen_exit);

        // Hide other cards (keep suggestions card visible)
        if (cardConnectivity != null) cardConnectivity.setVisibility(View.GONE);
        if (cardStatus != null) cardStatus.setVisibility(View.GONE);
        if (cardSendData != null) cardSendData.setVisibility(View.GONE);
        if (cardFileOperations != null) cardFileOperations.setVisibility(View.GONE);

        // Hide MainActivity's action bar if present
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.getSupportActionBar() != null) {
                mainActivity.getSupportActionBar().hide();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getActivity().getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Legacy immersive mode for older Android versions
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void exitImmersiveMode() {
        if (getActivity() == null || getActivity().getWindow() == null) return;

        isImmersiveMode = false;
        buttonToggleImmersive.setIconResource(R.drawable.ic_fullscreen);

        // Show other cards (but respect view-only mode)
        if (cardConnectivity != null) cardConnectivity.setVisibility(View.VISIBLE);
        if (cardStatus != null) cardStatus.setVisibility(View.VISIBLE);
        if (cardSendData != null) cardSendData.setVisibility(View.VISIBLE);
        // Don't show file operations in view-only mode
        if (cardFileOperations != null && !isViewOnlyMode) cardFileOperations.setVisibility(View.VISIBLE);

        // Show MainActivity's action bar if present
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.getSupportActionBar() != null) {
                mainActivity.getSupportActionBar().show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getActivity().getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            // Legacy mode for older Android versions
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Exit immersive mode when leaving fragment
        if (isImmersiveMode) {
            exitImmersiveMode();
        }
    }

    // ===== Card Resizing Methods =====

    @SuppressLint("ClickableViewAccessibility")
    private void setupDragHandle() {
        // Calculate minimum height (280dp in pixels)
        minCardHeight = (int) (280 * getResources().getDisplayMetrics().density);

        // Use post to ensure the view is laid out before we measure/modify it
        cardCustomScript.post(() -> {
            // Skip height restoration in view-only mode (card uses wrap_content when collapsed)
            if (isViewOnlyMode) return;

            // Restore saved height or use default
            int savedHeight = SettingsHelper.getCustomScriptCardHeight(requireContext());
            if (savedHeight > 0 && savedHeight >= minCardHeight) {
                ViewGroup.LayoutParams params = cardCustomScript.getLayoutParams();
                params.height = savedHeight;
                cardCustomScript.setLayoutParams(params);
                cardCustomScript.requestLayout();
            }
        });

        dragHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartY = event.getRawY();
                    dragStartHeight = cardCustomScript.getHeight();
                    // Prevent parent from intercepting touch events
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getRawY() - dragStartY;
                    int newHeight = (int) (dragStartHeight + deltaY);

                    // Enforce minimum height only (no maximum)
                    if (newHeight < minCardHeight) {
                        newHeight = minCardHeight;
                    }

                    ViewGroup.LayoutParams params = cardCustomScript.getLayoutParams();
                    params.height = newHeight;
                    cardCustomScript.setLayoutParams(params);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Save the new height
                    int finalHeight = cardCustomScript.getHeight();
                    SettingsHelper.saveCustomScriptCardHeight(requireContext(), finalHeight);
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return false;
        });
    }

    // ===== Editor Mode Methods =====

    /**
     * Sets up the fragment for editor mode.
     * Hides unnecessary cards and shows Cancel/Save buttons.
     */
    private void setupEditorMode() {
        // Hide cards that are not needed in editor mode
        cardConnectivity.setVisibility(View.GONE);
        cardStatus.setVisibility(View.GONE);
        cardSendData.setVisibility(View.GONE);

        // Show editor actions card
        cardEditorActions.setVisibility(View.VISIBLE);

        // Setup button click handlers
        buttonEditorCancel.setOnClickListener(v -> {
            // Return empty result (cancelled)
            Bundle result = new Bundle();
            result.putBoolean(EDITOR_RESULT_SAVED, false);
            result.putString(EDITOR_RESULT_SCRIPT, "");
            getParentFragmentManager().setFragmentResult(EDITOR_RESULT_KEY, result);

            // Go back
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        buttonEditorSave.setOnClickListener(v -> {
            // Return the script content
            String scriptContent = editTextScript.getText() != null ? editTextScript.getText().toString() : "";
            Bundle result = new Bundle();
            result.putBoolean(EDITOR_RESULT_SAVED, true);
            result.putString(EDITOR_RESULT_SCRIPT, scriptContent);
            getParentFragmentManager().setFragmentResult(EDITOR_RESULT_KEY, result);

            // Go back
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Sets up the fragment for view-only mode.
     * Makes the script read-only and hides update/file operation cards.
     */
    private void setupViewOnlyMode() {
        // Parse title and description from arguments
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_ENTRY_TITLE)) {
                viewOnlyEntryTitle = getArguments().getString(ARG_ENTRY_TITLE);
            }
            if (getArguments().containsKey(ARG_ENTRY_DESCRIPTION)) {
                viewOnlyEntryDescription = getArguments().getString(ARG_ENTRY_DESCRIPTION);
            }
        }

        // Show entry info card only if we have a description
        if (viewOnlyEntryDescription != null && !viewOnlyEntryDescription.isEmpty()) {
            cardEntryInfo.setVisibility(View.VISIBLE);
            textViewEntryDescription.setText(viewOnlyEntryDescription);
        }

        // Make the script EditText completely read-only
        editTextScript.setFocusable(false);
        editTextScript.setFocusableInTouchMode(false);
        editTextScript.setCursorVisible(false);
        editTextScript.setKeyListener(null); // Prevents text input even if focused
        editTextScript.setTextIsSelectable(true); // Allow text selection for copying

        // Add long-press listener to show "Open Temporary" menu
        editTextScript.setOnLongClickListener(v -> {
            showViewOnlyContextMenu(v);
            return true;
        });

        // Collapse script card by default in view-only mode
        iconScriptExpand.setVisibility(View.VISIBLE);
        layoutScriptContent.setVisibility(View.GONE);
        isScriptExpanded = false;

        // Change card height to wrap_content so it shrinks when collapsed
        ViewGroup.LayoutParams params = cardCustomScript.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        cardCustomScript.setLayoutParams(params);

        // Toggle expand/collapse on header click
        layoutScriptHeader.setOnClickListener(v -> {
            isScriptExpanded = !isScriptExpanded;
            if (isScriptExpanded) {
                layoutScriptContent.setVisibility(View.VISIBLE);
                iconScriptExpand.animate().rotation(180).setDuration(200).start();
                // Restore fixed height for expanded content
                ViewGroup.LayoutParams expandedParams = cardCustomScript.getLayoutParams();
                expandedParams.height = (int) (280 * getResources().getDisplayMetrics().density);
                cardCustomScript.setLayoutParams(expandedParams);
            } else {
                layoutScriptContent.setVisibility(View.GONE);
                iconScriptExpand.animate().rotation(0).setDuration(200).start();
                // Shrink card to wrap_content when collapsed
                ViewGroup.LayoutParams collapsedParams = cardCustomScript.getLayoutParams();
                collapsedParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cardCustomScript.setLayoutParams(collapsedParams);
            }
        });

        // Hide file operations card - not needed in view mode
        cardFileOperations.setVisibility(View.GONE);

        // Keep cardUpdateLauncherScript hidden (it's already hidden by default)
        cardUpdateLauncherScript.setVisibility(View.GONE);

        // Keep cardConnectivity, cardStatus, cardSendData visible for sending to printer
    }

    /**
     * Shows context menu for view-only mode with "Open Temporary" option.
     */
    private void showViewOnlyContextMenu(View anchorView) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), anchorView);
        popupMenu.getMenu().add(0, 1, 0, R.string.action_open_temporary);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openTemporaryEditor();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    /**
     * Opens the script in temporary edit mode (full features, no database saving).
     */
    private void openTemporaryEditor() {
        String currentScript = editTextScript.getText() != null ? editTextScript.getText().toString() : "";

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.openCustomScriptTemporary(currentScript);
        }
    }

    // ===== Launcher Script Update Methods =====

    /**
     * Sets up change tracking for launcher script editing.
     * Card is always visible; Save button is enabled only when script has changed.
     */
    private void setupLauncherScriptChangeTracking() {
        // Show the card immediately and disable Save until changes are made
        cardUpdateLauncherScript.setVisibility(View.VISIBLE);
        buttonSaveChanges.setEnabled(false);

        // Add text change listener to detect changes
        editTextScript.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (launcherEntryId != null && originalScriptContent != null) {
                    String currentContent = s.toString();
                    boolean hasChanges = !currentContent.equals(originalScriptContent);
                    buttonSaveChanges.setEnabled(hasChanges);
                }
            }
        });

        // Setup button click handlers
        buttonCancelChanges.setOnClickListener(v -> {
            // Revert to original content
            if (originalScriptContent != null) {
                editTextScript.setText(originalScriptContent);
            }
            buttonSaveChanges.setEnabled(false);
        });

        buttonSaveChanges.setOnClickListener(v -> {
            // Save changes to the database
            if (launcherEntryId != null && homeEntryRepository != null) {
                String newScriptContent = editTextScript.getText().toString();

                // Update the entry in the database (runs on background thread)
                new Thread(() -> {
                    com.zebra.zebraprintereuredsetup.data.entity.HomeEntry entry =
                            homeEntryRepository.getEntryByIdSync(launcherEntryId);
                    if (entry != null) {
                        homeEntryRepository.updateCustomEntry(
                                launcherEntryId,
                                entry.getTitleCustom(),
                                entry.getDescriptionCustom(),
                                newScriptContent
                        );

                        requireActivity().runOnUiThread(() -> {
                            // Update the original content to the new saved content
                            originalScriptContent = newScriptContent;
                            buttonSaveChanges.setEnabled(false);

                            // Show success message
                            Toast.makeText(requireContext(),
                                    R.string.status_launcher_script_updated,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });
    }

    // ToolbarConfigurable implementation
    @Override
    public int getToolbarTitleResId() {
        return R.string.nav_custom_script;
    }

    @Override
    public String getToolbarTitle() {
        // Return custom entry title in view-only mode
        // Read directly from arguments since this may be called before setupViewOnlyMode()
        if (getArguments() != null && getArguments().getBoolean(ARG_VIEW_ONLY_MODE, false)) {
            String title = getArguments().getString(ARG_ENTRY_TITLE);
            if (title != null && !title.isEmpty()) {
                return title;
            }
        }
        return null; // Use default from getToolbarTitleResId()
    }

    @Override
    public boolean showBackButton() {
        return true;
    }
}
