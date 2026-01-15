package com.zebra.zebraprintereuredsetup;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandSuggestionAdapter extends RecyclerView.Adapter<CommandSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(CommandSuggestion suggestion);
    }

    private final Context context;
    private final List<CommandSuggestion> allSuggestions = new ArrayList<>();
    private final List<CodeSnippet> allSnippets = new ArrayList<>();
    private final List<CommandSuggestion> filteredSuggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;
    private static final int MAX_SUGGESTIONS = 10;

    public CommandSuggestionAdapter(Context context) {
        this.context = context;
        // Load all snippets
        allSnippets.addAll(CodeSnippet.createAllSnippets());
    }

    public void setSuggestions(List<CommandSuggestion> suggestions) {
        allSuggestions.clear();
        allSuggestions.addAll(suggestions);
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void filter(String query) {
        filteredSuggestions.clear();
        Set<String> addedCommands = new HashSet<>();
        Set<String> addedSgdSegments = new HashSet<>();
        Set<String> addedSnippets = new HashSet<>();

        if (query != null && query.length() >= 1) {
            String lowerQuery = query.toLowerCase();
            String upperQuery = query.toUpperCase();
            int count = 0;
            boolean hasSgdMatches = false;

            // Track which actions are available for matching SGD commands
            boolean hasSetvar = false;
            boolean hasGetvar = false;
            boolean hasDo = false;

            // Collect suggestions by type
            List<CommandSuggestion> snippetSuggestions = new ArrayList<>();
            List<CommandSuggestion> sgdSuggestions = new ArrayList<>();
            List<CommandSuggestion> zplSuggestions = new ArrayList<>();
            List<CommandSuggestion> otherSuggestions = new ArrayList<>();

            // Check if this is a ZPL command query (starts with ^ or ~)
            boolean isZplQuery = query.startsWith("^") || query.startsWith("~");

            // First, find matching snippets (highest priority)
            for (CodeSnippet snippet : allSnippets) {
                String trigger = snippet.getTrigger();
                String triggerUpper = trigger.toUpperCase();

                // For ZPL snippets (starting with ^ or ~), match with or without the prefix
                if ("ZPL".equalsIgnoreCase(snippet.getType())) {
                    boolean matches = false;
                    // Match if query starts with ^ or ~ and trigger starts with query
                    if (isZplQuery && triggerUpper.startsWith(upperQuery)) {
                        matches = true;
                    }
                    // Also match if query is just letters and trigger (without prefix) starts with query
                    // e.g., query "XA" matches trigger "^XA"
                    else if (!isZplQuery && trigger.length() > 1) {
                        String triggerWithoutPrefix = triggerUpper.substring(1);
                        if (triggerWithoutPrefix.startsWith(upperQuery)) {
                            matches = true;
                        }
                    }

                    if (matches && !addedSnippets.contains(triggerUpper)) {
                        snippetSuggestions.add(CommandSuggestion.fromSnippet(snippet));
                        addedSnippets.add(triggerUpper);
                    }
                }
                // For ZBI snippets, show when query matches trigger or name
                else if ("ZBI".equalsIgnoreCase(snippet.getType())) {
                    if ((triggerUpper.startsWith(upperQuery) ||
                         snippet.getName().toUpperCase().contains(upperQuery)) &&
                        !addedSnippets.contains(triggerUpper)) {
                        snippetSuggestions.add(CommandSuggestion.fromSnippet(snippet));
                        addedSnippets.add(triggerUpper);
                    }
                }
                // For SGD snippets
                else if ("SGD".equalsIgnoreCase(snippet.getType())) {
                    if (snippet.matchesQuery(query) && !addedSnippets.contains(triggerUpper)) {
                        snippetSuggestions.add(CommandSuggestion.fromSnippet(snippet));
                        addedSnippets.add(triggerUpper);
                    }
                }
            }

            // Then find matching commands (including raw versions of snippets)
            for (CommandSuggestion suggestion : allSuggestions) {
                String command = suggestion.getCommand();
                if (command == null) continue;

                // For SGD commands, use hierarchical filtering
                if ("SGD".equalsIgnoreCase(suggestion.getType())) {
                    String lowerCommand = command.toLowerCase();
                    if (!lowerCommand.startsWith(lowerQuery)) {
                        continue;
                    }

                    // Track available actions from matching commands
                    if (suggestion.supportsAction("setvar")) hasSetvar = true;
                    if (suggestion.supportsAction("getvar")) hasGetvar = true;
                    if (suggestion.supportsAction("do")) hasDo = true;

                    String nextSegment = extractNextSegment(lowerCommand, lowerQuery);
                    if (nextSegment == null || nextSegment.equals(lowerQuery) || addedSgdSegments.contains(nextSegment)) {
                        continue;
                    }

                    boolean hasMoreSegments = hasMoreSegmentsAfter(nextSegment);
                    String segmentToInsert = nextSegment.substring(lowerQuery.length());
                    if (hasMoreSegments) {
                        segmentToInsert += ".";
                    }

                    sgdSuggestions.add(new CommandSuggestion(
                            nextSegment,
                            hasMoreSegments ? "..." : suggestion.getName(),
                            segmentToInsert,
                            "SGD"
                    ));
                    addedSgdSegments.add(nextSegment);
                    hasSgdMatches = true;
                } else if ("ZPL".equalsIgnoreCase(suggestion.getType())) {
                    // For ZPL commands, match by command prefix (case insensitive)
                    String upperCommand = command.toUpperCase();

                    // Check if command matches query (with or without ^ prefix)
                    boolean matches = false;
                    if (isZplQuery && upperCommand.startsWith(upperQuery)) {
                        matches = true;
                    } else if (!isZplQuery && command.length() > 1) {
                        // Match without prefix: "XA" matches "^XA"
                        String commandWithoutPrefix = upperCommand.substring(1);
                        if (commandWithoutPrefix.startsWith(upperQuery)) {
                            matches = true;
                        }
                    }

                    if (matches && !addedCommands.contains(upperCommand)) {
                        // Get the format for display
                        String format = suggestion.getFormat();
                        String displayFormat = (format != null && !format.isEmpty()) ? format : command;

                        // Add "(command)" suffix if a snippet exists for this command
                        String displayName = suggestion.getName();
                        if (addedSnippets.contains(upperCommand)) {
                            displayName = suggestion.getName() + " (command)";
                        }

                        zplSuggestions.add(new CommandSuggestion(
                                command,
                                displayName,
                                displayFormat,  // Use format as insert text
                                "ZPL"
                        ));
                        addedCommands.add(upperCommand);
                    }
                } else {
                    // For ZBI and other, use regular filtering
                    String key = command.toUpperCase();

                    if (suggestion.matchesQuery(query) && !addedCommands.contains(key)) {
                        // Add "(command)" suffix if a snippet exists for this command
                        if (addedSnippets.contains(key)) {
                            otherSuggestions.add(new CommandSuggestion(
                                    suggestion.getCommand(),
                                    suggestion.getName() + " (command)",
                                    suggestion.getFormat(),
                                    suggestion.getType()
                            ));
                        } else {
                            otherSuggestions.add(suggestion);
                        }
                        addedCommands.add(key);
                    }
                }
            }

            // Add snippets first (highest priority)
            for (CommandSuggestion snippet : snippetSuggestions) {
                if (count >= MAX_SUGGESTIONS) break;
                filteredSuggestions.add(snippet);
                count++;
            }

            // If there are SGD matches, add priority suggestions for available actions
            if (hasSgdMatches && !sgdSuggestions.isEmpty()) {
                // Add "! U1 setvar" wrapper if setvar is available
                if (hasSetvar && count < MAX_SUGGESTIONS) {
                    filteredSuggestions.add(new CommandSuggestion(
                            "! U1 setvar \"" + query,
                            "SGD Set Variable",
                            "! U1 setvar \"" + query,
                            "SGD"
                    ));
                    count++;
                }

                // Add "! U1 getvar" wrapper if getvar is available
                if (hasGetvar && count < MAX_SUGGESTIONS) {
                    filteredSuggestions.add(new CommandSuggestion(
                            "! U1 getvar \"" + query,
                            "SGD Get Variable",
                            "! U1 getvar \"" + query,
                            "SGD"
                    ));
                    count++;
                }

                // Add "! U1 do" wrapper if do is available
                if (hasDo && count < MAX_SUGGESTIONS) {
                    filteredSuggestions.add(new CommandSuggestion(
                            "! U1 do \"" + query,
                            "SGD Execute Action",
                            "! U1 do \"" + query,
                            "SGD"
                    ));
                    count++;
                }
            }

            // Add ZPL suggestions
            for (CommandSuggestion zpl : zplSuggestions) {
                if (count >= MAX_SUGGESTIONS) break;
                filteredSuggestions.add(zpl);
                count++;
            }

            // Add SGD suggestions
            for (CommandSuggestion sgd : sgdSuggestions) {
                if (count >= MAX_SUGGESTIONS) break;
                filteredSuggestions.add(sgd);
                count++;
            }

            // Add other suggestions
            for (CommandSuggestion other : otherSuggestions) {
                if (count >= MAX_SUGGESTIONS) break;
                filteredSuggestions.add(other);
                count++;
            }
        }

        notifyDataSetChanged();
    }

    public void setSpecialSuggestions(List<CommandSuggestion> suggestions) {
        filteredSuggestions.clear();
        filteredSuggestions.addAll(suggestions);
        notifyDataSetChanged();
    }

    public void filterSgdOnly(String query) {
        filteredSuggestions.clear();
        Set<String> addedSegments = new HashSet<>();

        if (query == null || query.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        String lowerQuery = query.toLowerCase();
        int count = 0;

        // Collect all unique next segments
        for (CommandSuggestion suggestion : allSuggestions) {
            if (!"SGD".equalsIgnoreCase(suggestion.getType())) {
                continue;
            }

            String command = suggestion.getCommand();
            if (command == null || command.isEmpty()) continue;

            String lowerCommand = command.toLowerCase();

            // Check if command starts with the query
            if (!lowerCommand.startsWith(lowerQuery)) {
                continue;
            }

            // Get the next segment to suggest
            String nextSegment = extractNextSegment(lowerCommand, lowerQuery);
            if (nextSegment == null || nextSegment.equals(lowerQuery)) {
                continue;
            }

            // Skip if already added
            if (addedSegments.contains(nextSegment)) {
                continue;
            }

            if (count >= MAX_SUGGESTIONS) {
                break;
            }

            // Check if there are more segments after this one
            boolean hasMoreSegments = hasMoreSegmentsAfter(nextSegment);

            // Calculate what to insert (only the new part, not what user already typed)
            String segmentToInsert = nextSegment.substring(lowerQuery.length());
            if (hasMoreSegments) {
                segmentToInsert += ".";
            }

            filteredSuggestions.add(new CommandSuggestion(
                    nextSegment,  // Display the path up to this segment
                    hasMoreSegments ? "..." : suggestion.getName(),
                    segmentToInsert,  // Insert only the new part
                    "SGD"
            ));
            addedSegments.add(nextSegment);
            count++;
        }

        notifyDataSetChanged();
    }

    /**
     * Extract the next segment from the command based on the query.
     * Examples:
     *   query="wlan", command="wlan.11ac.80mhz_enable" → "wlan.11ac"
     *   query="wlan.", command="wlan.11ac.80mhz_enable" → "wlan.11ac"
     *   query="wlan.11", command="wlan.11ac.80mhz_enable" → "wlan.11ac"
     *   query="wlan.11ac", command="wlan.11ac.80mhz_enable" → "wlan.11ac.80mhz_enable"
     */
    private String extractNextSegment(String command, String query) {
        // Command must be longer than query
        if (command.length() <= query.length()) {
            return null;
        }

        // Split command into segments
        String[] segments = command.split("\\.");

        // Build up path segment by segment until we exceed the query
        StringBuilder currentPath = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (currentPath.length() > 0) {
                currentPath.append(".");
            }
            currentPath.append(segments[i]);

            String path = currentPath.toString();

            // If this path is longer than the query and starts with query, return it
            if (path.length() > query.length() && path.startsWith(query)) {
                return path;
            }
        }

        // If we get here, return the full command if it's longer
        if (command.length() > query.length() && command.startsWith(query)) {
            return command;
        }

        return null;
    }

    /**
     * Check if there are more segments after the given path.
     */
    private boolean hasMoreSegmentsAfter(String path) {
        String lowerPath = path.toLowerCase();
        for (CommandSuggestion suggestion : allSuggestions) {
            if (!"SGD".equalsIgnoreCase(suggestion.getType())) {
                continue;
            }
            String command = suggestion.getCommand();
            if (command != null) {
                String lowerCommand = command.toLowerCase();
                // Check if any command starts with this path followed by a dot
                if (lowerCommand.startsWith(lowerPath + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSuggestions() {
        return !filteredSuggestions.isEmpty();
    }

    public int getSuggestionsCount() {
        return filteredSuggestions.size();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        CommandSuggestion suggestion = filteredSuggestions.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return filteredSuggestions.size();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewType;
        private final TextView textViewCommand;
        private final TextView textViewFormat;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewCommand = itemView.findViewById(R.id.textViewCommand);
            textViewFormat = itemView.findViewById(R.id.textViewFormat);
        }

        void bind(CommandSuggestion suggestion) {
            textViewType.setText(suggestion.getType());
            setBadgeColor(suggestion.getType());

            textViewCommand.setText(suggestion.getCommand());

            String format = suggestion.getFormat();
            if (format != null && !format.isEmpty() && !format.equals(suggestion.getCommand())) {
                textViewFormat.setVisibility(View.VISIBLE);
                textViewFormat.setText(format);
            } else {
                textViewFormat.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuggestionClick(suggestion);
                }
            });
        }

        private void setBadgeColor(String type) {
            int colorRes;
            switch (type.toUpperCase()) {
                case "ZPL":
                    colorRes = R.color.badge_zpl;
                    break;
                case "SGD":
                    colorRes = R.color.badge_sgd;
                    break;
                case "ZBI":
                    colorRes = R.color.badge_zbi;
                    break;
                default:
                    colorRes = R.color.badge_default;
            }
            GradientDrawable background = (GradientDrawable) textViewType.getBackground();
            background.setColor(ContextCompat.getColor(context, colorRes));
        }
    }
}
