package com.zebra.zebraprintereuredsetup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommandSuggestion {
    private final String command;
    private final String name;
    private final String format;
    private final String type;

    public CommandSuggestion(String command, String name, String format, String type) {
        this.command = command;
        this.name = name;
        this.format = format;
        this.type = type;
    }

    public String getCommand() { return command; }
    public String getName() { return name; }
    public String getFormat() { return format; }
    public String getType() { return type; }

    public String getDisplayText() {
        return command;
    }

    public String getInsertText() {
        // Return the format if available, otherwise the command
        return (format != null && !format.isEmpty()) ? format : command;
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
                    suggestions.add(new CommandSuggestion(
                            cmd.optString("command", ""),
                            cmd.optString("name", ""),
                            cmd.optString("format", ""),
                            "SGD"
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return suggestions;
    }
}
