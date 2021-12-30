/*
 * Takes in file path and optional antivirus engine name as command line arguments
 * and uploads the file to VirusTotal and display results to terminal. If
 * existing av engine name is specified, display results only for that av scan.
 *
 * ------------------------------------------------------------------------------
 * usage: vt [--help] [--file FILE_PATH] [--engine ENGINE]
 *
 * Scan your file across multiple antivirus engines
 *
 * arguments:
 *   --help, -h             : display this help menu and exit
 *   --file, -f FILE_PATH   : path of file to send to VirusTotal
 *   --engine, -e ENGINE    : (optional) choose what antivirus engine to
 *                            scan with (default is all available)
 *
 * See https://support.virustotal.com/hc/en-us/articles/115002146809-Contributors
 * to see the full list of antivirus engines
 *
 * example:
 * vt -f /full/path/to/java.exe -e Kaspersky
 * ------------------------------------------------------------------------------
 */


// Import libraries
// jansi - color printing
// selenium - browser automation
// file - dealing with file validity
// List - compile JavaScript Array
// logging - suppress logging

import org.fusesource.jansi.Ansi;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fusesource.jansi.Ansi.Color.BLACK;
import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.WHITE;
import static org.fusesource.jansi.Ansi.ansi;


// Multi-engine antivirus terminal program
public class VT {

    // Constants
    private static final String HELP_FLAG = "--help";
    private static final String SHORT_HELP_FLAG = "-h";
    private static final Ansi.Color NORMAL_HIGHLIGHT = BLACK;
    private static final Ansi.Color ERROR_HIGHLIGHT = WHITE;
    private static final Ansi.Color ERROR_TEXT = RED;
    private static final String MESSAGE_PREFIX = "\n\"";


    // Print 'str' with 'highlight' and 'text' colors and with
    // erase (clear terminal?); Used in ScanResult.java so has to be public
    public static void colorPrint(boolean erase, Ansi.Color highlight,
                                  Ansi.Color text, String str) {

        // Clear terminal + print or don't clear terminal + print
        if (erase)

            // http://fusesource.github.io/jansi/
            StdOut.print(ansi().eraseScreen().bg(highlight).fg(text).a(str)
                               .reset());
        else
            StdOut.print(ansi().bg(highlight).fg(text).a(str).reset());
    }


    // Print VirusTotal ascii art logo with 'text' color
    public static void printLogo(Ansi.Color text) {

        // VirusTotal ascii art logo
        // https://www.virustotal.com/gui/home/upload
        // https://cloudconvert.com/svg-to-jpg
        // https://pypi.org/project/ascii-art-cli/
        //      Python library for image -> ascii art
        //      cmd: ascii-art "logo - Copy.jpg" --width 90 --height 9 --chars @.
        String logo =
                "\n.@@@@@@@@@@@@@@@@......................................"
                        + "...................................\n"
                        + "..@@@..........@@.............................."
                        + "...........................................\n"
                        + "....@@@........@@......@....@@.@@..@@@@@..@...."
                        + "@..@@@@@.@@@@@@..@@@@@.@@@@@@...@@....@....\n"
                        + "......@@@......@@......@@..@@..@@.@@..@@..@...."
                        + "@.@@@......@@..@@....@@..@@....@@@...@@....\n"
                        + ".......@@@.....@@.......@@.@@..@@.@@@@@@..@...."
                        + "@..@@@@....@@..@@....@@..@@...@@.@@..@@....\n"
                        + "......@@@......@@.......@@@@...@@.@@..@@..@@..@"
                        + "@.....@@...@@...@....@@..@@..@@@@@@@.@@....\n"
                        + "....@@@........@@........@@....@@.@@...@..@@@@@"
                        + "...@@@@.....@....@@@@@...@@..@@....@..@@@@@\n"
                        + "..@@@..........@@.............................."
                        + "...........................................\n"
                        + ".@@@@@@@@@@@@@@@@.............................."
                        + "...........................................\n";

        // Clear terminal, print 'logo' with 'NORMAL_HIGHLIGHT' + 'text' color
        colorPrint(true, NORMAL_HIGHLIGHT, text, logo);
    }


    // Check 'args' (command line arguments) and if --help, -h flag passed,
    // print help menu with 'text' color
    private static void help(String[] args, Ansi.Color text) {

        // Iterate through command line arguments
        for (int i = 0; i < args.length; i++) {

            // If argument matches --help, -h flag
            if (args[i].equalsIgnoreCase(HELP_FLAG)
                    || args[i].equalsIgnoreCase(SHORT_HELP_FLAG)) {

                // Long help menu String
                String helpMenu = "\nusage: vt [--help] [--file FILE_PATH]"
                        + " [--engine ENGINE]\n\n"
                        + "Scan your file across multiple antivirus engines\n\n"
                        + "arguments:\n"
                        + " --help, -h\t\t: display this help menu and exit\n"
                        + " --file, -f FILE_PATH\t: path of file to"
                        + " send to VirusTotal\n"
                        + " --engine, -e ENGINE\t: (optional) choose what"
                        + " antivirus engine to\n\t\t\t  scan with (default is"
                        + " all available)\n\n"
                        + "See https://support.virustotal.com/hc/en-us/"
                        + "articles/115002146809-Contributors\n"
                        + "to see the full list of antivirus engines\n\n"
                        + "example:\n"
                        + "vt -f /full/path/to/java.exe -e Kaspersky\n";

                // Don't clear terminal, print 'helpMenu' with
                // 'NORMAL_HIGHLIGHT' + 'text' color
                colorPrint(false, NORMAL_HIGHLIGHT, text, helpMenu);
                StdOut.println();

                // End program
                // https://stackoverflow.com/questions/2434592/difference-in-syst
                // em-exit0-system-exit-1-system-exit1-in-java
                System.exit(0);
            }
        }
    }


    // Return argument from 'args' (command line arguments) following
    // 'flag' or 'shortFlag'
    private static String cmdLineArg(String[] args, String flag,
                                     String shortFlag) {

        // Argument after the flag
        String cla = "";

        // Iterate through command line arguments
        for (int i = 0; i < args.length; i++) {

            // If argument matches 'flag' or 'shortFlag'
            if (args[i].equalsIgnoreCase(flag)
                    || args[i].equalsIgnoreCase(shortFlag)) {

                // An argument follows the flag
                try {
                    cla = args[i + 1];
                }

                // No argument follows the flag
                catch (ArrayIndexOutOfBoundsException e) {

                    // Color print error message
                    String message = "\nPlease enter an argument after the "
                            + args[i] + " flag.\n";
                    colorPrint(false, ERROR_HIGHLIGHT, ERROR_TEXT, message);
                    StdOut.println();

                    // End program
                    System.exit(0);
                }
            }
        }

        // Return argument after the flag
        return cla;
    }


    // Check if 'filePath' represent a valid, existing, accessible file
    // and return absolute file path to the file
    private static String fileExists(String filePath) {

        // Absolute path of 'filePath'
        // https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html
        // https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html
        // https://docs.oracle.com/javase/7/docs/api/java/nio/file/Paths.html
        Path absFilePath = Paths.get(filePath).toAbsolutePath();

        // Is the file valid, existing, accessible file
        // https://docs.oracle.com/javase/tutorial/essential/io/check.html
        boolean realFile = Files.isRegularFile(absFilePath)
                && Files.isReadable(absFilePath)
                && Files.isExecutable(absFilePath) && Files.exists(absFilePath);

        // If not valid, existing, accessible file
        if (!realFile) {

            // Color print error message
            String message = MESSAGE_PREFIX + filePath + "\" is not an"
                    + " accessible file.\n";
            colorPrint(false, ERROR_HIGHLIGHT, ERROR_TEXT, message);
            StdOut.println();

            // End program
            System.exit(0);
        }

        // Return absolute path of file
        return absFilePath.toString();
    }


    // Execute JavaScript 'script' on webpage open on 'driver' (browser)
    // and return the HTML element if you want to return something (!'noReturn')
    public static WebElement findElement(WebDriver driver, String script,
                                         boolean noReturn) {

        // HTML element returned from JavaScript script or null
        WebElement element = null;

        // JavaScript 'script' returns something
        // https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/J
        // avascriptExecutor.html
        // https://www.browserstack.com/guide/javascriptexecutor-in-selenium
        // https://www.lambdatest.com/blog/how-to-use-javascriptexecutor-in-sele
        // nium-webdriver/
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            element = (WebElement) js.executeScript(script);
        }
        finally {

            // If you want to return the HTML element, but JavaScript 'script'
            // doesn't return anything
            // https://www.oracle.com/technical-resources/articles/java/java8-op
            // tional.html
            if (element == null && !noReturn) {

                // Color print error message
                String message = "\nELEMENT NOT FOUND: There was an error with"
                        + " VirusTotal. Please try again.\n";
                colorPrint(false, ERROR_HIGHLIGHT, ERROR_TEXT, message);
                driver.quit();
                StdOut.println();

                // End program
                System.exit(0);
            }
        }

        // Return the HTML element or null
        return element;
    }


    // Returns true when a String is returned from execution of JavaScript
    // 'script' on webpage open on 'driver' (browser)
    public static boolean foundString(WebDriver driver, String script) {

        // String to be returned from JavaScript 'script' execution
        String str = "";

        // Keep executing JavaScript 'script' until a String is returned
        while (str == null || str.isEmpty()) {

            // Execute 'script'
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                str = (String) js.executeScript(script);
            }

            // Execute 'script' again
            // https://stackoverflow.com/questions/40392594/java-try-and-catch-u
            // ntil-no-exception-is-thrown
            catch (JavascriptException e) {
                continue;
            }
        }

        // Return true when a String is returned
        return true;
    }


    // Return a list of HTML elements that is returned from executing
    // JavaScript 'resultScript' on webpage open on 'driver' (browser)
    public static List<WebElement> findResult(WebDriver driver,
                                              String resultScript) {

        // List of HTML elements returned from execution of JavaScript
        // 'resultScript'
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<WebElement> detectionElements = (List<WebElement>)
                js.executeScript(resultScript);

        // Return list of HTML elements
        return detectionElements;
    }


    //  If specified engine found, removes all <engine, result> pairs that are
    //  not the <'selectEngine', result> pair from 'rawScanResults' SymbolTable
    public static void removeEngines(String selectEngine,
                                     ST<String, String> rawScanResults) {

        // Compile array of engines to remove; '- 1' since all are removed
        // except the one <'selectEngine', result> pair
        String[] removeEngines = new String[rawScanResults.size() - 1];
        int index = 0;
        for (String engine : rawScanResults.keys()) {
            if (!engine.equalsIgnoreCase(selectEngine)) {
                removeEngines[index] = engine;
                index++;
            }
        }

        // Remove <engine, result> pairs for engines in the
        // above compiled array
        for (String removeEngine : removeEngines)
            if (removeEngine != null)
                rawScanResults.remove(removeEngine);

        // Color print notification
        String message = MESSAGE_PREFIX + selectEngine + "\" engine was found."
                + " Scanned with \"" + selectEngine + "\".\n";
        colorPrint(false, NORMAL_HIGHLIGHT, CYAN, message);
    }


    // Main method of this class
    // Runs everything
    public static void main(String[] args) {

        //--------------------------------------------------------------
        // TESTING: runs if '--test' or '-t' flag is present
        // 2 methods to test: cmdLineArg and fileExists
        // Iterate through command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--test")) {
                // cmdLineArg test 1:
                String[] args0 = new String[] { "a", "-b", "follows b" };
                String afterB = cmdLineArg(args0, "--b", "-b");
                assert afterB.equals("follows b");

                // cmdLineArg test 2:
                String[] args1 = new String[] { "vt", "--file", "file", "-e", "engine" };
                String file0 = cmdLineArg(args1, "--file", "-f");
                assert file0.equals("file");
                String engineName = cmdLineArg(args1, "--engine", "-e");
                assert engineName.equals("engine");

                // cmdLineArg test 3:
                StdOut.println("\nThe following output is from a test case:");
                String[] args2 = new String[] { "vt", "--file" };
                String file1 = cmdLineArg(args2, "--file", "-f");
                assert file1 == null;

            }

            if (args[i].equals("-t")) {

                // fileExists test 1:
                // https://www.delftstack.com/howto/java/java-get-type-of-object/
                StdOut.println(fileExists("ScanResult.java").getClass() ==
                                       String.class);

                // filesExists test 2:
                StdOut.println("\nThe following output is from a test case:");
                StdOut.println(fileExists("asdfasdf").equals("sdf"));
            }
        }
        //--------------------------------------------------------------

        // Color print logo
        printLogo(BLUE);

        // Check help requested
        help(args, GREEN);

        // Get file and engine command line arguments
        String filePath = cmdLineArg(args, "--file", "-f");
        String selectEngine = cmdLineArg(args, "--engine", "-e");
        boolean engineFound = false;

        // If user entered --file, -f
        if (!filePath.isEmpty()) {

            // Check if valid file
            String absFilePath = fileExists(filePath);

            // If valid file
            // Start Chrome WebDriver
            //      Locate ChromeDriver binary
            System.setProperty("webdriver.chrome.driver",
                               ".\\chromedriver_win32\\chromedriver.exe");

            // Browsing options
            // https://stackoverflow.com/questions/52975287/selenium-chromedrive
            // r-disable-logging-or-redirect-it-java
            // https://www.selenium.dev/selenium/docs/api/java/org/openqa/seleni
            // um/chrome/ChromeOptions.html
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(true);
            //      Logging suppression
            options.setLogLevel(ChromeDriverLogLevel.OFF);
            Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
            WebDriver driver = new ChromeDriver(options);

            // Go to VirusTotal
            driver.get("https://www.virustotal.com/gui/home/upload");

            // Clear terminal with colored logo and notification
            printLogo(BLUE);
            String message = "\nScanning \"" + filePath + "\"...\n";
            colorPrint(false, BLACK, CYAN, message);

            // Find 'Choose file' button
            //      Long messy JavaScript code to find the button
            // https://stackoverflow.com/questions/62799036/unable-to-locate-the
            // -sign-in-element-within-shadow-root-open-using-selenium-a/6280367
            // 1#62803671
            // https://stackoverflow.com/questions/59345959/how-to-pass-the-valu
            // e-in-search-on-url-https-www-virustotal-com-gui-home-searc
            // https://developer.mozilla.org/en-US/docs/Web/API/Document/querySe
            // lector
            String script = "return document.querySelector('vt-ui-shell"
                    + " div#view-container home-view')"
                    + ".shadowRoot.querySelector('div.wrapper"
                    + " div.omnibar vt-ui-selector#section"
                    + " div.vt-selected vt-ui-main-upload-form#uploadForm')"
                    + ".shadowRoot"
                    + ".querySelector('div.wrapper input#fileSelector')";
            WebElement fileUpload = findElement(driver, script, false);

            // Upload file
            // https://www.guru99.com/upload-download-file-selenium-webdriver.html
            fileUpload.sendKeys(absFilePath);

            // Check if the file has been uploaded to VirusTotal before
            //      If file is new to VirusTotal, click 'Confirm upload' button
            //          Long messy JavaScript code to find + click the button
            String newFileScript = "document.querySelector('vt-ui-shell')"
                    + ".shadowRoot.querySelector('vt-ui-dialog#uploadModal')"
                    + ".getElementsByClassName('content upload-dialog')[0]"
                    + ".querySelector('vt-ui-main-upload-form#uploadMoldaForm')"
                    + ".shadowRoot.querySelector('div.wrapper"
                    + " vt-ui-button#confirmUpload').click();";
            findElement(driver, newFileScript, true);

            // Check if all engine scans are complete
            //      Long messy JavaScript code to find text for scan completion
            String scanCompleteScript
                    = "return document.querySelector('vt-ui-shell"
                    + " div#view-container file-view')"
                    + ".shadowRoot"
                    + ".querySelector('vt-ui-main-generic-report#report"
                    + " vt-ui-file-card').shadowRoot"
                    + ".querySelector('vt-ui-generic-card div"
                    + " div.detections span div p').innerText;";

            //      If/wait until all engine scans are complete
            if (foundString(driver, scanCompleteScript)) {
                //  Long messy JavaScript code to find + form Array of
                //  each individual engine scan HTML element
                // https://www.geeksforgeeks.org/jquery-queryselector-vs-queryse
                // lectorall-methods/
                // https://www.techiediaries.com/javascript-queryselectorall-nod
                // elist-foreach/
                String resultScript
                        = "return Array.from(document.querySelector("
                        + "'vt-ui-shell div#view-container file-view')"
                        + ".shadowRoot.querySelector('"
                        + "vt-ui-main-generic-report#report span.tab-slot"
                        + " vt-ui-detections-list#detectionsList')"
                        + ".shadowRoot.querySelector('div#detections')"
                        + ".querySelectorAll('div.detection'));";

                // Compile list of HTML elements for each engine scan
                List<WebElement> detectionElements = findResult(driver,
                                                                resultScript);

                // SymbolTable for <engine, result> pairs
                // https://introcs.cs.princeton.edu/java/44st/
                // https://introcs.cs.princeton.edu/java/code/javadoc/ST.html
                ST<String, String> rawScanResults = new ST<String, String>();

                // Iterate through list of each engine scan HTML elements
                for (int i = 0; i < detectionElements.size(); i++) {

                    // Individual engine name
                    String engine = "";

                    // Following try-catch needed because of
                    // org.openqa.selenium.StaleElementReferenceException
                    //      Get engine name from engine scan element
                    // https://stackoverflow.com/questions/19149327/selenium-exc
                    // eption-in-thread-main-org-openqa-selenium-staleelementref
                    // erenceex
                    try {
                        // https://docs.oracle.com/javase/7/docs/api/java/util/regex
                        // /Pattern.html#sum
                        engine = detectionElements.get(i).getText().split("\n")[0];
                    }

                    //      Get engine name from engine scan element again
                    catch (StaleElementReferenceException e) {
                        engine = detectionElements.get(i).getText().split("\n")[0];
                    }

                    // Individual result from engine scan
                    String result = "";

                    // Get result from engine scan element
                    try {
                        result = detectionElements.get(i).getText().split("\n")[1];
                    }

                    // Continue if no result in engine scan element
                    catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }

                    // Put <engine, result> pair into SymbolTable
                    rawScanResults.put(engine, result);

                    // If user entered --engine, -e
                    if (!selectEngine.isEmpty()) {

                        // If engine was used
                        if (engine.equalsIgnoreCase(selectEngine)) {

                            engineFound = true;
                            // Remove all engine scans except scan with selectEngine
                            removeEngines(selectEngine, rawScanResults);
                            break;
                        }
                    }
                }

                // If user specified engine was not used
                if (!selectEngine.isEmpty() && !engineFound) {
                    // Color print notification
                    message = MESSAGE_PREFIX + selectEngine + "\" engine"
                            + "\" was not found. Scanned with all engines."
                            + "\n";
                    colorPrint(false, ERROR_HIGHLIGHT, ERROR_TEXT, message);
                }

                // Convert SymbolTable <engine, result> pairs into ScanResult[]
                ScanResult[] scanResults = new ScanResult[rawScanResults.size()];
                int index = 0;
                for (String engine : rawScanResults.keys()) {
                    scanResults[index] = new ScanResult(engine,
                                                        rawScanResults.get(engine));
                    index++;
                }

                // Print out engine scan results in tabular format
                ScanResult.multiPrint(scanResults, 2);
            }

            // End WebDriver
            driver.quit();
        }

        // If user didn't enter --file, -f
        else {

            // Color print error message
            String message = "\nPlease enter the -f or --file flag with the"
                    + " file path after that.\n";
            colorPrint(false, WHITE, RED, message);

            // Color print help menu
            help(new String[] { "-h" }, GREEN);
        }

        StdOut.println();

        // End program
        System.exit(0);
    }
}
