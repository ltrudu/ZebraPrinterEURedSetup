package com.zebra.zebraprintereuredsetup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandSuggestion {
    private final String command;
    private final String name;
    private final String format;
    private final String type;
    private final Set<String> supportedActions;
    private final CodeSnippet snippet;  // Optional snippet reference

    public CommandSuggestion(String command, String name, String format, String type) {
        this(command, name, format, type, null, null);
    }

    public CommandSuggestion(String command, String name, String format, String type, Set<String> supportedActions) {
        this(command, name, format, type, supportedActions, null);
    }

    public CommandSuggestion(String command, String name, String format, String type, Set<String> supportedActions, CodeSnippet snippet) {
        this.command = command;
        this.name = name;
        this.format = format;
        this.type = type;
        this.supportedActions = supportedActions != null ? supportedActions : new HashSet<>();
        this.snippet = snippet;
    }

    /**
     * Create a CommandSuggestion from a CodeSnippet.
     */
    public static CommandSuggestion fromSnippet(CodeSnippet snippet) {
        return new CommandSuggestion(
                snippet.getTrigger(),
                snippet.getName() + " (snippet)",
                snippet.getInsertText(),
                snippet.getType(),
                null,
                snippet
        );
    }

    public String getCommand() { return command; }
    public String getName() { return name; }
    public String getFormat() { return format; }
    public String getType() { return type; }
    public Set<String> getSupportedActions() { return supportedActions; }
    public CodeSnippet getSnippet() { return snippet; }

    public boolean isSnippet() {
        return snippet != null;
    }

    public boolean supportsAction(String action) {
        return supportedActions != null && supportedActions.contains(action.toLowerCase());
    }

    public String getDisplayText() {
        return command;
    }

    public String getInsertText() {
        // For snippets, return the template without cursor marker
        if (snippet != null) {
            return snippet.getInsertText();
        }
        // Return the format if available, otherwise the command
        return (format != null && !format.isEmpty()) ? format : command;
    }

    /**
     * Get cursor offset for snippets (position where cursor should be placed).
     */
    public int getCursorOffset() {
        if (snippet != null) {
            return snippet.getCursorOffset();
        }
        return getInsertText().length();  // Default: end of inserted text
    }

    public boolean matchesQuery(String query) {
        if (query == null || query.isEmpty()) {
            return false;
        }
        String lowerQuery = query.toLowerCase();
        return (command != null && command.toLowerCase().startsWith(lowerQuery)) ||
               (name != null && name.toLowerCase().contains(lowerQuery));
    }

    public static List<CommandSuggestion> parseFromJson(String jsonString) {
        List<CommandSuggestion> suggestions = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);

            // Parse ZPL commands
            JSONArray zplArray = root.optJSONArray("zpl_commands");
            if (zplArray != null) {
                for (int i = 0; i < zplArray.length(); i++) {
                    JSONObject cmd = zplArray.getJSONObject(i);
                    suggestions.add(new CommandSuggestion(
                            cmd.optString("command", ""),
                            cmd.optString("name", ""),
                            cmd.optString("format", ""),
                            "ZPL"
                    ));
                }
            }

            // Parse ZBI commands
            JSONArray zbiArray = root.optJSONArray("zbi_commands");
            if (zbiArray != null) {
                for (int i = 0; i < zbiArray.length(); i++) {
                    JSONObject cmd = zbiArray.getJSONObject(i);
                    suggestions.add(new CommandSuggestion(
                            cmd.optString("command", ""),
                            cmd.optString("name", ""),
                            cmd.optString("format", ""),
                            "ZBI"
                    ));
                }
            }

            // Parse SGD commands
            JSONArray sgdArray = root.optJSONArray("sgd_commands");
            if (sgdArray != null) {
                for (int i = 0; i < sgdArray.length(); i++) {
                    JSONObject cmd = sgdArray.getJSONObject(i);

                    // Parse supported_actions
                    Set<String> actions = new HashSet<>();
                    JSONArray actionsArray = cmd.optJSONArray("supported_actions");
                    if (actionsArray != null) {
                        for (int j = 0; j < actionsArray.length(); j++) {
                            actions.add(actionsArray.optString(j, "").toLowerCase());
                        }
                    }

                    suggestions.add(new CommandSuggestion(
                            cmd.optString("command", ""),
                            cmd.optString("name", ""),
                            cmd.optString("format", ""),
                            "SGD",
                            actions
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return suggestions;
    }
}
