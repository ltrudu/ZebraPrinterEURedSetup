package com.zebra.zebraprintereuredsetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes script text to find ZPL, ZBI, and SGD commands.
 * Used for syntax highlighting and documentation lookup.
 */
public class ScriptAnalyzer {

    public enum CommandType {
        ZPL, ZBI, SGD
    }

    /**
     * Represents a command found in the script text.
     */
    public static class FoundCommand {
        public int startIndex;
        public int endIndex;
        public String commandText;
        public CommandType type;
        public String normalizedCommand; // For lookup in documentation

        public FoundCommand(int start, int end, String text, CommandType type, String normalized) {
            this.startIndex = start;
            this.endIndex = end;
            this.commandText = text;
            this.type = type;
            this.normalizedCommand = normalized;
        }
    }

    // Regex patterns
    // ZPL commands: ^XX or ~XX followed by parameters until next ^ or ~ or newline
    // This captures the full command with parameters (e.g., ^FO100,200 or ^A0N,50,50)
    private static final Pattern ZPL_FULL_COMMAND_PATTERN = Pattern.compile(
            "([\\^~][A-Z@][A-Z0-9]?)([^\\^~\\n\\r]*)", Pattern.CASE_INSENSITIVE);

    // Pattern to extract just the base command (^XX or ~XX) for documentation lookup
    private static final Pattern ZPL_BASE_COMMAND_PATTERN = Pattern.compile(
            "^([\\^~][A-Z@][A-Z0-9]?)", Pattern.CASE_INSENSITIVE);

    // SGD paths in quotes: "device.languages", "ip.dhcp.enable"
    private static final Pattern SGD_PATH_PATTERN = Pattern.compile("\"([a-z0-9_]+\\.[a-z0-9_.]+)\"", Pattern.CASE_INSENSITIVE);

    // SGD command prefix pattern: ! U1 followed by action
    private static final Pattern SGD_ACTION_PATTERN = Pattern.compile("!\\s*U1\\s+(getvar|setvar|do)", Pattern.CASE_INSENSITIVE);

    // SGD full command pattern: ! U1 action "param1" "param2" (captures both quoted parameters)
    private static final Pattern SGD_FULL_COMMAND_PATTERN = Pattern.compile(
            "!\\s*U1\\s+(getvar|setvar|do)\\s+\"([^\"]*)\"\\s*\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);

    // ZBI keywords (common BASIC-like keywords used in ZBI)
    private static final Set<String> ZBI_KEYWORDS = new HashSet<>(Arrays.asList(
            // Control flow
            "IF", "THEN", "ELSE", "ELSEIF", "END_IF", "END IF",
            "DO", "WHILE", "UNTIL", "LOOP", "EXIT",
            "FOR", "TO", "STEP", "NEXT",
            "GOTO", "GOSUB", "RETURN", "SUB", "END",
            "ON", "ERROR", "RESUME",
            // Variable declarations
            "DIM", "LET", "CONST",
            // Input/Output
            "PRINT", "INPUT", "OPEN", "CLOSE", "GET", "PUT",
            "READ", "WRITE", "LINE INPUT",
            // Functions/Commands
            "SLEEP", "AUTONUM", "REM", "DATA", "RESTORE",
            // Port operations
            "NAME", "AS", "OUTPUT", "APPEND",
            // Printer specific
            "RESTART", "PRTSTATUS$", "GETSETTING$", "SETSETTING"
    ));

    // Word boundary pattern for ZBI keywords (to avoid matching partial words)
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\b([A-Z_][A-Z0-9_]*\\$?)\\b", Pattern.CASE_INSENSITIVE);

    // Command lookup maps
    private Map<String, DocumentationCommand> zplLookup = new HashMap<>();
    private Map<String, DocumentationCommand> zbiLookup = new HashMap<>();
    private Map<String, DocumentationCommand> sgdLookup = new HashMap<>();
    private boolean initialized = false;

    /**
     * Set the command database for documentation lookup.
     */
    public void setCommandDatabase(List<DocumentationCommand> commands) {
        zplLookup.clear();
        zbiLookup.clear();
        sgdLookup.clear();

        for (DocumentationCommand cmd : commands) {
            String key = cmd.getCommand().toUpperCase();
            switch (cmd.getType()) {
                case ZPL:
                    zplLookup.put(key, cmd);
                    break;
                case ZBI:
                    zbiLookup.put(key, cmd);
                    break;
                case SGD:
                    // SGD commands use lowercase dot notation
                    sgdLookup.put(cmd.getCommand().toLowerCase(), cmd);
                    break;
            }
        }
        initialized = true;
    }

    /**
     * Check if the analyzer has been initialized with command data.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Analyze script text and return all found commands.
     */
    public List<FoundCommand> analyzeScript(String scriptText) {
        List<FoundCommand> foundCommands = new ArrayList<>();

        if (scriptText == null || scriptText.isEmpty()) {
            return foundCommands;
        }

        // Track positions to avoid overlapping matches
        Set<Integer> usedPositions = new HashSet<>();

        // Find ZPL commands first (highest priority)
        findZplCommands(scriptText, foundCommands, usedPositions);

        // Find SGD paths and actions
        findSgdCommands(scriptText, foundCommands, usedPositions);

        // Find ZBI keywords (avoid positions already used)
        findZbiKeywords(scriptText, foundCommands, usedPositions);

        // Sort by start position
        foundCommands.sort((a, b) -> Integer.compare(a.startIndex, b.startIndex));

        return foundCommands;
    }

    private void findZplCommands(String text, List<FoundCommand> commands, Set<Integer> usedPositions) {
        Matcher matcher = ZPL_FULL_COMMAND_PATTERN.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            String baseCommand = matcher.group(1); // The ^XX or ~XX part
            String params = matcher.group(2);      // Everything after until next ^ or ~

            // Calculate end position - include parameters but trim trailing whitespace
            String fullMatch = baseCommand + params;
            String trimmedMatch = fullMatch.stripTrailing();
            int end = start + trimmedMatch.length();

            // Make sure we have at least the base command
            if (end <= start) {
                end = start + baseCommand.length();
                trimmedMatch = baseCommand;
            }

            // Skip if position already used
            if (isPositionUsed(start, end, usedPositions)) {
                continue;
            }

            // Normalize to uppercase base command for documentation lookup (e.g., ^NC from ^NC2)
            String normalized = baseCommand.toUpperCase();

            // Highlight the full command with parameters
            commands.add(new FoundCommand(start, end, trimmedMatch, CommandType.ZPL, normalized));
            markPositionUsed(start, end, usedPositions);
        }
    }

    private void findSgdCommands(String text, List<FoundCommand> commands, Set<Integer> usedPositions) {
        // First, try to match full SGD commands with both parameters: ! U1 action "param1" "param2"
        Matcher fullMatcher = SGD_FULL_COMMAND_PATTERN.matcher(text);
        while (fullMatcher.find()) {
            int fullStart = fullMatcher.start();
            int fullEnd = fullMatcher.end();

            if (isPositionUsed(fullStart, fullEnd, usedPositions)) {
                continue;
            }

            // Highlight the entire command as SGD
            String action = fullMatcher.group(1).toLowerCase();
            String param1 = fullMatcher.group(2);
            String param2 = fullMatcher.group(3);

            // Use param1 (the SGD path) as the normalized command for documentation lookup
            commands.add(new FoundCommand(fullStart, fullEnd, fullMatcher.group(), CommandType.SGD, param1.toLowerCase()));
            markPositionUsed(fullStart, fullEnd, usedPositions);
        }

        // Find remaining SGD action keywords (getvar, setvar, do) that weren't part of full commands
        Matcher actionMatcher = SGD_ACTION_PATTERN.matcher(text);
        while (actionMatcher.find()) {
            int start = actionMatcher.start();
            int end = actionMatcher.end();

            if (isPositionUsed(start, end, usedPositions)) {
                continue;
            }

            // Highlight the entire "! U1 action" part
            commands.add(new FoundCommand(start, end, actionMatcher.group(), CommandType.SGD, "SGD_PREFIX"));
            markPositionUsed(start, end, usedPositions);
        }

        // Find remaining SGD paths in quotes that weren't part of full commands
        Matcher pathMatcher = SGD_PATH_PATTERN.matcher(text);
        while (pathMatcher.find()) {
            int start = pathMatcher.start();
            int end = pathMatcher.end();

            if (isPositionUsed(start, end, usedPositions)) {
                continue;
            }

            String path = pathMatcher.group(1).toLowerCase();

            // Highlight ALL SGD paths (documentation lookup is done separately for clicks)
            commands.add(new FoundCommand(start, end, pathMatcher.group(), CommandType.SGD, path));
            markPositionUsed(start, end, usedPositions);
        }
    }

    private void findZbiKeywords(String text, List<FoundCommand> commands, Set<Integer> usedPositions) {
        Matcher matcher = WORD_BOUNDARY.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (isPositionUsed(start, end, usedPositions)) {
                continue;
            }

            String word = matcher.group().toUpperCase();

            // Check if it's a ZBI keyword
            if (ZBI_KEYWORDS.contains(word)) {
                String normalized = word;

                // Check if in documentation, fall back to keyword name
                commands.add(new FoundCommand(start, end, matcher.group(), CommandType.ZBI, normalized));
                markPositionUsed(start, end, usedPositions);
            }
        }
    }

    private boolean isPositionUsed(int start, int end, Set<Integer> usedPositions) {
        for (int i = start; i < end; i++) {
            if (usedPositions.contains(i)) {
                return true;
            }
        }
        return false;
    }

    private void markPositionUsed(int start, int end, Set<Integer> usedPositions) {
        for (int i = start; i < end; i++) {
            usedPositions.add(i);
        }
    }

    /**
     * Lookup documentation for a command.
     */
    public DocumentationCommand lookupCommand(String normalizedCommand, CommandType type) {
        if (normalizedCommand == null) return null;

        switch (type) {
            case ZPL:
                return zplLookup.get(normalizedCommand.toUpperCase());
            case ZBI:
                return zbiLookup.get(normalizedCommand.toUpperCase());
            case SGD:
                return sgdLookup.get(normalizedCommand.toLowerCase());
            default:
                return null;
        }
    }

    /**
     * Get all command lookup maps combined.
     */
    public Map<String, DocumentationCommand> getCommandLookup() {
        Map<String, DocumentationCommand> combined = new HashMap<>();
        combined.putAll(zplLookup);
        combined.putAll(zbiLookup);
        combined.putAll(sgdLookup);
        return combined;
    }

    /**
     * Get ZPL lookup map.
     */
    public Map<String, DocumentationCommand> getZplLookup() {
        return zplLookup;
    }

    /**
     * Get ZBI lookup map.
     */
    public Map<String, DocumentationCommand> getZbiLookup() {
        return zbiLookup;
    }

    /**
     * Get SGD lookup map.
     */
    public Map<String, DocumentationCommand> getSgdLookup() {
        return sgdLookup;
    }
}
