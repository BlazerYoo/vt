/*
 * Object representing a single engine scan with engine name and scan result.
 * public static multiPrint methods is used in VT.java to print engine scans in
 * tabular format.
 */

// Import libraries
// jansi - color printing

import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Color.BLACK;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.WHITE;
import static org.fusesource.jansi.Ansi.Color.YELLOW;


public class ScanResult {

    // Constants
    private static final int MAX_ENGINE_LEN = 20;
    private static final int TABLE_ROW_LEN = 36;
    private static final Ansi.Color NORMAL_HIGHLIGHT = BLACK;
    private static final Ansi.Color NORMAL_TEXT = GREEN;
    private static final Ansi.Color ERROR_HIGHLIGHT = YELLOW;
    private static final Ansi.Color ERROR_TEXT = BLACK;
    private static final Ansi.Color BAD_HIGHLIGHT = RED;
    private static final Ansi.Color BAD_TEXT = WHITE;
    private static final String[] ERROR_MESSAGES = new String[] {
            "Confirmed timeout", "Timeout",
            "Unable to process file type", ""
    };
    private static final String UNDETECTED = "Undetected";

    // Instance variables
    private String engine;  // Engine name
    private String result;  // Result from engine scan
    private boolean error;  // Was there error in engine scan

    // Default constructor
    public ScanResult() {
        engine = "VirusTotal";
        result = UNDETECTED;
        error = false;
    }

    // Specified constructor sets instance variables with 'engine' and 'result'
    public ScanResult(String engine, String result) {
        this.engine = engine;
        this.result = result;
        error = false;

        // Change 'result' and 'error' instance variables if there was error
        for (String errorMessage : ERROR_MESSAGES) {
            if (result.equalsIgnoreCase(errorMessage)) {
                this.result = UNDETECTED;
                error = true;
            }
        }
    }

    // Return engine name
    private String getEngine() {
        return engine;
    }

    // Return whether engine had error
    private boolean hasError() {
        return error;
    }

    // Return engine scan result
    private String getResult() {
        return result;
    }

    // Print colored coded (truncated or padded) engine name based on 'scanResult'
    private static void colorPrintEngine(ScanResult scanResult) {

        // Engine name
        String engine = "";

        // Truncate engine name
        // https://docs.oracle.com/javase/7/docs/api/java/lang/String.html
        try {
            engine = scanResult.getEngine().substring(0, MAX_ENGINE_LEN);
        }

        // Pad engine name with spaces
        catch (IndexOutOfBoundsException e) {
            engine = scanResult.getEngine();
            for (int i = engine.length(); i < MAX_ENGINE_LEN; i++)
                engine += " ";
        }

        // If result is "Undetected" -> print engine name NORMAL
        if (scanResult.getResult().equalsIgnoreCase(UNDETECTED)) {
            VT.colorPrint(false, NORMAL_HIGHLIGHT, NORMAL_TEXT, engine);
        }

        // If scan had error -> print engine name ERROR
        else if (scanResult.hasError()) {
            VT.colorPrint(false, ERROR_HIGHLIGHT, ERROR_TEXT, engine);
        }

        // If result is malicious -> print engine name BAD
        else {
            VT.colorPrint(false, BAD_HIGHLIGHT, BAD_TEXT, engine);
        }
    }

    // Print out 'scanResults' in tabular format with 'column' columns
    public static void multiPrint(ScanResult[] scanResults, int column) {
        StdOut.println();

        // Determine width of table
        int tableWidth = TABLE_ROW_LEN * column;

        // If user specified engine was found
        if (scanResults.length == 1)
            tableWidth = TABLE_ROW_LEN;

        // Add upper bar to table
        for (int i = 0; i < tableWidth; i++)
            StdOut.print('-');
        StdOut.println();

        // Add engine-result content
        int nextLineCount = 1;      // Determine when to print to next row
        int engineIndex = 0;        // Determine when printing last engine

        // Iterate through given engine scans
        for (ScanResult scanResult : scanResults) {

            // Print color coded
            colorPrintEngine(scanResult);

            // https://introcs.cs.princeton.edu/java/15inout/
            // https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html
            StdOut.printf("%-11.10s", scanResult.getResult());

            // If time to print to next line or if user selected engine was found or
            // if printing the last engine
            if (nextLineCount == column || scanResults.length == 1
                    || engineIndex == scanResults.length - 1) {

                // Add lower bar
                StdOut.println();
                for (int i = 0; i < tableWidth; i++)
                    StdOut.print('-');
                StdOut.println();
                nextLineCount = 1;
            }

            // Print next engine scan tabbed next to previous engine scan
            else {
                StdOut.print("\t|\t");
                nextLineCount++;
            }

            engineIndex++;
        }

    }
}
