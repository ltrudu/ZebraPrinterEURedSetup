package com.zebra.zebraprintereuredsetup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DocumentationCommand {

    public enum CommandType {
        ZPL, ZBI, SGD, SNIPPET
    }

    // Common fields
    private String command;
    private String name;
    private String description;
    private String format;
    private int page;
    private CommandType type;

    // ZPL specific
    private List<Parameter> parameters;

    // ZBI specific
    private String returns;
    private String keywordType;

    // SGD specific
    private List<String> supportedActions;
    private List<String> possibleValues;
    private String defaultValue;

    // Snippet specific
    private String example;

    // Parameter inner class for ZPL commands
    public static class Parameter {
        private String name;
        private String description;
        private String values;
        private String defaultValue;

        public Parameter(String name, String description, String values, String defaultValue) {
            this.name = name;
            this.description = description;
            this.values = values;
            this.defaultValue = defaultValue;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getValues() { return values; }
        public String getDefaultValue() { return defaultValue; }
    }

    public static DocumentationCommand fromJson(JSONObject json) throws JSONException {
        DocumentationCommand cmd = new DocumentationCommand();

        cmd.command = json.optString("command", "");
        cmd.name = json.optString("name", "");
        cmd.description = json.optString("description", "");
        cmd.format = json.optString("format", "");
        cmd.page = json.optInt("page", 0);

        String typeStr = json.optString("type", "").toLowerCase();
        switch (typeStr) {
            case "zpl":
                cmd.type = CommandType.ZPL;
                cmd.parseZplFields(json);
                break;
            case "zbi":
                cmd.type = CommandType.ZBI;
                cmd.parseZbiFields(json);
                break;
            case "sgd":
                cmd.type = CommandType.SGD;
                cmd.parseSgdFields(json);
                break;
            case "snippet":
                cmd.type = CommandType.SNIPPET;
                cmd.parseSnippetFields(json);
                break;
            default:
                cmd.type = CommandType.ZPL;
        }

        return cmd;
    }

    private void parseZplFields(JSONObject json) throws JSONException {
        parameters = new ArrayList<>();
        JSONArray paramsArray = json.optJSONArray("parameters");
        if (paramsArray != null) {
            for (int i = 0; i < paramsArray.length(); i++) {
                JSONObject paramJson = paramsArray.getJSONObject(i);
                Parameter param = new Parameter(
                        paramJson.optString("name", ""),
                        paramJson.optString("description", ""),
                        paramJson.optString("values", ""),
                        paramJson.optString("default", "")
                );
                parameters.add(param);
            }
        }
    }

    private void parseZbiFields(JSONObject json) {
        returns = json.optString("returns", "N/A");
        keywordType = json.optString("keyword_type", "statement");
    }

    private void parseSgdFields(JSONObject json) throws JSONException {
        supportedActions = new ArrayList<>();
        JSONArray actionsArray = json.optJSONArray("supported_actions");
        if (actionsArray != null) {
            for (int i = 0; i < actionsArray.length(); i++) {
                supportedActions.add(actionsArray.getString(i));
            }
        }

        possibleValues = new ArrayList<>();
        JSONArray valuesArray = json.optJSONArray("possible_values");
        if (valuesArray != null) {
            for (int i = 0; i < valuesArray.length(); i++) {
                possibleValues.add(valuesArray.getString(i));
            }
        }

        defaultValue = json.optString("default", "NA");
    }

    private void parseSnippetFields(JSONObject json) throws JSONException {
        example = json.optString("example", "");

        // Snippets can also have parameters like ZPL
        parameters = new ArrayList<>();
        JSONArray paramsArray = json.optJSONArray("parameters");
        if (paramsArray != null) {
            for (int i = 0; i < paramsArray.length(); i++) {
                JSONObject paramJson = paramsArray.getJSONObject(i);
                Parameter param = new Parameter(
                        paramJson.optString("name", ""),
                        paramJson.optString("description", ""),
                        paramJson.optString("values", ""),
                        paramJson.optString("default", "")
                );
                parameters.add(param);
            }
        }
    }

    // Getters
    public String getCommand() { return command; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getFormat() { return format; }
    public int getPage() { return page; }
    public CommandType getType() { return type; }
    public List<Parameter> getParameters() { return parameters; }
    public String getReturns() { return returns; }
    public String getKeywordType() { return keywordType; }
    public List<String> getSupportedActions() { return supportedActions; }
    public List<String> getPossibleValues() { return possibleValues; }
    public String getDefaultValue() { return defaultValue; }
    public String getExample() { return example; }

    public String getTypeString() {
        if (type == null) return "ZPL";
        return type.name();
    }

    // Check if command matches search query
    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return (command != null && command.toLowerCase().contains(lowerQuery)) ||
                (name != null && name.toLowerCase().contains(lowerQuery)) ||
                (description != null && description.toLowerCase().contains(lowerQuery)) ||
                (format != null && format.toLowerCase().contains(lowerQuery));
    }

    // Get formatted parameters string for display
    public String getParametersDisplayString() {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            if (i > 0) sb.append("\n\n");
            sb.append(param.getName());
            if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                sb.append("\n").append(param.getDescription());
            }
        }
        return sb.toString();
    }

    // Get formatted actions string for display
    public String getActionsDisplayString() {
        if (supportedActions == null || supportedActions.isEmpty()) {
            return "";
        }
        return String.join(", ", supportedActions);
    }

    // Get formatted values string for display
    public String getValuesDisplayString() {
        if (possibleValues == null || possibleValues.isEmpty()) {
            return "";
        }
        return String.join("\n", possibleValues);
    }
}
