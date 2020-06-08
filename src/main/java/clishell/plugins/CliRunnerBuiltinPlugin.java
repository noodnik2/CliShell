/**
 *
 *
 * Command Line Interface Harness
 *
 *
 *
 *
 * @author MRoss
 *
 */

package clishell.plugins;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import clishell.CliCommandOptions;
import clishell.CliPluginCommandMetadata;
import clishell.CliPluginMetadata;
import clishell.CliRunner;
import clishell.CommandName;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.ex.CliInvariantViolationException;
import clishell.ex.CliPluginMethodException;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;
import org.mortbay.util.WriterOutputStream;

/**
 *
 * CLI "Builtin" plugin - comes bundled with CLI;
 * implements the most basic of CLI commands,
 * available to all other plugins and users
 *
 */
@CliPlugin(
     name = "clirunner-builtin"
, version = "0.2.$Rev: 12400 $"
)
public class CliRunnerBuiltinPlugin {


    //
    // Private instance data
    //

    /** errordisplay console bufferwriter when turned "on" */
    private TailCharBufferWriter mErrorDisplayConsoleBuffer;

    /** errordisplay console printwriter (wraps the buffer writer) when turned "on" */
    private PrintWriter mErrorDisplayConsole;


    //
    // Public plugin finalization methods
    //

    @CliPluginFinalizer
    public void fini() {
        // deactivate any currently active error display buffer
        deactivateErrorDisplayBuffer();
    }


    //
    //  Public plugin command methods
    //

    @CliPluginCommand(
        names = {"quit", "bye", "exit" }
      , helptext = { "Exit the CLI application" }
      , ordering = "a1"
    )
    public void exitCommand() {
        CliRunner cliRunner = CliRunner.getInstance();
        if (cliRunner.isCliDebug()) {
            cliRunner.getMessageConsole().println("cli builtin byebye");
        }
        cliRunner.quit();
    }

    @CliPluginCommand(
          name = "help"
    ,   syntax = "[-v] [-p <plugin-list>] [<command-filter> [<command-filter> [...]]]"
    ,  options = "vp:"
    , ordering = "a2"
    , helptext = {
          "Prints syntax diagram(s) of currently loaded commands matching <command-filter>(s)."
        , "Use a regular expression for each <command-filter> to restrict the list to commands"
        , "whose names begin with matched word(s)."
        , "Options:"
        , "   -v   includes help text for each command"
        , "   -p   only include the specified plugin(s) in the search"
      }
    )
    public void helpCommand(CliCommandOptions options, String... commandNameFilters) {

        // get the list plugins to restrict the search to (if specified)
        Set<String> pluginList = null;
        if (options.isOptionSet('p')) {
            pluginList = new HashSet<String>();
            for (String pluginName : options.getOptionValue('p').split(",")) {
                pluginList.add(pluginName);
            }
        }

        //
        // setup any command filters present
        //

        Pattern[] commandNameFilterPatterns = new Pattern[commandNameFilters.length];
        for (int i = 0; i < commandNameFilterPatterns.length; i++) {
            commandNameFilterPatterns[i] = Pattern.compile(commandNameFilters[i]);
        }

        //
        // get helper objects for building user output
        //

        // CliRunner
        CliRunner cliRunner = CliRunner.getInstance();

        // console writer
        PrintWriter messageConsoleWriter = cliRunner.getMessageConsole();

        // list of commands to show user for "current" plugin
        LinkedList<String> matchingPluginCommands = new LinkedList<String>();

        // buffer for building up "current" command entry in output
        StringBuffer commandEntry = new StringBuffer();

        // quick flag to tell if "verbose" option was specified
        boolean verboseFlag = options.isOptionSet('v');

        // scan all plugins...
        for (CliPluginMetadata pluginMetadata : cliRunner.getPlugins()) {
            matchingPluginCommands.clear(); // no commands for this plugin - yet

            // if there's a plugin list specified, check that this plugin is in the list
            if (pluginList != null) {
                if (!pluginList.contains(pluginMetadata.getName())) {
                    // skip this plugin - it's not in the list
                    continue;
                }
            }

            // scan all commands supported by current plugin...
            for (CliPluginCommandMetadata commandMetadata : pluginMetadata.getCommands()) {

                // apply any command filters that may have been specified...
                if (commandNameFilterPatterns.length > 0) {

                    // say it doesn't match unless we see that it does...
                    boolean matched = false;

                    // try to match using all names current command goes by...
                    for (CommandName commandName : commandMetadata.getNames()) {
                        // try all command filters specified by user...
                        for (Pattern commandNameFilterPattern : commandNameFilterPatterns) {
                            matched = commandNameFilterPattern
                                .matcher(commandName.toString()).lookingAt();
                            if (matched) {
                                break;          // if it matches, no need to check for more
                            }
                        }
                        if (matched) {
                            break;              // if it matches, no need to check for more
                        }
                    }
                    if (!matched) {
                        continue;               // if command didn't match, skip it
                    }
                }

                // build the command entry
                CliPluginCommand cliPluginCommand = commandMetadata
                    .getCliMethod().getMethod().getAnnotation(CliPluginCommand.class);

                commandEntry.setLength(0);
                commandEntry.append(commandMetadata.getDisplayName());
                String syntax = cliPluginCommand.syntax();
                if (!"".equals(syntax)) {
                    commandEntry.append(" " + syntax);
                }

                // add the command entry to the list for this plugin
                matchingPluginCommands.add(commandEntry.toString());

                // if "verbose" is selected, then add in the help text underneath
                if (verboseFlag) {
                    for (String helpLine : cliPluginCommand.helptext()) {
                        matchingPluginCommands.add("    " + helpLine);
                    }
                    matchingPluginCommands.add("");
                }
            }

            // if this plugin had any matching commands, then print them on the console
            if (matchingPluginCommands.size() > 0) {
                // ... prefixed with the plugin name
                messageConsoleWriter.println("plugin: " + pluginMetadata.getName());
                for (String command : matchingPluginCommands) {
                    messageConsoleWriter.println(" " + command);
                }
            }
        }
    }

    @CliPluginCommand(
          name = "get versions"
    ,   syntax = "[<module-name-pattern>]"
    , helptext = { "Displays the known version(s) of the currently loaded module(s)"
                 , "matching the regular expression <module-name-pattern> (optional);"
                 , "module(s) for which version information is not known will not be listed"
              }
    , minargs = 0
    , ordering = "a2b"
    )
    public void getVersion(String moduleNamePattern)
        throws IOException, CliPluginMethodException {

        CliRunner cliRunnerInstance = CliRunner.getInstance();
        PrintWriter consoleOut = cliRunnerInstance.getMessageConsole();

        Pattern matchingPattern = null;
        if (moduleNamePattern != null) {
            matchingPattern = Pattern.compile(moduleNamePattern);
        }

        for (Map.Entry<String, String> versionEntry : cliRunnerInstance.getVersions().entrySet()) {

            String versionString = versionEntry.getValue();
            if (versionString == null) {
                continue;
            }

            String moduleName = versionEntry.getKey();
            if ((matchingPattern != null) && !matchingPattern
                    .matcher(moduleName).lookingAt()) {
                continue;
            }

            consoleOut.println(moduleName + "=" + versionString);
        }
    }

    @CliPluginCommand(
          name = "capture file"
    ,   syntax = "[-qe] <filename> <command> [<command-arg> [<command-arg> [...]]"
    ,  options = "qe"
    , helptext = {
              "Captures output from <command> and writes it to the file specified by <filename>"
          ,   "Options:"
          ,   "  -q  (quiet) suppress echoing the output to the current message console"
          ,   "  -e  (errors) also captures error message(s) into the file"
          }
    ,  minargs = 2
    )
    public void captureFile(CliCommandOptions options, String... args) throws CliRunnerException, IOException {

        // setup to write output to specified file
        PrintWriter messageConsole = new PrintWriter(new FileWriter(args[0]));

        // dispatch the command in args[1..n] using the new message console
        CliPluginUtil.dispatchCommand(options, null, messageConsole,
            Arrays.copyOfRange(args, 1, args.length));

        // close out the new message console
        // (will close underlying fileWriter)
        messageConsole .close();

    }

    @CliPluginCommand(
        name = "load properties"
      , syntax = "<propertyFilename>"
      , helptext = { "loads CLI properties from file" }
    )
    public void setProperties(String propertyFilename) throws IOException {
        CliRunner.getInstance().getProperties().load(new FileReader(propertyFilename));
    }

    @CliPluginCommand(
        name = "set property"
      , syntax = "<propertyName> <propertyValue>"
      , helptext = { "sets CLI property" }
    )
    public void setProperty(String propertyName, String propertyValue) {
        CliRunner.getInstance().getProperties().setProperty(propertyName, propertyValue);
    }

    @CliPluginCommand(
        name = "get properties"
      , ordering = "set property 2"
      , syntax = "[<propertyName-filter> [<propertyName-filter> [...]]]"
      , helptext = { "Prints the name(s) & value(s) of CLI property or properties."
                , "Use a regular expression for <propertyName-filter>"
                , "to restrict the list to properties whose names begin"
                , "with matched word(s)."
                }
    )
    public void getProperties(String... propertyNameFilters) throws IOException {

        CliRunner cliRunner = CliRunner.getInstance();

        //
        // setup any command filters present
        //

        Pattern[] propertyNameFilterPatterns = new Pattern[propertyNameFilters.length];
        propertyNameFilterPatterns = new Pattern[propertyNameFilters.length];
        for (int i = 0; i < propertyNameFilters.length; i++) {
            propertyNameFilterPatterns[i] = Pattern.compile(propertyNameFilters[i]);
        }

        Properties cliRunnerProperties = cliRunner.getProperties();
        Properties outputProperties = new Properties();
        for (String propertyName : cliRunnerProperties.stringPropertyNames()) {
            // apply any property name filter(s) that may have been specified
            if (propertyNameFilterPatterns.length > 0) {
                boolean found = false;
                for (Pattern propertyNameFilterPattern : propertyNameFilterPatterns) {
                    found = propertyNameFilterPattern.matcher(propertyName).lookingAt();
                    if (found) {
                        break;
                    }
                }
                // if no specified property name filter matched, don't output the entry
                if (!found) {
                    continue;
                }
            }
            // output the entry
            outputProperties.setProperty(propertyName, cliRunnerProperties.getProperty(propertyName));
        }

        // persist desired property set directly to user's console
        outputProperties.store(cliRunner.getMessageConsole(), null);

    }

    @CliPluginCommand(
        name = "list plugins"
      , options = "v"
      , syntax = "[-v]"
      , helptext = { "Lists currently loaded plugin(s)."
                , "If the '-v' option is specified, will also list the class names for each."
            }
    )
    public void listPlugins(CliCommandOptions options) {
        CliRunner cliRunner = CliRunner.getInstance();
        PrintWriter messageConsole = cliRunner.getMessageConsole();
        boolean verboseOption = options.isOptionSet('v');
        for (CliPluginMetadata cliPluginMetadata : cliRunner.getPlugins()) {
            messageConsole.print("plugin: '"
                + cliPluginMetadata.getName()
                + "'");
            if (verboseOption) {
                messageConsole.print(" ("
                    + cliPluginMetadata.getPluginInstance().getClass().getName()
                    + ")");
            }
            messageConsole.println();
        }
    }

    @CliPluginCommand(
        name = "load plugins"
      , ordering = "list plugins2"
      , syntax = "<plugin-class>[/<name>][ <plugin-class>[/<name>][ ...]]"
      , helptext = { "loads plugin(s) and optionally overrides name used by CLI" }
      , minargs = 1
    )
    public void loadPlugins(String[] args) throws CliPluginMethodException {
        CliRunner.getInstance().loadPlugins(Arrays.asList(args));
    }

    @CliPluginCommand(
        name = "unload plugins"
      , ordering = "list plugins 3"
      , syntax = "<plugin-name>][ <plugin-name>][ ...]]"
      , helptext = { "unloads plugin(s) identified by their CLI plugin-name" }
      , minargs = 1
    )
    public void unloadPlugins(String[] args) throws CliPluginMethodException {
        CliRunner.getInstance().unloadPlugins(Arrays.asList(args));
    }

    @CliPluginCommand(
        name = "system"
    , syntax = "[<command> [<arg> [<arg> [...]]]]"
    , helptext = { "Invokes a system command, or the system's shell"
                 , "if no arguments are specified."
            }
    , minargs = 0
    )
    public void invokeSystem(String... cmdArray)
        throws IOException {

        CliRunner instance = CliRunner.getInstance();
        SysCommandInvoker syscmdInvoker = new SysCommandInvoker();

        String[] args = cmdArray;

        // if user didn't give any arguments, simply invoke
        // the system shell, if it can be found
        if (cmdArray.length == 0) {
            String shellCommand = syscmdInvoker.getShellCommand();
            if (shellCommand == null) {
                throw new IOException("can't find system shell");
            }
            args = new String[] {shellCommand };
        }

        syscmdInvoker.system(
            new WriterOutputStream(instance.getMessageConsole()),
            new WriterOutputStream(instance.getErrorConsole()),
            null,
            args
        );

    }

    @CliPluginCommand(
        name = "echo"
      , ordering = "z end"
      , options = "*"
      , syntax = "<arg>[ <arg>[ ...]]"
      , helptext = { "echoes arguments (and options specified, if any)" }
    )
    public void echo(CliCommandOptions options, String[] args) {

        PrintWriter messageConsole = CliRunner.getInstance().getMessageConsole();
        messageConsole.println(new CommandName(args).toString());
        if (options.getOptionSet().size() > 0) {
            messageConsole.println("options = " + options);
        }

    }

    @CliPluginCommand(
          name = "set errordisplay buffer"
    ,   syntax = "{ <n> | off }"
    , helptext = {
                  "Activates or deactivates the error display buffer by allocating"
                , "the indicated number of characters for it (setting the size to 0,"
                , "or 'off' deactivates it)."
                , "When the error display buffer is active, the last <n> characters"
                , "of error message details (such as stack traces) are only written"
                , "into the error buffer (rather than the console), and must be"
                , "retrieved using a separate command."
            }
    ,  minargs = 1
    ,  maxargs = 1
    )
    public void setErrorDisplayBuffer(String selector) throws CliRunnerException {

        CliRunner cliRunner = CliRunner.getInstance();

        int bufferSize = 0;
        if (!"off".equals(selector)) {
            bufferSize = Integer.parseInt(selector);
        }

        // deactivate any currently active error display buffer
        deactivateErrorDisplayBuffer();

        // if user wanted it deactivated, we're done
        if (bufferSize == 0) {
            return;
        }

        mErrorDisplayConsoleBuffer = new TailCharBufferWriter(bufferSize);
        mErrorDisplayConsole = new PrintWriter(mErrorDisplayConsoleBuffer);
        cliRunner.setErrorConsole(mErrorDisplayConsole);

    }

    @CliPluginCommand(
          name = "clear errordisplay buffer"
    , ordering = "set errordisplay buffer 2"
    , helptext = { "Clears errordisplay buffer."
                 , "Command is ignored if buffer not active."
                 }
    )
    public void clearErrorDisplayBuffer() {

        // can only "clear" it if it's allocated (active)
        if (mErrorDisplayConsoleBuffer != null) {
            mErrorDisplayConsoleBuffer.reset();
        }

    }

    @CliPluginCommand(
            name = "view errordisplay buffer"
      , ordering = "set errordisplay buffer 3"
      , helptext = {
                    "Views tail of errordisplay buffer."
                  , "Command is ignored if buffer not active."
            }
    )
    public void viewErrorDisplayBuffer() {

        PrintWriter messageConsole = CliRunner.getInstance().getMessageConsole();

        messageConsole.println("--- Start of Error Display Buffer Tail ---");
        if (mErrorDisplayConsoleBuffer != null) {
            messageConsole.print(mErrorDisplayConsoleBuffer.toString());
        }
        messageConsole.println("--- End of Error Display Buffer Tail ---");

    }

    @CliPluginCommand(
          name = "time"
    ,   syntax = "[-dts] [-T <format>] <command> [<command-arg> [<command-arg> [...]]"
    ,  options = "dtsT:"
    ,  minargs = 1
    , helptext = {
            "Provides timing information about the execution of <command>.  Optionally prefixes each"
        ,   "output line with the time of day (-t or -T options), the elapsed time executing so far"
        ,   "(-d option), and includes an additional (summary) line at the end showing how long the"
        ,   "command took to execute (-s option)."
        ,   "Options:"
        ,   "  -d  prefix line(s) with relative time (milliseconds)"
        ,   "  -t  prefix line(s) with absolute time (standard format; yyMMddHHmmss)"
        ,   "  -T  prefix line(s) with absolute time (specified format; see javadoc for 'java.text.SimpleDateFormat')"
        ,   "  -s  prints summary timing (milliseconds); selected automatically if no other option selected"
        }
    )
    public void time(final CliCommandOptions options, String... args) throws CliRunnerException {

        CliRunner cliInstance = CliRunner.getInstance();
        PrintWriter newMessageConsole = null;
        final long startingTime = System.currentTimeMillis();
        boolean isLineOptionSpecified = false;

        // no date format yet
        DateFormat dateFormat = null;
        // if standard date format specified,
        if (options.isOptionSet('t')) {
            // then ensure custom date format not specified
            if (options.isOptionSet('T')) {
                throw new CliRejectedInputException("can't specify both '-t' and '-T' options");
            }
            // define standard format
            dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        }
        // if custom date format specified,
        if (options.isOptionSet('T')) {
            // define custom format
            dateFormat = new SimpleDateFormat(options.getOptionValue('T'));
        }

        // if 'detail' option or date format is set, then install new console
        if (options.isOptionSet('d') || dateFormat != null) {
            isLineOptionSpecified = true;
            final PrintWriter oldMessageConsole = cliInstance.getMessageConsole();
            final DateFormat finalDateFormat = dateFormat;
            StringValueListener timePrefixingLineOutputter = new StringValueListener() {
                private final StringBuffer mStringBuffer = new StringBuffer();
                public void stringValueNotification(String line) throws IOException {
                    long currentTime = System.currentTimeMillis();
                    mStringBuffer.setLength(0);
                    if (finalDateFormat != null) {
                        mStringBuffer.append(finalDateFormat.format(new Date(currentTime)));
                    }
                    if (options.isOptionSet('d')) {
                        boolean isParenthetical = false;
                        if (mStringBuffer.length() > 0) {
                            mStringBuffer.append(" (");
                            isParenthetical = true;
                        }
                        mStringBuffer.append(String.valueOf(currentTime - startingTime));
                        if (isParenthetical) {
                            mStringBuffer.append(")");
                        }
                    }
                    oldMessageConsole.print(mStringBuffer.toString() + ": " + line);
                    oldMessageConsole.flush();
                }
            };
            LineEventWriter timePrefixingWriter = new LineEventWriter();
            timePrefixingWriter.addListener(timePrefixingLineOutputter);
            newMessageConsole = new PrintWriter(timePrefixingWriter, true);
            cliInstance.setMessageConsole(newMessageConsole);
        }

        // execute user's command
        cliInstance.dispatchCommand(args);

        // roll back any new console that was established
        if (newMessageConsole != null) {
            // revert back to previous console
            cliInstance.unsetMessageConsole(newMessageConsole);
        }

        // print summary if no "line" option was specified,
        // or if the 's' option was specified
        if (!isLineOptionSpecified || options.isOptionSet('s')) {
            long endingTime = System.currentTimeMillis();
            cliInstance.getMessageConsole().println("(command completed in "
                    + (endingTime - startingTime)
                    + " milliseconds)");
        }

    }

    //
    // Private instance methods
    //

    /**
     * Deactivate error display buffer
     */
    private void deactivateErrorDisplayBuffer() {

        // if not active, nothing to do
        if (mErrorDisplayConsole == null) {
            return;
        }

        try {
            // unregister it from CLI
            CliRunner.getInstance().unsetErrorConsole(mErrorDisplayConsole);
        } catch(CliRunnerException cre) {
            throw new CliInvariantViolationException(
                "programmer error; error unsetting error console", cre);
        } finally {
            mErrorDisplayConsole = null;
            mErrorDisplayConsoleBuffer = null;
        }

    }


}
