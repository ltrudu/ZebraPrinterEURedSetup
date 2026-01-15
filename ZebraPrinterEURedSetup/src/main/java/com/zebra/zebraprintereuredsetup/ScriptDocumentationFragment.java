package com.zebra.zebraprintereuredsetup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptDocumentationFragment extends Fragment {

    private EditText editTextSearch;
    private CheckBox checkBoxZpl;
    private CheckBox checkBoxSgd;
    private CheckBox checkBoxZbi;
    private CheckBox checkBoxSnippets;
    private TextView textViewResultsCount;
    private RecyclerView recyclerViewCommands;
    private CircularProgressIndicator progressIndicator;
    private LinearLayout layoutEmptyState;
    private TextView textViewEmptyMessage;

    private DocumentationAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Debounce search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 300;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_script_documentation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        loadDocumentation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null);
    }

    private void setupViews(View view) {
        // Setup toolbar with back navigation
        MaterialToolbar toolbar = view.findViewById(R.id.toolbarDocumentation);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        editTextSearch = view.findViewById(R.id.editTextSearch);
        checkBoxZpl = view.findViewById(R.id.checkBoxZpl);
        checkBoxSgd = view.findViewById(R.id.checkBoxSgd);
        checkBoxZbi = view.findViewById(R.id.checkBoxZbi);
        checkBoxSnippets = view.findViewById(R.id.checkBoxSnippets);
        textViewResultsCount = view.findViewById(R.id.textViewResultsCount);
        recyclerViewCommands = view.findViewById(R.id.recyclerViewCommands);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        textViewEmptyMessage = view.findViewById(R.id.textViewEmptyMessage);

        // Setup RecyclerView
        adapter = new DocumentationAdapter(requireContext());
        recyclerViewCommands.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCommands.setAdapter(adapter);

        // Setup search listener with debounce
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                debounceSearch(s.toString());
            }
        });

        // Search action on keyboard
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Setup filter listeners
        CheckBox.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();
        checkBoxZpl.setOnCheckedChangeListener(filterListener);
        checkBoxSgd.setOnCheckedChangeListener(filterListener);
        checkBoxZbi.setOnCheckedChangeListener(filterListener);
        checkBoxSnippets.setOnCheckedChangeListener(filterListener);
    }

    private void debounceSearch(String query) {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
    }

    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        adapter.setSearchQuery(query);
        updateResultsCount();
        updateEmptyState();
    }

    private void applyFilters() {
        adapter.setFilters(
                checkBoxZpl.isChecked(),
                checkBoxSgd.isChecked(),
                checkBoxZbi.isChecked(),
                checkBoxSnippets.isChecked()
        );
        updateResultsCount();
        updateEmptyState();
    }

    private void updateResultsCount() {
        int count = adapter.getFilteredCount();
        textViewResultsCount.setText(getString(R.string.label_commands_found, count));
    }

    private void updateEmptyState() {
        int count = adapter.getFilteredCount();
        if (count == 0) {
            recyclerViewCommands.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCommands.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadDocumentation() {
        showLoading(true);

        executor.execute(() -> {
            try {
                // Read JSON from assets
                InputStream is = requireContext().getAssets().open("zebra_documentation.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();

                // Parse JSON
                JSONObject root = new JSONObject(sb.toString());
                List<DocumentationCommand> commands = new ArrayList<>();

                // Parse ZPL commands
                JSONArray zplArray = root.optJSONArray("zpl_commands");
                if (zplArray != null) {
                    for (int i = 0; i < zplArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zplArray.getJSONObject(i)));
                    }
                }

                // Parse ZBI commands
                JSONArray zbiArray = root.optJSONArray("zbi_commands");
                if (zbiArray != null) {
                    for (int i = 0; i < zbiArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(zbiArray.getJSONObject(i)));
                    }
                }

                // Parse SGD commands
                JSONArray sgdArray = root.optJSONArray("sgd_commands");
                if (sgdArray != null) {
                    for (int i = 0; i < sgdArray.length(); i++) {
                        commands.add(DocumentationCommand.fromJson(sgdArray.getJSONObject(i)));
                    }
                }

                // Load snippets from separate file
                try {
                    InputStream snippetsIs = requireContext().getAssets().open("snippets_documentation.json");
                    BufferedReader snippetsReader = new BufferedReader(new InputStreamReader(snippetsIs));
                    StringBuilder snippetsSb = new StringBuilder();
                    String snippetsLine;
                    while ((snippetsLine = snippetsReader.readLine()) != null) {
                        snippetsSb.append(snippetsLine);
                    }
                    snippetsReader.close();
                    snippetsIs.close();

                    JSONObject snippetsRoot = new JSONObject(snippetsSb.toString());
                    JSONArray snippetsArray = snippetsRoot.optJSONArray("snippets");
                    if (snippetsArray != null) {
                        for (int i = 0; i < snippetsArray.length(); i++) {
                            commands.add(DocumentationCommand.fromJson(snippetsArray.getJSONObject(i)));
                        }
                    }
                } catch (Exception e) {
                    // Snippets file is optional, continue without it
                    e.printStackTrace();
                }

                // Update UI on main thread
                mainHandler.post(() -> {
                    adapter.setCommands(commands);
                    showLoading(false);
                    updateResultsCount();
                    updateEmptyState();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    showLoading(false);
                    textViewEmptyMessage.setText(getString(R.string.error_loading_documentation, e.getMessage()));
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewCommands.setVisibility(View.GONE);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewCommands.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}
