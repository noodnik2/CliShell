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

package clishell;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import clishell.anno.CliPlugin;
import clishell.db.CliCommandDb;
import clishell.db.CliPluginDb;
import clishell.ex.CliCommandResolutionException;
import clishell.ex.CliException;
import clishell.ex.CliInvariantViolationException;
import clishell.ex.CliPluginMethodException;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;
import org.mortbay.util.WriterOutputStream;

/**
 *
 * Main CLI Class
 *
 */
public class CliRunner {


    //
    // Private class data
    //

    private static final String CLISHELL_REV = "$Rev: 11945 $";

    /** currently active <code>CliRunner</code> instance */
    private static CliRunner sCliRunnerInstance;

    /** file name of "builtin" properties file (must be in same package as this) */
    private static final String PROPFILE_BUILTINS = "clirunner-builtin.properties";

    /** file name of "user" properties file (must be in same package as this) */
    private static final String PROPFILE_USER = "clirunner.properties";

    /** module name prefix for "applications" */
    private static final String MODULENAME_VERSIONPREFIX_JAR = "jar.";

    /** module name prefix for CLI plugins */
    private static final String MODULENAME_VERSIONPREFIX_PLUGIN = "plugin.";


    //
    // Private instance data
    //

    /** Stack of CLI "console" input streams for regular user input */
    private final ConsoleStack<InputStream> mInputConsoles = new ConsoleStack<InputStream>();

    /** Stack of CLI "console" output streams for regular user feedback / messages */
    private final ConsoleStack<PrintWriter> mMessageConsoles = new ConsoleStack<PrintWriter>();

    /**
     * Stack of CLI "console" output streams for error details (stack traces, etc.)
     */
    private final ConsoleStack<PrintWriter> mErrorConsoles = new ConsoleStack<PrintWriter>();

    /**
     * database of plugins
     */
    private final CliPluginDb<CliPluginMetadata> mCliPluginDb
        = new CliPluginDb<CliPluginMetadata>();

    /**
     * database of CLI commands
     */
    private final CliCommandDb mCliCommandDb = new CliCommandDb();

    /**
     * CLI properties
     */
    private CliProperties mCliProperties;

    /** when set to true, CLI command loop will terminate */
    private boolean mQuitFlag;

    /** utility class used for loading CLI plugins */
    private final CliPluginLoader mPluginLoader = new CliPluginLoader();

    /** CLI invocation options */
    private CliCommandOptions mCliOptions = new CliCommandOptions();


    //
    // Public class methods
    //

    /**
     * Invocation options (via <code>args</code>):
     *
     * See command syntax diagram for invocation syntax.
     * @see #printCommandSyntax()
     *
     * @param args external command line arguments
     * @throws CliException unhandled CLI Exception
     */
    public static void main(String[] args) throws CliException {

        CliOptionParser cliOptionParser = new CliOptionParser("hu:p:vxes");
        CliCommandOptions options = new CliCommandOptions();

        int firstArgIndex = cliOptionParser.parseOptions(options, 0, args);
        if (firstArgIndex < args.length) {
            throw new CliRejectedInputException("unrecognized command argument(s)");
        }

        new CliRunner().run(options);

    }

    /**
     * @return global cliInstance in current use
     */
    public static CliRunner getInstance() {
        return sCliRunnerInstance;
    }


    //
    // Public instance methods
    //

    /**
     * @param options CLI invocation options
     */
    public void run(CliCommandOptions options) throws CliException {

        // set this as the current CLI Runner execution instance
        CliRunner.setInstance(this);

        // record the options in effect for this invocation
        setOptions(options);

        // Setup the consoles
        setInputConsole(System.in);
        setMessageConsole(new PrintWriter(System.out, true));
        setErrorConsole(new PrintWriter(System.out, true));

        // sign on
        printSignonBanner();

        // if "help" requested, simply print it and return
        if (options.isOptionSet('h')) {
            printCommandSyntax();
            return;
        }

        // create a list of plugins to be loaded initially
        List<String> initialPluginClassNames = new LinkedList<String>();

        // get quick access to the initially loaded properties
        CliProperties cliRunnerProperties = getProperties();

        // if user wants us to load system properties, let's do it
        if (options.isOptionSet('s')) {
            Properties systemProperties = System.getProperties();
            for (String systemPropertyName : systemProperties.stringPropertyNames()) {
                cliRunnerProperties.setProperty(systemPropertyName,
                    systemProperties.getProperty(systemPropertyName));
            }
        }

        // if user wants us to load system environment, let's do it
        if (options.isOptionSet('e')) {
            Map<String, String> systemEnvironmentMap = System.getenv();
            for (Map.Entry<String, String> systemEnvironmentEntry
                : systemEnvironmentMap.entrySet()) {
                cliRunnerProperties.setProperty(systemEnvironmentEntry.getKey(),
                        systemEnvironmentEntry.getValue());
            }
        }

        // load initial application properties (from known property files)
        //
        // NOTE: we do this AFTER loading the "System" and "Environment" properties,
        //       since in case there is a clash, we'd like THESE properties to override
        //
        loadInitialProperties(cliRunnerProperties);

        // if user wants us to load other property file(s), let's do it
        if (options.isOptionSet('u')) {
            String userPropertyFilenameList = options.getOptionValue('u');
            for (String userPropertyFilename : parseStringList(userPropertyFilenameList)) {
                loadPropertiesFromFile(cliRunnerProperties, userPropertyFilename);
            }
        }

        // register the "preload" plugins configured in the builtin properties file
        // unless user said not to (with 'x' option)
        if (!options.isOptionSet('x')) {
            String preloadPluginClassListString = cliRunnerProperties
                .getProperty("preload-plugins");
            if (preloadPluginClassListString != null) {
                for (String preloadPluginClassName : parseStringList(preloadPluginClassListString)) {
                    initialPluginClassNames.add(preloadPluginClassName);
                }
            }
        }

        // register user plugins to load, if any
        if (options.isOptionSet('p')) {
            String userPluginClassListString = options.getOptionValue('p');
            for (String userPluginClassName : parseStringList(userPluginClassListString)) {
                initialPluginClassNames.add(userPluginClassName);
            }
        }

        // load the initial plugins
        loadPlugins(initialPluginClassNames);

        // invoke any "main" methods defined for the initially loaded plugins
        invokePluginMains();

        // unload all loaded plugins before exiting
        unloadPlugins();

    }

    /**
     * @return map of module to version for all loaded modules reporting their
     * version; note that the format of the version string is not standardized
     */
    public Map<String, String> getVersions() {

        // construct return object
        Map<String, String> versionMap = new LinkedHashMap<String, String>();

        // add in version of main jar module, if it can be determined
        Attributes jarfileMainAttributes = getJarManifestMainAttributes(null);
        if (jarfileMainAttributes != null) {
            String jarfileName = jarfileMainAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            String jarfileVersion = jarfileMainAttributes
                .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            versionMap.put(
                MODULENAME_VERSIONPREFIX_JAR + jarfileName,
                jarfileVersion
            );
        }

        // get version of CliShell JAR even when it's not running in the main jar file...
        URL cliShellCodeSourceURL = getClass().getProtectionDomain()
            .getCodeSource().getLocation();

        if (cliShellCodeSourceURL != null && cliShellCodeSourceURL
                .toExternalForm().endsWith(".jar")) {

            // add in version of main jar module, if it can be determined
            Attributes clijarfileMainAttributes = getJarManifestMainAttributes(
                cliShellCodeSourceURL);

            if (clijarfileMainAttributes != null) {
                String jarfileName = clijarfileMainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_TITLE);
                String jarfileVersion = clijarfileMainAttributes
                    .getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                versionMap.put(
                    MODULENAME_VERSIONPREFIX_JAR + jarfileName,
                    jarfileVersion
                );
            }
        }
        // add in version of any plugins that have specified it, taking
        for (CliPluginMetadata cliPluginMetadata : mCliPluginDb.cliPlugins()) {
            Object pluginInstance = cliPluginMetadata.getPluginInstance();
            CliPlugin cliPlugin = pluginInstance.getClass()
                .getAnnotation(CliPlugin.class);
            if (cliPlugin != null) {
                String pluginVersion = cliPlugin.version();
                if (!"".equals(pluginVersion)) {
                    versionMap.put(
                        MODULENAME_VERSIONPREFIX_PLUGIN + cliPluginMetadata.getName(),
                        pluginVersion
                    );
                }
            }
        }

        // return map of module to its version
        return versionMap;
    }

    //
    //  Management of standard CLI console input
    //

    /**
     * @return console input stream in current use by <code>CliRunner</code>
     * or <code>null</code> if none is defined currently
     */
    public InputStream getInputConsole() {
        return mInputConsoles.getConsole();
    }

    /**
     * @param newInputConsole input stream object to push onto the "stack" of
     * input consoles, and to begin using as the source for console input
     * @throws <code>NullPointerException</code> if <code>newInputConsole</code>
     * is null
     */
    public void setInputConsole(InputStream newInputConsole) {
        mInputConsoles.setConsole(newInputConsole);
    }

    /**
     * Remove a specified console input object from the input console stack
     * @param oldInputConsole input console object that was previously set
     * using <code>setInputConsole()</code>
     * @throws CliRunnerException thrown if attempt to pop last console, or
     * cannot find specified console to unset
     */
    public void unsetInputConsole(InputStream oldInputConsole) throws CliRunnerException {
        mInputConsoles.unsetConsole(oldInputConsole);
    }

    //
    //  Management of standard CLI console message output
    //

    /**
     * @return message console output stream in current use by <code>CliRunner</code>,
     * or <code>null</code> if none is defined currently
     */
    public PrintWriter getMessageConsole() {
        return mMessageConsoles.getConsole();
    }

    /**
     * @param newMessageConsole print writer object to push onto the "stack" of
     * message consoles, and to begin using as the destination for console messages
     * @throws <code>NullPointerException</code> if <code>newMessageConsole</code>
     * is null
     */
    public void setMessageConsole(PrintWriter newMessageConsole) {
        mMessageConsoles.setConsole(newMessageConsole);
        synchronizeStdout();
    }

    /**
     * Remove a specified console writer from the message console writer stack
     * @param oldMessageConsole message console writer that was previously set
     * using <code>setMessageConsole()</code>
     * @throws CliRunnerException thrown if attempt to pop last console, or
     * cannot find specified console to unset
     */
    public void unsetMessageConsole(PrintWriter oldMessageConsole) throws CliRunnerException {
        mMessageConsoles.unsetConsole(oldMessageConsole);
        synchronizeStdout();
    }

    //
    //  Management of standard CLI console error output
    //

    /**
     * @return error console output stream in current use by <code>CliRunner</code>
     * or <code>null</code> if none is defined currently
     */
    public PrintWriter getErrorConsole() {
        return mErrorConsoles.getConsole();
    }

    /**
     * @param newErrorConsole print writer object to push onto the "stack" of
     * error consoles, and to begin using as the destination for console errors
     * @throws <code>NullPointerException</code> if <code>newErrorConsole</code>
     * is null
     */
    public void setErrorConsole(PrintWriter newErrorConsole) {
        mErrorConsoles.setConsole(newErrorConsole);
        System.setErr(new PrintStream(new WriterOutputStream(newErrorConsole)));
        synchronizeStderr();
    }

    /**
     * Remove a specified console writer from the error console writer stack
     * @param oldErrorConsole error console writer that was previously set
     * using <code>setErrorConsole()</code>
     * @throws CliRunnerException thrown if attempt to pop last console, or
     * cannot find specified console to unset
     */
    public void unsetErrorConsole(PrintWriter oldErrorConsole) throws CliRunnerException {
        mErrorConsoles.unsetConsole(oldErrorConsole);
        synchronizeStderr();
    }

    /**
     * @param pluginClassSpecIterable iterable to collection of plugin class specs;
     * each carrying the java class name and optionally the CLI plugin name override
     * for the plugin, in the form:
     *
     *   java-plugin-class-name[/CLI-plugin-name-override]
     *
     * NOTE: there is no requirement to load and/or to initialize the plugins
     *       in the order they are enumerated by the passed Iterable
     *
     */
    public void loadPlugins(Iterable<String> pluginClassSpecIterable) {

        for (String pluginClassSpec : pluginClassSpecIterable) {

            String[] pluginClassSpecParts = pluginClassSpec.split("/");
            String pluginClassName = pluginClassSpecParts[0];
            String pluginClassNameOverride = null;
            if (pluginClassSpecParts.length > 1) {
                pluginClassNameOverride = pluginClassSpecParts[1];
            }

            CliPluginMetadata cliPluginMetadata;

            try {
                cliPluginMetadata = mPluginLoader.loadFromClassName(
                        pluginClassName, pluginClassNameOverride);
            } catch(CliRunnerException cre) {
                getMessageConsole().println("error loading plugin class: '"
                    + pluginClassName
                    + "', "
                    + cre.getMessage());
                cre.printStackTrace(getErrorConsole());
                continue;
            }

            // get the (unique) name by which the plugin
            // wants to be identified
            String pluginName = cliPluginMetadata.getName();

            // save plugin in plugin database
            try {
                mCliPluginDb.addPlugin(pluginName, cliPluginMetadata);
            } catch(CliRejectedInputException crie) {
                getMessageConsole().println("error registering plugin: '"
                    + pluginName
                    + "', "
                    + crie.getMessage());
                crie.printStackTrace(getErrorConsole());
                continue;
            }

            // load command names into command tree
            // if it fails, report the error, unregister the plugin and continue
            try {
                mCliCommandDb.addCommands(pluginName, cliPluginMetadata.getCommands());
            } catch(CliRunnerException cre) {
                getMessageConsole().println("error loading CLI commands from plugin: '"
                    + pluginName
                    + "', "
                    + cre.getMessage());
                cre.printStackTrace(getErrorConsole());
                mCliPluginDb.removePlugin(pluginName);
                continue;
            }

            // initialize the plugin AFTER it's been registered
            // if it fails, report the error, unregister the plugin
            // and its commands, and continue
            try {
                mPluginLoader.initializePlugin(cliPluginMetadata);
            } catch(CliRunnerException cre) {
                getMessageConsole().println("error initializing plugin: '"
                    + pluginName
                    + "', "
                    + cre.getMessage());
                cre.printStackTrace(getErrorConsole());
                mCliCommandDb.removeCommands(pluginName);
                mCliPluginDb.removePlugin(pluginName);
                continue;
            }

            // print confirmatory message
            getMessageConsole().println("plugin '"
                    + pluginName
                    + "' ("
                    + pluginClassName
                    + ") loaded");
        }
    }

    /**
     * @param userCommandline user command line
     * @throws CliRunnerException unhandled exception
     * @throws NullPointerException thrown if <code>userCommandline</code> is
     * <code>null</code> (among other possible reasons)
     */
    public void dispatchCommand(String userCommandline) throws CliRunnerException {

        if (userCommandline == null) {
            throw new NullPointerException();
        }

        // parse the command line into tokens and dispatch that
        dispatchCommand(CliCommandParser.parseTokens(userCommandline));
    }

    /**
     * @param userCommandTokens tokenized user command line
     * @throws CliRunnerException unhandled exception
     * @throws NullPointerException thrown if <code>userCommandTokens</code> is
     * <code>null</code> (among other possible reasons)
     */
    public void dispatchCommand(String[] userCommandTokens) throws CliRunnerException {

        if (userCommandTokens == null) {
            throw new NullPointerException();
        }

        // handle plugin hint prefix, if present
        String pluginNameHint = null;
        if (userCommandTokens.length >= 1) {
            if ((userCommandTokens[0].length() > 1)
                    && (userCommandTokens[0].endsWith(":"))) {
                pluginNameHint = userCommandTokens[0].substring(0,
                        userCommandTokens[0].length() - 1);
                userCommandTokens = Arrays.copyOfRange(userCommandTokens,
                        1, userCommandTokens.length);
            }
        }

        // execute command
        try {

            dispatchCommand(pluginNameHint,
                    new CommandName(userCommandTokens));

        } catch(CliPluginMethodException crce) {
            getMessageConsole().println("Command Exception: " + crce.getMessage());
            crce.printStackTrace(mErrorConsoles.getConsole());
            // fall through
        } catch(CliCommandResolutionException ccre) {
            getMessageConsole().println("Command Resolution Exception: " + ccre.getMessage());
            // fall through
        } catch(CliRejectedInputException iae) {
            getMessageConsole().println("illegal argument: " + iae.getMessage());
            iae.printStackTrace(mErrorConsoles.getConsole());
            // fall through
        }

    }

    /**
     * @param inputStream input stream to "feed" to command as its input
     * (if <code>null</code>, will use existing input stream)
     * @param messageWriter writer to receive command message output
     * (if <code>null</code>, will use existing message writer)
     * @param errorWriter writer to receive command error output
     * (if <code>null</code>, will use existing error writer)
     * @param commandTokens tokenized command string
     * @throws CliRunnerException unhandled exception
     */
    public void dispatchCommand(InputStream inputStream, PrintWriter messageWriter,
        PrintWriter errorWriter, String... commandTokens) throws CliRunnerException {

        if (inputStream != null) {
            setInputConsole(inputStream);
        }

        if (messageWriter != null) {
            setMessageConsole(messageWriter);
        }

        if (errorWriter != null) {
            setErrorConsole(errorWriter);
        }

        CliRunnerException cliRunnerException = null;

        // execute user's command with new "teeWriter console"\
        try {

            dispatchCommand(commandTokens);

        } catch(CliRunnerException dispatchException) {

            cliRunnerException = dispatchException;

        } finally {

            if (inputStream != null) {
                // revert back to previous input stream
                try {
                    unsetInputConsole(inputStream);
                } catch(CliRunnerException unsetException) {
                    if (cliRunnerException != null) {
                        cliRunnerException.add(unsetException);
                    }
                }
            }

            if (messageWriter != null) {
                // revert back to previous message writer
                try {
                    unsetMessageConsole(messageWriter);
                } catch(CliRunnerException unsetException) {
                    if (cliRunnerException != null) {
                        cliRunnerException.add(unsetException);
                    }
                }
            }

            if (errorWriter != null) {
                // revert back to previous error writer
                try {
                    unsetErrorConsole(errorWriter);
                } catch(CliRunnerException unsetException) {
                    if (cliRunnerException != null) {
                        cliRunnerException.add(unsetException);
                    }
                }
            }

        }

        if (cliRunnerException != null) {
            throw cliRunnerException;
        }

    }

    /**
     * Unloads all currently loaded plugins from CLI
     * @throws CliRunnerException unhandled exception thrown while unloading plugins
     * @see #unloadPlugins(Iterable)
     */
    public void unloadPlugins() throws CliRunnerException {
        unloadPlugins(mCliPluginDb.cliPluginNames());
    }

    /**
     * Unloads the specified plugin(s) from the CLI
     * @param pluginNamesIterable iterable specifying name(s) of plugin(s) to unload
     */
    public void unloadPlugins(Iterable<String> pluginNamesIterable) {

        for (String pluginName : pluginNamesIterable) {
            CliPluginMetadata cliPluginMetadata = mCliPluginDb.getPlugin(pluginName);
            if (cliPluginMetadata == null) {
                getMessageConsole().println("WARNING: plugin '"
                    + pluginName
                    + "' not found");
                continue;
            }
            try {
                mPluginLoader.finalizePlugin(cliPluginMetadata);
            } catch(CliRunnerException cre) {
                getMessageConsole().println("error finalizing plugin: '"
                    + pluginName
                    + "', "
                    + cre.getMessage());
                cre.printStackTrace(getErrorConsole());
            }

            // unregister the plugin commands and the plugin itself
            mCliCommandDb.removeCommands(pluginName);
            mCliPluginDb.removePlugin(pluginName);

            // print confirmatory message
            getMessageConsole().println("plugin '"
                    + pluginName
                    + "' unloaded");
        }

    }

    /**
     * @param pluginName name of plugin whose instance is being located
     * @return object instance of specified cli plugin
     */
    public Object getPluginInstance(String pluginName) {
        CliPluginMetadata metadata = mCliPluginDb.getPlugin(pluginName);
        if (metadata != null) {
            return metadata.getPluginInstance();
        }
        return null;
    }

    /**
     * Retrieve information about all loaded plugin(s) in the form of
     * a (new) set of map entries.
     * NOTE: it is unsupported behavior for the caller to modify the objects
     * referenced by the entries in the returned set.
     * @return Iterable<Map.Entry<String, CliPluginMetadata>> copy of "entry set"
     * of internal plugins backing map
     */
    public Iterable<CliPluginMetadata> getPlugins() {
        return mCliPluginDb.cliPlugins();
    }

    /**
     * Cause the CLI to quit instead of reading another command
     */
    public void quit() {
        mQuitFlag = true;
    }

    /**
     * @return true if CLI debug mode is enabled
     */
    public boolean isCliDebug() {
        return getOptions().isOptionSet('v');
    }

    /**
     * @return true if CLI debug mode has quit
     */
    public boolean isCliQuit() {
        return mQuitFlag;
    }

    /**
     * @return CLI application properties
     * will not be <code>null</code>
     */
    public CliProperties getProperties() {

        if (mCliProperties == null) {
            mCliProperties = new CliProperties();
        }

        return mCliProperties;
    }

    /**
     * @return CLI invocation options
     * will not be <code>null</code>
     */
    public CliCommandOptions getOptions() {
        return mCliOptions;
    }


    //
    // Private class methods
    //

    /**
     * @param cliRunner cliRunner instance to set
     */
    private static void setInstance(CliRunner cliRunner) {
        sCliRunnerInstance = cliRunner;
    }


    //
    // Private instance methods
    //

    /**
     * @param options options to set (may not be <code>null</code>
     */
    private void setOptions(CliCommandOptions options) {
        if (options == null) {
            throw new NullPointerException();
        }
        mCliOptions = options;
    }

    /**
     * Invokes the "Main" methods of all loaded plugins
     * Should invoke the methods in the following order:
     *    by plugin load order
     *       by user-specified method order within the plugin
     *
     * @throws CliRunnerException no 'main' method(s) were executed
     */
    private void invokePluginMains() throws CliRunnerException {

        boolean executedMain = false;

        // iterate over all registered plugins
        for (CliPluginMetadata cliPluginMetadata : mCliPluginDb.cliPlugins()) {

            // grab plugin instance object
            Object cliPluginInstance = cliPluginMetadata.getPluginInstance();

            // iterate over all "main" method metadata for each plugin
            for (CliPluginMethodMetadata cliPluginMethodMetadata : cliPluginMetadata.getMains()) {

                // get the main method from the plugin method metadata
                CliMethod mainMethod = cliPluginMethodMetadata.getCliMethod();

                // invoke the "main" method
                try {

                    mainMethod.invoke(cliPluginInstance);
                    executedMain = true;

                } catch(CliRunnerException cre) {

                    String userMessage = "Exception from 'main' method: "
                        + mainMethod.getMethodSignature()
                        + "' of cli plugin: '"
                        + cliPluginMetadata.getName()
                        + "'";

                    if (cre.getMessage() != null) {
                        userMessage += ": " + cre.getMessage();
                    }

                    getErrorConsole().println(userMessage);

                    getErrorConsole().println(this.getClass().getSimpleName()
                        + " terminating due to exception; details follow...");

                    cre.printStackTrace(getErrorConsole());
                }
            }
        }

        if (!executedMain) {
            throw new CliRunnerException(
                "No 'main' CLI plugin method(s) were successfully executed; check configuration");
        }

    }

    /**
     * @param pluginNameHint hint given by user for which plugin should
     * perform this command
     * @param commandName tokenized input command line
     * @throws CliException unhandled exception during processing of command
     */
    private void dispatchCommand(String pluginNameHint,
            CommandName commandName)
        throws CliRunnerException {

        //
        //  ------------------- TODO: START OF NEED TO COMBINE
        //
        //  The problem: as of this writing (Aug 2011) the next line looks
        //  for a unique command across all plugins; however, we have in-hand
        //  a "plugin hint" that the user specified, telling us which plugin(s)
        //  we should restrict our search for commands to, yet we're not using
        //  that in the "resolveUserCommandInput()" method.  Therefore, we'll
        //  tell the user that his command is not unique when in fact it might
        //  be unique within the plugin s/he told us we should look in.  The
        //  very next statement (resolveCommandPlugin()) DOES look at the
        //  plugin specified by the user, but only for the SINGLE command
        //  that was failed to be located by the previous method call
        //  because it failed to restrict its search.  Solution: combine
        //  the logic used in both of the methods below so that it calculates
        //  the INTERSECTION of the plugin hint (filter on plugins) and the
        //  command name abbreviation (filter on commands).
        //

        // resolve what the user entered into a single "full command"
        // that we can lookup using our database of commands and plugins
        CommandName foundFullCommand = resolveUserCommandInput(commandName);

        // resolve the "full command" into a single plugin supporting that command
        CliPluginMetadata foundPluginMetadata = resolveCommandPlugin(pluginNameHint,
                foundFullCommand);

        //
        //  ------------------- TODO: END OF NEED TO COMBINE
        //


        // retrieve the plugin object instance
        Object pluginInstance = foundPluginMetadata.getPluginInstance();

        // lookup the command metadata
        CliPluginCommandMetadata commandMetadata = mCliCommandDb.getCommandMetadata(
            foundPluginMetadata.getName(), foundFullCommand);

        // assertion: catch some future programming error
        if (commandMetadata == null) {
            throw new CliInvariantViolationException("Command Metadata not found for: '"
                + foundPluginMetadata.getName()
                + ": "
                + foundFullCommand
                + "'");
        }

        // parse command options
        CliCommandOptions commandOptions = new CliCommandOptions();
        String[] commandNameAsWordArray = commandName.getCommandNameAsWordArray();
        int firstCommandArgumentIndex = foundFullCommand.getCommandNameAsWordArray().length;
        if (commandMetadata.getOptionParser() != null) {
            firstCommandArgumentIndex = commandMetadata.getOptionParser().parseOptions(
                commandOptions, firstCommandArgumentIndex, commandNameAsWordArray);
        }

        CliMethod cliMethod = commandMetadata.getCliMethod();

        // verify the requisite number of arguments are being supplied to the command
        int nArgsSupplied = commandNameAsWordArray.length - firstCommandArgumentIndex;
        String wrongNumberOfArgumentsMessage = null;

        int minArgs = commandMetadata.getMinArgs();
        if (minArgs >= 0) {
            if (nArgsSupplied < minArgs) {
                wrongNumberOfArgumentsMessage = "too few";
            }
        }

        int maxArgs = commandMetadata.getMaxArgs();
        if (maxArgs >= 0) {
            if (nArgsSupplied > maxArgs) {
                wrongNumberOfArgumentsMessage = "too many";
            }
        }

        if (wrongNumberOfArgumentsMessage != null) {
            throw new CliRejectedInputException(wrongNumberOfArgumentsMessage
                + " command argument(s) supplied");
        }

        // invoke the command on the plugin instance,
        // passing the full set of tokens entered by the user for the command,
        // indicating where the command arguments begin within the set of tokens
        cliMethod.invokeCliCommand(pluginInstance, commandOptions,
            commandNameAsWordArray, firstCommandArgumentIndex);

    }

    /**
     * @param userCommandNameWithArguments full command entered by user
     * (the command words may be abbreviated, and contains command arguments)
     * @return the complete set of unabbreviated command words which unambiguously
     * matched the supplied <code>userCommandNameWithArguments</code>, or
     * @throws CliCommandResolutionException if unambiguous command was not
     * recognizable from provided <code>userCommandNameWithArguments</code>
     */
    private CommandName resolveUserCommandInput(CommandName userCommandNameWithArguments)
        throws CliCommandResolutionException {

        // get the set of possible commands the user might have intended
        // based upon complete, incomplete, or abbreviated command entry
        Set<CommandName> foundCommands = mCliCommandDb
            .findCommandNamesFromCommandLine(userCommandNameWithArguments);

        // if exactly one command was found, then we might have it!
        if (foundCommands.size() == 1) {
            // we have it only if it doesn't contain more words than what was entered
            if (foundCommands.iterator().next().getCommandNameAsWordArray().length
                <= userCommandNameWithArguments.getCommandNameAsWordArray().length) {
                return foundCommands.iterator().next();
            }
            // TODO investigate this case - only one found, but still ambiguous??
        }

        // if command was not found, or was ambiguous,
        // then reflect to user and return
        if (foundCommands.size() == 0) {
            throw new CliCommandResolutionException("command '"
                + userCommandNameWithArguments
                + "' could not be resolved to a known command");
        }
        StringBuffer ambiguousCommandsStringBuffer = new StringBuffer();
        ambiguousCommandsStringBuffer.append("command: '"
            + userCommandNameWithArguments
            + "' is ambiguous; could resolve to { ");
        boolean isFirst = true;
        for (CommandName commandName : foundCommands) {
            if (!isFirst) {
                ambiguousCommandsStringBuffer.append(", ");
            } else {
                isFirst = false;
            }
            ambiguousCommandsStringBuffer.append("'" + commandName + "'");
        }
        ambiguousCommandsStringBuffer.append(" }");

        throw new CliCommandResolutionException(
            ambiguousCommandsStringBuffer.toString());

    }

    /**
     * @param pluginNameHint hint given by user as to which plugin
     * should execute this command
     * @param fullCommandName complete set of unabbreviated command words
     * @return metadata of the plugin command that should be used to
     * execute this command
     * @throws CliRunnerException could not find metadata for specified command
     * @throws CliCommandResolutionException command is ambiguous because it is
     * supported by more than one plugin; user should use plugin hint
     */
    private CliPluginMetadata resolveCommandPlugin(String pluginNameHint,
            CommandName fullCommandName)
        throws CliRunnerException, CliCommandResolutionException {

        // set of all plugins supporting the named command
        Set<CliPluginMetadata> supportingPlugins = new LinkedHashSet<CliPluginMetadata>();

        // loop across all plugins to find which one implements the command
        // consider user's hint about which plugins to use as well
        for (CliPluginMetadata pluginMetadata : mCliPluginDb.cliPlugins()) {
            String pluginName = pluginMetadata.getName();
            if (mCliCommandDb.getCommandMetadata(pluginName, fullCommandName) != null) {
                if (pluginNameHint != null) {
                    if (!pluginName.startsWith(pluginNameHint)) {
                        // plugin doesn't match user's hint - don't consider it
                        continue;
                    }
                }
                supportingPlugins.add(pluginMetadata);
            }
        }

        // exactly one plugin supporting this command - return it!
        if (supportingPlugins.size() == 1) {
            return supportingPlugins.iterator().next();
        }

        // that means either zero plugins, ...
        if (supportingPlugins.size() == 0) {
            String notFoundCommand;
            if (pluginNameHint != null) {
                notFoundCommand = pluginNameHint + ": " + fullCommandName.toString();
            } else {
                notFoundCommand = fullCommandName.toString();
            }
            throw new CliCommandResolutionException("plugin"
                + " not found for command '"
                + notFoundCommand
                + "'");
        }

        // or more than one plugin was found supporting command...
        StringBuffer ambiguousCommandsStringBuffer = new StringBuffer();
        ambiguousCommandsStringBuffer.append("command: '"
            + fullCommandName
            + "' is supported by more than one plugin { ");
        boolean isFirst = true;
        for (CliPluginMetadata pluginMetadata : supportingPlugins) {
            if (!isFirst) {
                ambiguousCommandsStringBuffer.append(", ");
            } else {
                isFirst = false;
            }
            ambiguousCommandsStringBuffer.append("'" + pluginMetadata.getName() + "'");
        }
        ambiguousCommandsStringBuffer.append(" }; use plugin name prefix: hint to disambiguate");

        throw new CliCommandResolutionException(
            ambiguousCommandsStringBuffer.toString());

    }

    /**
     * @param cliRunnerProperties container for loading properties into
     */
    private void loadInitialProperties(CliProperties cliRunnerProperties) {

        // list of property files we'll load
        final String[] propertyFilenames = {
            PROPFILE_BUILTINS
          , PROPFILE_USER
          ,
        };

        // load all of the property files we find
        for (String propFilename : propertyFilenames) {

            InputStream propertiesInputStream = getClass()
                .getResourceAsStream(propFilename);

            if (propertiesInputStream == null) {
                continue;
            }

            try {
                cliRunnerProperties.load(propertiesInputStream);
            } catch(IOException ioex) {
                getMessageConsole().println("could not load property file: '"
                    + propFilename
                    + "': "
                    + ioex.getMessage());
                ioex.printStackTrace(getErrorConsole());
            }

            try {
                propertiesInputStream.close();
            } catch(IOException ioex2) {
                getMessageConsole().println("could not close property file: '"
                    + propFilename
                    + "': "
                    + ioex2.getMessage());
                ioex2.printStackTrace(getErrorConsole());
            }

        }

    }

    /**
     * @param cliRunnerProperties CLI property object to load properties into
     * @param propertyFileName complete file path to user property file
     * @throws CliRunnerException unhandled exception reading / accessing property file
     */
    private void loadPropertiesFromFile(CliProperties cliRunnerProperties,
            String propertyFileName)
        throws CliRunnerException {
        try {
            cliRunnerProperties.load(new FileReader(propertyFileName));
        } catch(FileNotFoundException fnfe) {
            throw new CliRunnerException("can't open property file: \""
                    + propertyFileName
                    + "\"",
                    fnfe);
        } catch(IOException ioe) {
            throw new CliRunnerException("I/O error reading property file: \""
                    + propertyFileName
                    + "\"",
                    ioe);
        }

    }

    /**
     * @param stringList string 'list' containing zero or more
     * (possibly quoted) entries, each separated by whitespace or comma
     */
    private String[] parseStringList(String stringList) {
        return StringListParser.parseTokens(stringList, new char[] {','});
    }

    /**
     * Prints help diagram.
     */
    private void printSignonBanner() {

        String[] bannerLines = {
            "CliShell " + CLISHELL_REV
        ,   ""
        ,
        };

        PrintWriter consoleOut = getMessageConsole();
        for (String bannerLine : bannerLines) {
            consoleOut.println(bannerLine);
        }

    }

    /**
     * Prints help diagram.
     */
    private void printCommandSyntax() {

        String[] syntaxLines = {
            "cmd [option(s)]"
        ,
        };

        String[] optionLines = {
            "-h"
        ,   "   (Prints this command syntax diagram)"
        ,   ""
        ,   "-p plugin-class[/name][,plugin-class[/name][,...]]"
        ,   "   (Loads specified plugin(s))"
        ,   ""
        ,   "-u user-property-file[,user-property-file[,...]]"
        ,   "   (Loads specified property file(s))"
        ,   ""
        ,   "-x"
        ,   "   (Does not load \"builtin\" plugins)"
        ,   ""
        ,   "-s"
        ,   "   (Loads java \"system\" properties)"
        ,   ""
        ,   "-e"
        ,   "   (Loads system \"environment\" as properties)"
        ,   ""
        ,   "-v"
        ,   "   (Enables verbose output)"
        ,
        };

        PrintWriter consoleOut = getMessageConsole();
        consoleOut.println("Syntax:");
        for (String syntaxLine : syntaxLines) {
            consoleOut.println("    " + syntaxLine);
        }
        consoleOut.println();

        consoleOut.println("Options:");
        for (String optionLine : optionLines) {
            consoleOut.println("    " + optionLine);
        }
        consoleOut.println();

    }

    /**
     * @param jarURL URL of jarfile, or <code>null</code>
     * to use the manifest from the root of the currently
     * running class.
     * @return main attributes from the specified jar file,
     * or <code>null</code> if could not find / load them
     */
    private Attributes getJarManifestMainAttributes(URL jarURL) {

        //
        // if the user asked for the version of a JAR via URL, do this:
        //

        if (jarURL != null) {
            InputStream inputStream = null;
            JarInputStream jarInputStream = null;
            try {
                inputStream = jarURL.openStream();
                jarInputStream = new JarInputStream(inputStream);
                Manifest manifest = jarInputStream.getManifest();
                return manifest.getMainAttributes();
            } catch(IOException ioex) {
                ioex.printStackTrace(getErrorConsole());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch(IOException ioex) {
                        ;   // ignore
                    }
                }
                if (jarInputStream != null) {
                    try {
                        jarInputStream.close();
                    } catch(IOException ioex) {
                        ;   // ignore
                    }
                }
            }
        }

        //
        // else try to get the version from the "manifest file"
        // in the standard location in the current classpath
        //

        InputStream manifestInputStream = getClass()
            .getResourceAsStream("/META-INF/MANIFEST.MF");

        if (manifestInputStream != null) {
            Manifest manifest;
            try {
                manifest = new Manifest(manifestInputStream);
                return manifest.getMainAttributes();
            } catch(IOException ioex) {
                ioex.printStackTrace(getErrorConsole());
            } finally {
                try {
                    manifestInputStream.close();
                } catch(IOException ioex) {
                    ;   // ignore
                }
            }
        }

        // didn't find it
        return null;
    }

    /**
     * Sets Java's "standard output" stream (System.out) to the
     * same value as returned by <code>getMessageConsole()</code>
     * (well, almost; now it needs conversion - which may be not
     * so good - we can go back to using a <code>PrintStream</code>
     * instead of a <code>PrintWriter</code> if this conversion
     * proves problematic)
     * Call this method whenever the value returned by the method
     * <code>getMessageConsole()</code> changes.
     */
    private void synchronizeStdout() {
        System.out.flush();
        System.setOut(new PrintStream(new WriterOutputStream(getMessageConsole())));
    }

    /**
     * Sets Java's "standard error" stream (System.err) to the
     * same value as returned by <code>getErrorConsole()</code>
     * (well, almost; now it needs conversion - which may be not
     * so good - we can go back to using a <code>PrintStream</code>
     * instead of a <code>PrintWriter</code> if this conversion
     * proves problematic)
     * Call this method whenever the value returned by the method
     * <code>getErrorConsole()</code> changes.
     */
    private void synchronizeStderr() {
        System.err.flush();
        System.setErr(new PrintStream(new WriterOutputStream(getErrorConsole())));
    }

}
