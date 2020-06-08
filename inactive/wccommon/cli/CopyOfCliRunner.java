/**
 * 
 *
 *  
 *
 *
 * 
 * @author MRoss
 * 
 */

package wccommon.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import wccommon.CliHelper;
import wccommon.CliRunnerCommand;

import wt.log4j.LogR;
import wt.util.WTException;

/**
 * 
 * @author mross
 *
 */
public class CopyOfCliRunner extends CliHelper {
    
    public static final String CLIRUNNER_LOGGERNAME = CopyOfCliRunner.class.getName();
    
    private static final Logger sLogger = LogR.getLogger(CLIRUNNER_LOGGERNAME);
    private static CopyOfCliRunner sCliRunnerInstance;
    
    private PrintStream mConsoleOut = System.out;
    private Map<String, CliRunnerCommand> mCommands = new HashMap<String, CliRunnerCommand>();
    private Map<String, CliRunnerPlugin> mPlugins = new HashMap<String, CliRunnerPlugin>();

    /**
     * @return singleton instance of running CLI 
     */
    public static CopyOfCliRunner getInstance() {
        return sCliRunnerInstance;
    }
    
    /**
     * @param commandList list of CLI commands to add
     */
    public void addCommands(CliRunnerCommand[] commandList) {
        if (commandList == null) {
            return;
        }
        for (CliRunnerCommand command : commandList) {
            sCliRunnerInstance.mCommands.put(command.getName(), command);
        }
    }

    /**
     * 
     * Invocation options:
     * 
     *     -d           Enable debug output for <code>AimService</code>
     *     -i klass     Name of <code>AimService</code> implementation class 
     * 
     * @param args external command line arguments
     * @throws IOException unhandled I/O Exception
     */
    public static void main(String[] args) throws CliRunnerException {
        
        CopyOfCliRunner cliRunnerInstance = new CopyOfCliRunner();
        
        List<String> pluginClassNames = new LinkedList<String>();
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                sLogger.setLevel(Level.ALL);
            }
            if ("-p".equals(args[i])) {
                pluginClassNames.add(cliRunnerInstance.getCommandArgument(args, ++i));
            }
        }
        
        sCliRunnerInstance = cliRunnerInstance;
        sCliRunnerInstance.loadPluginClasses(pluginClassNames);
        sCliRunnerInstance.run();
        sCliRunnerInstance.unloadPluginClasses();

    }
    
    /**
     * Runs the command line interface
     */
    private void run() throws CliRunnerException {

        mConsoleOut.println("CliRunner Waiting for Input ('help' for list of commands)");

        // loop while there are commands
        while(true) {

            mConsoleOut.print(">> ");
            mConsoleOut.flush();

            // read command
            String userCommandLine = null;
            try {
                if ((userCommandLine = readCommandLine()) == null) {
                    break;
                }
            } catch(IOException ioe) {
                mConsoleOut.println("I/O Exception reading input - aborting");
                break;
            }

            // execute command
            try {
                if (!dispatchCommand(parseCommandLine(userCommandLine))) {
                    break;
                }
            } catch(IllegalArgumentException iae) {
                mConsoleOut.println(iae.getMessage());
            } catch(IOException ioe) {
                mConsoleOut.println("I/O Exception");
                ioe.printStackTrace();
            } catch(WTException wte) {
                mConsoleOut.println("Windchill Exception");
                wte.printStackTrace();
            }
        }

        // let user know we're leaving
        mConsoleOut.println("bye!");
    }

    /**
     * @param cliCommandLineArgs input command line tokens
     * @throws WTException unhandled Windchill exception
     * @throws IllegalArgumentException invalid user argument
     * @throws IOException unhandled I/O Exception
     * @throws CliRunnerException unhandled exception during processing of command
     */
    private boolean dispatchCommand(String[] cliCommandLineArgs)
    throws WTException, IllegalArgumentException, IOException, CliRunnerException {
        
        String userCommandVerb = getCommandArgument(cliCommandLineArgs, 0);

        if (userCommandVerb.equalsIgnoreCase("exit") || userCommandVerb.equalsIgnoreCase("quit")) {
            return false;
        }

        if (userCommandVerb.equalsIgnoreCase("echo")) {
            for (String commandToken : cliCommandLineArgs) {
                mConsoleOut.println("\"" + commandToken + "\"");
            }
            return true;
        }

        if ("help".equalsIgnoreCase(userCommandVerb)) {
            sLogger.debug("NOTE: debug is enabled");
            for (String syntaxDiagram : getSyntaxDiagrams()) {
                mConsoleOut.println(syntaxDiagram);
            }
            return true;
        }

        
        
        throw new IllegalArgumentException("unrecognized command: '"
                + userCommandVerb + "' (use 'help' for help)");
        
    }

    /**
     * @return list of help (syntax diagram) strings
     */
    private List<String> getSyntaxDiagrams() {
        List<String> helpStringList = new LinkedList<String>();
        for (CliRunnerCommand cliRunnerCommand : mCommands.values()) {
            helpStringList.add(cliRunnerCommand.getSyntaxDiagram());
        }
        return helpStringList;
    }
    
    /**
     * @param pluginClassNamesIterator
     * @throws CliRunnerException
     */
    private void loadPluginClasses(Iterable<String> pluginClassNamesIterator) 
    throws CliRunnerException {
        
        Iterator<String> i = pluginClassNamesIterator.iterator();
        while(i.hasNext()) {
            CliRunnerPlugin cliRunnerCommandsPlugin = loadPluginClass(i.next());
        }
    }
    
    /**
     * @throws CliRunnerException
     */
    private void unloadPluginClasses() throws CliRunnerException {
        for (String pluginClassName : mPlugins.keySet()) {
            unloadPluginClass(pluginClassName);
        }
    }
    
    /**
     * @param pluginClassNamesIterator
     * @throws CliRunnerException
     */
    private void unloadPluginClasses(Iterable<String> pluginClassNamesIterator) throws CliRunnerException {
        Iterator<String> i = pluginClassNamesIterator.iterator();
        while(i.hasNext()) {
            unloadPluginClass(i.next());
        }
    }
    
    /**
     * @param pluginClassName
     * @returns CliRunnerCommandsPlugin
     * @throws CliRunnerException
     */
    private CliRunnerPlugin loadPluginClass(String pluginClassName) throws CliRunnerException {

        unloadPluginClass(pluginClassName);
        try {
            Class<CliRunnerPlugin> pluginClass 
                = (Class<CliRunnerPlugin>) Class.forName(pluginClassName);
            CliRunnerPlugin cliRunnerCommandsPlugin = pluginClass.newInstance();
            cliRunnerCommandsPlugin.init();
            mPlugins.put(pluginClassName, cliRunnerCommandsPlugin);
            return cliRunnerCommandsPlugin;
        } catch(Exception e) {
            throw new CliRunnerException("unable to initialize plugin class: '"
                    + pluginClassName + "'");
        }
    }
    
    /**
     * @param pluginClassName
     * @throws CliRunnerException
     */
    private void unloadPluginClass(String pluginClassName) throws CliRunnerException {
        CliRunnerPlugin cliRunnerCommandsPlugin = mPlugins.remove(pluginClassName);
        if (cliRunnerCommandsPlugin != null) {
            cliRunnerCommandsPlugin.fini();
        }
    }
    
}
