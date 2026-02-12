package com.zebra.zebraprintereuredsetup;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.zebra.zebraprintereuredsetup.data.entity.HomeEntry;
import com.zebra.zebraprintereuredsetup.data.repository.HomeEntryRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public interface NavigationCallback {
        void navigateTo(int navItemId);
        void openCustomScriptWithContent(String scriptContent);
        void openCustomScriptWithContentAndEntryId(String scriptContent, String entryId);
        void openScriptEditorForResult(String initialScript);
        void openCustomScriptViewOnly(String scriptContent, String entryTitle, String entryDescription);
    }

    private NavigationCallback navigationCallback;
    private EditText editTextSearch;
    private MaterialButton buttonExitEditMode;
    private RecyclerView recyclerViewEntries;
    private FloatingActionButton fabAddEntry;
    private HomeEntryAdapter adapter;
    private HomeEntryRepository repository;
    private List<HomeEntry> allEntries = new ArrayList<>();
    private String currentSearchQuery = "";

    // Add Custom Script dialog state
    private String pendingCustomTitle = "";
    private String pendingCustomDescription = "";
    private String pendingCustomScript = "";
    private AlertDialog currentAddDialog = null;
    private ScriptAnalyzer scriptAnalyzer;

    // Edit Custom Action dialog state
    private HomeEntry editingEntry = null;
    private String editingTitle = "";
    private String editingDescription = "";
    private String editingScript = "";
    private AlertDialog currentEditDialog = null;
    private boolean isEditMode = false;

    // Drag and drop
    private ItemTouchHelper itemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Try to get navigation callback from activity
        if (getActivity() instanceof NavigationCallback) {
            navigationCallback = (NavigationCallback) getActivity();
        }

        // Initialize repository
        repository = new HomeEntryRepository(requireContext());

        // Initialize script analyzer for validation
        scriptAnalyzer = new ScriptAnalyzer();
        loadScriptAnalyzerCommands();

        // Setup fragment result listener for script editor
        setupScriptEditorResultListener();

        setupViews(view);
        setupRecyclerView();
        setupSearch(view);
        setupFab();
        observeEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update FAB visibility when returning to this fragment
        updateFabVisibility();
    }

    private void setupViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonExitEditMode = view.findViewById(R.id.buttonExitEditMode);
        recyclerViewEntries = view.findViewById(R.id.recyclerViewEntries);
        fabAddEntry = view.findViewById(R.id.fabAddEntry);

        // Exit Edit Mode button click listener
        buttonExitEditMode.setOnClickListener(v -> exitEditMode());
    }

    private void setupRecyclerView() {
        adapter = new HomeEntryAdapter(requireContext());
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewEntries.setAdapter(adapter);

        // Click listener for navigation
        adapter.setOnEntryClickListener(this::onEntryClicked);

        // Long click listener for context menu
        adapter.setOnEntryLongClickListener(this::onEntryLongClicked);

        // Setup drag and drop
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Only allow drag when edit mode is enabled
                if (!SettingsHelper.getEditModeEnabled(requireContext())) {
                    return 0;
                }
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used - swipe disabled
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Save the new order when drag ends
                saveEntryOrder();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                // Disable long press drag - we use the drag handle
                return false;
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewEntries);

        // Set drag start listener on adapter
        adapter.setOnDragStartListener(viewHolder -> {
            if (SettingsHelper.getEditModeEnabled(requireContext())) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });
    }

    private void saveEntryOrder() {
        List<HomeEntry> currentEntries = adapter.getEntries();
        List<String> orderedIds = new ArrayList<>();
        for (HomeEntry entry : currentEntries) {
            orderedIds.add(entry.getId());
        }
        repository.updateOrder(orderedIds);
    }

    private void setupSearch(View view) {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString();
                filterEntries();
            }
        });
    }

    private void setupFab() {
        fabAddEntry.setOnClickListener(v -> showFabMenu());
        updateFabVisibility();
    }

    private void updateFabVisibility() {
        boolean editModeEnabled = SettingsHelper.getEditModeEnabled(requireContext());
        fabAddEntry.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);
        buttonExitEditMode.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);

        // Update adapter to show/hide drag handles
        if (adapter != null) {
            adapter.setEditModeEnabled(editModeEnabled);
        }
    }

    /**
     * Exits edit mode and updates the UI accordingly.
     */
    private void exitEditMode() {
        SettingsHelper.saveEditModeEnabled(requireContext(), false);
        updateFabVisibility();
    }

    private void observeEntries() {
        boolean advancedMode = SettingsHelper.getAdvancedModeEnabled(requireContext());
        repository.getVisibleHomeEntries(advancedMode).observe(getViewLifecycleOwner(), entries -> {
            allEntries = entries != null ? entries : new ArrayList<>();
            filterEntries();
        });
    }

    private void filterEntries() {
        if (currentSearchQuery == null || currentSearchQuery.isEmpty()) {
            adapter.setEntries(allEntries);
        } else {
            List<HomeEntry> filteredEntries = new ArrayList<>();
            String lowerQuery = currentSearchQuery.toLowerCase();
            for (HomeEntry entry : allEntries) {
                String title = entry.getDisplayTitle(requireContext()).toLowerCase();
                String description = entry.getDisplayDescription(requireContext()).toLowerCase();
                if (title.contains(lowerQuery) || description.contains(lowerQuery)) {
                    filteredEntries.add(entry);
                }
            }
            adapter.setEntries(filteredEntries);
        }
    }

    private void onEntryClicked(HomeEntry entry) {
        // Check if it's a custom entry with script content
        if (entry.isCustomEntry() && entry.getCustomScriptContent() != null && !entry.getCustomScriptContent().isEmpty()) {
            if (navigationCallback != null) {
                if (SettingsHelper.getEditModeEnabled(requireContext())) {
                    // In edit mode, open the full script editor (same as long-press "Edit Script")
                    navigationCallback.openCustomScriptWithContentAndEntryId(
                            entry.getCustomScriptContent(),
                            entry.getId()
                    );
                } else {
                    // Normal mode: open in view-only mode (read-only, no update/file operations)
                    String title = entry.getDisplayTitle(requireContext());
                    String description = entry.getDisplayDescription(requireContext());
                    navigationCallback.openCustomScriptViewOnly(entry.getCustomScriptContent(), title, description);
                }
            }
            return;
        }

        // Navigate to the target for system entries
        int navResId = entry.getNavigationResId(requireContext());
        if (navResId != 0 && navigationCallback != null) {
            navigationCallback.navigateTo(navResId);
        }
    }

    private boolean onEntryLongClicked(View view, HomeEntry entry) {
        // Only show context menu if edit mode is enabled
        if (!SettingsHelper.getEditModeEnabled(requireContext())) {
            return false;
        }

        PopupMenu popupMenu = new PopupMenu(requireContext(), view);

        // For custom entries: Edit, Edit Script, Hide, Delete
        if (entry.isCustomEntry()) {
            popupMenu.getMenu().add(0, R.id.action_edit, 0, R.string.action_edit);
            popupMenu.getMenu().add(0, R.id.action_edit_script, 1, R.string.action_edit_script);
            popupMenu.getMenu().add(0, R.id.action_hide, 2, R.string.action_hide);
            popupMenu.getMenu().add(0, R.id.action_delete, 3, R.string.action_delete);
        } else {
            // For system entries: only Hide
            popupMenu.getMenu().add(0, R.id.action_hide, 0, R.string.action_hide);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_hide) {
                repository.hideEntry(entry.getId());
                return true;
            } else if (itemId == R.id.action_edit) {
                // Open the Edit Custom Action dialog
                showEditCustomActionDialog(entry);
                return true;
            } else if (itemId == R.id.action_edit_script) {
                // Open the script editor with full features
                if (navigationCallback != null && entry.getCustomScriptContent() != null) {
                    navigationCallback.openCustomScriptWithContentAndEntryId(
                            entry.getCustomScriptContent(),
                            entry.getId()
                    );
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(entry);
                return true;
            }
            return false;
        });

        popupMenu.show();
        return true;
    }

    private void showDeleteConfirmationDialog(HomeEntry entry) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.action_delete)
                .setMessage(getString(R.string.dialog_delete_entry_message, entry.getDisplayTitle(requireContext())))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    repository.deleteEntry(entry.getId());
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void showEditCustomActionDialog(HomeEntry entry) {
        // Initialize editing state
        editingEntry = entry;
        editingTitle = entry.getTitleCustom() != null ? entry.getTitleCustom() : "";
        editingDescription = entry.getDescriptionCustom() != null ? entry.getDescriptionCustom() : "";
        editingScript = entry.getCustomScriptContent() != null ? entry.getCustomScriptContent() : "";
        isEditMode = true;

        showEditCustomActionDialogWithState();
    }

    private void showEditCustomActionDialogWithState() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_custom_entry, null);

        TextInputEditText editTitle = dialogView.findViewById(R.id.editTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editDescription);
        MaterialButton buttonScanTitle = dialogView.findViewById(R.id.buttonScanTitle);
        MaterialButton buttonScanDescription = dialogView.findViewById(R.id.buttonScanDescription);
        TextView textViewScriptStatus = dialogView.findViewById(R.id.textViewScriptStatus);
        MaterialButton buttonEditScript = dialogView.findViewById(R.id.buttonEditScript);

        // Set current values
        editTitle.setText(editingTitle);
        editDescription.setText(editingDescription);
        updateEditScriptStatusText(textViewScriptStatus);

        // Auto-capitalize title (first letter of each word)
        addTitleCapitalizationWatcher(editTitle);

        // Auto-capitalize description (first character only)
        addDescriptionCapitalizationWatcher(editDescription);

        // Scan buttons
        buttonScanTitle.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scan feature coming soon", Toast.LENGTH_SHORT).show();
        });

        buttonScanDescription.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scan feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Edit Script button
        buttonEditScript.setOnClickListener(v -> {
            // Save current state before opening editor
            editingTitle = editTitle.getText() != null ? editTitle.getText().toString() : "";
            editingDescription = editDescription.getText() != null ? editDescription.getText().toString() : "";

            // Dismiss current dialog
            if (currentEditDialog != null) {
                currentEditDialog.dismiss();
            }

            // Open script editor
            if (navigationCallback != null) {
                navigationCallback.openScriptEditorForResult(editingScript);
            }
        });

        currentEditDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_edit_custom_action_title)
                .setView(dialogView)
                .setPositiveButton(R.string.button_save_changes, null) // Set to null, we'll override below
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                    // Clear editing state on cancel
                    clearEditingState();
                })
                .create();

        currentEditDialog.setOnShowListener(dialog -> {
            currentEditDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String finalTitle = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
                String finalDescription = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

                // Validate title
                if (finalTitle.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.error_title_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate script
                if (editingScript.length() < 3) {
                    Toast.makeText(requireContext(), R.string.error_script_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the entry
                updateCustomEntry(finalTitle, finalDescription);
            });
        });

        currentEditDialog.show();
    }

    private void updateEditScriptStatusText(TextView textViewScriptStatus) {
        if (editingScript == null || editingScript.isEmpty()) {
            textViewScriptStatus.setText(R.string.script_status_empty);
        } else {
            textViewScriptStatus.setText(getString(R.string.script_status_defined, editingScript.length()));
        }
    }

    private void updateCustomEntry(String title, String description) {
        if (editingEntry != null) {
            repository.updateCustomEntry(editingEntry.getId(), title, description, editingScript);

            // Clear state and dismiss dialog
            clearEditingState();
            if (currentEditDialog != null) {
                currentEditDialog.dismiss();
                currentEditDialog = null;
            }

            Toast.makeText(requireContext(), R.string.status_launcher_script_updated, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearEditingState() {
        editingEntry = null;
        editingTitle = "";
        editingDescription = "";
        editingScript = "";
        isEditMode = false;
    }

    private void showFabMenu() {
        // Check for hidden custom scripts on background thread first
        new Thread(() -> {
            List<HomeEntry> hiddenCustomEntries = repository.getHiddenCustomEntriesSync();
            boolean hasHiddenCustomScripts = !hiddenCustomEntries.isEmpty();

            requireActivity().runOnUiThread(() -> {
                PopupMenu popupMenu = new PopupMenu(requireContext(), fabAddEntry);
                popupMenu.getMenu().add(0, 1, 0, R.string.menu_add_library_script);
                popupMenu.getMenu().add(0, 2, 1, R.string.menu_add_custom_script);

                // Only show "Show Hidden Scripts" if there are hidden custom scripts
                if (hasHiddenCustomScripts) {
                    popupMenu.getMenu().add(0, 3, 2, R.string.menu_show_hidden_scripts);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        showAddLibraryScriptDialog();
                        return true;
                    } else if (item.getItemId() == 2) {
                        showAddCustomScriptDialog();
                        return true;
                    } else if (item.getItemId() == 3) {
                        showHiddenCustomScriptsDialog();
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        }).start();
    }

    private void showAddLibraryScriptDialog() {
        // Run on background thread to get hidden entries
        new Thread(() -> {
            List<HomeEntry> hiddenEntries = repository.getHiddenSystemEntriesSync();

            requireActivity().runOnUiThread(() -> {
                if (hiddenEntries.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.no_hidden_entries, Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] titles = new String[hiddenEntries.size()];
                for (int i = 0; i < hiddenEntries.size(); i++) {
                    titles[i] = hiddenEntries.get(i).getDisplayTitle(requireContext());
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.menu_add_library_script)
                        .setItems(titles, (dialog, which) -> {
                            HomeEntry selectedEntry = hiddenEntries.get(which);
                            repository.showEntry(selectedEntry.getId());
                        })
                        .setNegativeButton(R.string.button_cancel, null)
                        .show();
            });
        }).start();
    }

    private void showHiddenCustomScriptsDialog() {
        // Run on background thread to get hidden custom entries
        new Thread(() -> {
            List<HomeEntry> hiddenCustomEntries = repository.getHiddenCustomEntriesSync();

            requireActivity().runOnUiThread(() -> {
                if (hiddenCustomEntries.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.no_hidden_custom_scripts, Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] titles = new String[hiddenCustomEntries.size()];
                for (int i = 0; i < hiddenCustomEntries.size(); i++) {
                    titles[i] = hiddenCustomEntries.get(i).getDisplayTitle(requireContext());
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.menu_show_hidden_scripts)
                        .setItems(titles, (dialog, which) -> {
                            HomeEntry selectedEntry = hiddenCustomEntries.get(which);
                            repository.showEntry(selectedEntry.getId());
                        })
                        .setNegativeButton(R.string.button_cancel, null)
                        .show();
            });
        }).start();
    }

    private void showAddCustomScriptDialog() {
        showAddCustomScriptDialogWithState(pendingCustomTitle, pendingCustomDescription, pendingCustomScript);
    }

    private void showAddCustomScriptDialogWithState(String title, String description, String script) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_custom_entry, null);

        TextInputEditText editTitle = dialogView.findViewById(R.id.editTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editDescription);
        MaterialButton buttonScanTitle = dialogView.findViewById(R.id.buttonScanTitle);
        MaterialButton buttonScanDescription = dialogView.findViewById(R.id.buttonScanDescription);
        TextView textViewScriptStatus = dialogView.findViewById(R.id.textViewScriptStatus);
        MaterialButton buttonEditScript = dialogView.findViewById(R.id.buttonEditScript);

        // Restore state
        editTitle.setText(title);
        editDescription.setText(description);
        pendingCustomScript = script;
        updateScriptStatusText(textViewScriptStatus);

        // Auto-capitalize title (first letter of each word)
        addTitleCapitalizationWatcher(editTitle);

        // Auto-capitalize description (first character only)
        addDescriptionCapitalizationWatcher(editDescription);

        // Scan buttons (simplified - just show toast for now, could be enhanced)
        buttonScanTitle.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scan feature coming soon", Toast.LENGTH_SHORT).show();
        });

        buttonScanDescription.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scan feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Edit Script button
        buttonEditScript.setOnClickListener(v -> {
            // Save current state before opening editor
            pendingCustomTitle = editTitle.getText() != null ? editTitle.getText().toString() : "";
            pendingCustomDescription = editDescription.getText() != null ? editDescription.getText().toString() : "";

            // Dismiss current dialog
            if (currentAddDialog != null) {
                currentAddDialog.dismiss();
            }

            // Open script editor
            if (navigationCallback != null) {
                navigationCallback.openScriptEditorForResult(pendingCustomScript);
            }
        });

        currentAddDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_add_custom_title)
                .setView(dialogView)
                .setPositiveButton(R.string.button_add, null) // Set to null, we'll override below
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                    // Clear pending state on cancel
                    clearPendingCustomState();
                })
                .create();

        currentAddDialog.setOnShowListener(dialog -> {
            currentAddDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String finalTitle = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
                String finalDescription = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

                // Validate title
                if (finalTitle.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.error_title_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate script
                if (pendingCustomScript.length() < 3) {
                    Toast.makeText(requireContext(), R.string.error_script_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if script contains recognized commands
                validateAndCreateEntry(finalTitle, finalDescription);
            });
        });

        currentAddDialog.show();
    }

    private void updateScriptStatusText(TextView textViewScriptStatus) {
        if (pendingCustomScript == null || pendingCustomScript.isEmpty()) {
            textViewScriptStatus.setText(R.string.script_status_empty);
        } else {
            textViewScriptStatus.setText(getString(R.string.script_status_defined, pendingCustomScript.length()));
        }
    }

    private void validateAndCreateEntry(String title, String description) {
        // Check if script contains recognized commands
        if (scriptAnalyzer != null && scriptAnalyzer.isInitialized()) {
            List<ScriptAnalyzer.FoundCommand> foundCommands = scriptAnalyzer.analyzeScript(pendingCustomScript);

            if (foundCommands.isEmpty()) {
                // Show warning dialog
                showUnrecognizedCommandWarning(title, description);
                return;
            }
        }

        // Script is valid or analyzer not ready, create entry
        createCustomEntry(title, description);
    }

    private void showUnrecognizedCommandWarning(String title, String description) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.warning_unrecognized_command_title)
                .setMessage(R.string.warning_unrecognized_command_message)
                .setPositiveButton(R.string.button_save_anyway, (dialog, which) -> {
                    createCustomEntry(title, description);
                })
                .setNegativeButton(R.string.button_edit_again, (dialog, which) -> {
                    // Save state and reopen editor
                    pendingCustomTitle = title;
                    pendingCustomDescription = description;
                    if (navigationCallback != null) {
                        navigationCallback.openScriptEditorForResult(pendingCustomScript);
                    }
                })
                .show();
    }

    private void createCustomEntry(String title, String description) {
        repository.createCustomEntry(title, description, pendingCustomScript);

        // Clear state and dismiss dialog
        clearPendingCustomState();
        if (currentAddDialog != null) {
            currentAddDialog.dismiss();
            currentAddDialog = null;
        }

        Toast.makeText(requireContext(), "Custom script entry created", Toast.LENGTH_SHORT).show();
    }

    private void clearPendingCustomState() {
        pendingCustomTitle = "";
        pendingCustomDescription = "";
        pendingCustomScript = "";
    }

    private void setupScriptEditorResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                CustomScriptFragment.EDITOR_RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean saved = result.getBoolean(CustomScriptFragment.EDITOR_RESULT_SAVED, false);
                    if (saved) {
                        String scriptContent = result.getString(CustomScriptFragment.EDITOR_RESULT_SCRIPT, "");
                        if (isEditMode) {
                            editingScript = scriptContent;
                        } else {
                            pendingCustomScript = scriptContent;
                        }
                    }

                    // Reopen the appropriate dialog based on mode
                    if (isEditMode) {
                        showEditCustomActionDialogWithState();
                    } else {
                        showAddCustomScriptDialogWithState(pendingCustomTitle, pendingCustomDescription, pendingCustomScript);
                    }
                }
        );
    }

    public boolean handleBackPress() {
        // No child fragments in launcher, return false to allow default behavior
        return false;
    }

    private void loadScriptAnalyzerCommands() {
        new Thread(() -> {
            try {
                java.io.InputStream is = requireContext().getAssets().open("zebra_documentation.json");
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();

                java.util.List<DocumentationCommand> commands = new java.util.ArrayList<>();
                org.json.JSONObject jsonObject = new org.json.JSONObject(sb.toString());

                // Parse ZPL commands
                org.json.JSONArray zplArray = jsonObject.optJSONArray("zpl_commands");
                if (zplArray != null) {
                    for (int i = 0; i < zplArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zplArray.getJSONObject(i)));
                    }
                }

                // Parse SGD commands
                org.json.JSONArray sgdArray = jsonObject.optJSONArray("sgd_commands");
                if (sgdArray != null) {
                    for (int i = 0; i < sgdArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(sgdArray.getJSONObject(i)));
                    }
                }

                // Parse ZBI commands
                org.json.JSONArray zbiArray = jsonObject.optJSONArray("zbi_commands");
                if (zbiArray != null) {
                    for (int i = 0; i < zbiArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zbiArray.getJSONObject(i)));
                    }
                }

                scriptAnalyzer.setCommandDatabase(commands);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Adds a TextWatcher to capitalize the first letter of each word in the title.
     */
    private void addTitleCapitalizationWatcher(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                String text = s.toString();
                String capitalized = capitalizeWords(text);

                if (!text.equals(capitalized)) {
                    isFormatting = true;
                    int cursorPos = editText.getSelectionStart();
                    s.replace(0, s.length(), capitalized);
                    // Restore cursor position
                    if (cursorPos <= capitalized.length()) {
                        editText.setSelection(cursorPos);
                    }
                    isFormatting = false;
                }
            }
        });
    }

    /**
     * Adds a TextWatcher to capitalize the first character of the description.
     */
    private void addDescriptionCapitalizationWatcher(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                String text = s.toString();
                if (text.length() > 0 && Character.isLowerCase(text.charAt(0))) {
                    isFormatting = true;
                    int cursorPos = editText.getSelectionStart();
                    String capitalized = Character.toUpperCase(text.charAt(0)) + text.substring(1);
                    s.replace(0, s.length(), capitalized);
                    // Restore cursor position
                    if (cursorPos <= capitalized.length()) {
                        editText.setSelection(cursorPos);
                    }
                    isFormatting = false;
                }
            }
        });
    }

    /**
     * Capitalizes the first letter of each word in a string.
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                capitalizeNext = false;
            }
        }

        return result.toString();
    }
}
