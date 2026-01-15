package com.zebra.zebraprintereuredsetup;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines code snippets for ZPL, SGD, and ZBI commands.
 * Snippets are templates with cursor positioning for common command patterns.
 */
public class CodeSnippet {
    private final String trigger;      // What user types to trigger snippet (e.g., "^XA", "^FO")
    private final String name;         // Display name
    private final String template;     // The snippet template with $0 for cursor position
    private final String type;         // ZPL, SGD, or ZBI
    private final String description;  // Brief description

    public CodeSnippet(String trigger, String name, String template, String type, String description) {
        this.trigger = trigger;
        this.name = name;
        this.template = template;
        this.type = type;
        this.description = description;
    }

    public String getTrigger() { return trigger; }
    public String getName() { return name; }
    public String getTemplate() { return template; }
    public String getType() { return type; }
    public String getDescription() { return description; }

    /**
     * Get the text to insert (template without cursor marker).
     */
    public String getInsertText() {
        return template.replace("$0", "");
    }

    /**
     * Get the cursor position offset from the start of the inserted text.
     */
    public int getCursorOffset() {
        int pos = template.indexOf("$0");
        return pos >= 0 ? pos : template.length();
    }

    /**
     * Check if this snippet matches the query.
     */
    public boolean matchesQuery(String query) {
        if (query == null || query.isEmpty()) return false;
        String upperQuery = query.toUpperCase();
        return trigger.toUpperCase().startsWith(upperQuery) ||
               name.toUpperCase().contains(upperQuery);
    }

    /**
     * Create all predefined snippets.
     */
    public static List<CodeSnippet> createAllSnippets() {
        List<CodeSnippet> snippets = new ArrayList<>();

        // ===== ZPL Snippets =====

        // Label wrapper
        snippets.add(new CodeSnippet(
                "^XA",
                "Label Start/End",
                "^XA\n$0\n^XZ",
                "ZPL",
                "Complete label with opening and closing tags"
        ));

        // Text field
        snippets.add(new CodeSnippet(
                "^FO",
                "Text Field",
                "^FO$0,^A0N,30,30^FD^FS",
                "ZPL",
                "Field origin with font and data"
        ));

        // Text field with position
        snippets.add(new CodeSnippet(
                "^FD",
                "Field Data",
                "^FD$0^FS",
                "ZPL",
                "Field data with separator"
        ));

        // Code 128 Barcode
        snippets.add(new CodeSnippet(
                "^BC",
                "Code 128 Barcode",
                "^FO50,50^BCN,100,Y,N,N^FD$0^FS",
                "ZPL",
                "Code 128 barcode with field"
        ));

        // QR Code
        snippets.add(new CodeSnippet(
                "^BQ",
                "QR Code",
                "^FO50,50^BQN,2,5^FDQA,$0^FS",
                "ZPL",
                "QR code with field"
        ));

        // Data Matrix
        snippets.add(new CodeSnippet(
                "^BX",
                "Data Matrix",
                "^FO50,50^BXN,5,200^FD$0^FS",
                "ZPL",
                "Data Matrix barcode"
        ));

        // Graphic Box
        snippets.add(new CodeSnippet(
                "^GB",
                "Graphic Box",
                "^FO50,50^GB$0,100,3^FS",
                "ZPL",
                "Draw a box or line"
        ));

        // Font selection
        snippets.add(new CodeSnippet(
                "^A0",
                "Scalable Font",
                "^A0N,$0,^FD^FS",
                "ZPL",
                "Scalable font with height, width"
        ));

        // Change default font
        snippets.add(new CodeSnippet(
                "^CF",
                "Change Default Font",
                "^CF0,$0,",
                "ZPL",
                "Set default font and size"
        ));

        // Print quantity
        snippets.add(new CodeSnippet(
                "^PQ",
                "Print Quantity",
                "^PQ$0,0,1,Y",
                "ZPL",
                "Set print quantity"
        ));

        // Label home
        snippets.add(new CodeSnippet(
                "^LH",
                "Label Home",
                "^LH$0,",
                "ZPL",
                "Set label home position"
        ));

        // Field block
        snippets.add(new CodeSnippet(
                "^FB",
                "Field Block",
                "^FB$0,3,0,L,0^FD^FS",
                "ZPL",
                "Multi-line text block"
        ));

        // ===== SGD Snippets =====

        // Get variable
        snippets.add(new CodeSnippet(
                "getvar",
                "Get Variable",
                "! U1 getvar \"$0\" \"\"",
                "SGD",
                "Get printer variable value"
        ));

        // Set variable
        snippets.add(new CodeSnippet(
                "setvar",
                "Set Variable",
                "! U1 setvar \"$0\" \"\"",
                "SGD",
                "Set printer variable value"
        ));

        // Do action
        snippets.add(new CodeSnippet(
                "do",
                "Execute Action",
                "! U1 do \"$0\" \"\"",
                "SGD",
                "Execute printer action"
        ));

        // Common SGD commands
        snippets.add(new CodeSnippet(
                "device.languages",
                "Get Languages",
                "! U1 getvar \"device.languages\" \"\"",
                "SGD",
                "Get supported languages"
        ));

        snippets.add(new CodeSnippet(
                "device.friendly_name",
                "Get/Set Friendly Name",
                "! U1 getvar \"device.friendly_name\" \"\"",
                "SGD",
                "Get printer friendly name"
        ));

        snippets.add(new CodeSnippet(
                "ip.dhcp.enable",
                "DHCP Enable",
                "! U1 setvar \"ip.dhcp.enable\" \"$0\"",
                "SGD",
                "Enable/disable DHCP (on/off)"
        ));

        snippets.add(new CodeSnippet(
                "media.type",
                "Media Type",
                "! U1 setvar \"media.type\" \"$0\"",
                "SGD",
                "Set media type (label/continuous)"
        ));

        // ===== ZBI Snippets =====

        // IF block
        snippets.add(new CodeSnippet(
                "IF",
                "IF...THEN...END IF",
                "IF $0 THEN\n\nEND IF",
                "ZBI",
                "Conditional block"
        ));

        // IF-ELSE block
        snippets.add(new CodeSnippet(
                "IFELSE",
                "IF...THEN...ELSE...END IF",
                "IF $0 THEN\n\nELSE\n\nEND IF",
                "ZBI",
                "Conditional with else"
        ));

        // DO WHILE loop
        snippets.add(new CodeSnippet(
                "DO",
                "DO...LOOP WHILE",
                "DO\n    $0\nLOOP WHILE ",
                "ZBI",
                "Do-while loop"
        ));

        // DO UNTIL loop
        snippets.add(new CodeSnippet(
                "DOUNTIL",
                "DO...LOOP UNTIL",
                "DO\n    $0\nLOOP UNTIL ",
                "ZBI",
                "Do-until loop"
        ));

        // FOR loop
        snippets.add(new CodeSnippet(
                "FOR",
                "FOR...TO...NEXT",
                "FOR $0 = 1 TO 10\n\nNEXT",
                "ZBI",
                "For loop"
        ));

        // OPEN file
        snippets.add(new CodeSnippet(
                "OPEN",
                "OPEN...CLOSE",
                "OPEN #1: NAME \"$0\"\n\nCLOSE #1",
                "ZBI",
                "Open and close file"
        ));

        // SUB routine
        snippets.add(new CodeSnippet(
                "SUB",
                "SUB...END SUB",
                "SUB $0\n\nEND SUB",
                "ZBI",
                "Subroutine definition"
        ));

        // PRINT to printer
        snippets.add(new CodeSnippet(
                "PRINTCHANNEL",
                "PRINT to Channel",
                "OPEN #1: NAME \"ZPL\"\nPRINT #1: $0\nCLOSE #1",
                "ZBI",
                "Print to ZPL channel"
        ));

        // Error handling
        snippets.add(new CodeSnippet(
                "ONERROR",
                "ON ERROR Handler",
                "ON ERROR GOTO ErrorHandler\n$0\nEND\n\nErrorHandler:\nRESUME NEXT",
                "ZBI",
                "Error handling block"
        ));

        // DIM array
        snippets.add(new CodeSnippet(
                "DIM",
                "DIM Array",
                "DIM $0(10)",
                "ZBI",
                "Declare array"
        ));

        return snippets;
    }
}
