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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import clishell.CliCommandOptions;
import clishell.CliProperties;
import clishell.CliRunner;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.anno.CliPluginMain;
import clishell.ex.CliRunnerException;

@CliPlugin(
     name = "file-input"
, version = "0.1.$Rev: 9094 $"
)
public class FileInputPlugin {


    //
    //  Public class data
    //

    public static final String INITPROPERTY_CLISCRIPTS = "initialization-cliscripts";


    //
    // Private instance data
    //

    /**
     * quit flag - set to true if this plugin is unloaded,
     * causes command loop to terminate
     */
    private boolean mQuit;


    //
    //  Public plugin initialization / finalization methods
    //

    @CliPluginMain
    /**
     * run any initialization scripts that have been defined
     */
    public void main() throws CliRunnerException, IOException {

        CliRunner cliRunner = CliRunner.getInstance();

        // see if there are any initialization scripts configured
        String initScripts = cliRunner.getProperties()
            .getProperty(INITPROPERTY_CLISCRIPTS);

        // if there are none, nothing to do
        if (initScripts == null) {
            return;
        }

        // execute all scripts, in order
        for (String scriptName : initScripts.split("[\\s,]+")) {
            try {
                runScript(new CliCommandOptions(), scriptName);
            } catch(IOException ioex) {
                cliRunner.getMessageConsole().println(scriptName
                    + ": "
                    + ioex.getMessage());
                ioex.printStackTrace(cliRunner.getErrorConsole());
            }
        }
    }

    /**
     * Plugin finalization
     */
    @CliPluginFinalizer
    public void fini() {
        mQuit = true;
    }


    //
    //  Public plugin command methods
    //

    @CliPluginCommand(
         names = { "cliscript", "cscript" }
    ,  options = "q"
    ,   syntax = "[-q] <script-file> [<arg> [<arg> [...]]]"
    , helptext = {
                "Runs the specified CLI script, optionally passing arguments."
              , "If the '-q' option is specified, will not echo the commands"
              , "to the console as they are executed."
            }
    , minargs = 1
    )
    public void runScript(CliCommandOptions options, String... args)
        throws FileNotFoundException, CliRunnerException, IOException {

        String filename = args[0];
        FileReader fileReader = new FileReader(filename);

        CliRunner cliRunner = CliRunner.getInstance();
        CliProperties newProperties = new CliProperties(
            cliRunner.getProperties());

        // set commandline arguments for this script
        for (int i = 1; i < args.length; i++) {
            newProperties.setProperty("_" + i, args[i]);
        }

        // create the console reader using stdin
        FileCommandLoopContext fileCommandReader
            = new FileCommandLoopContext(cliRunner, filename, fileReader,
                newProperties, options.isOptionSet('q'));

        // let the user know the script has started
        CliRunner.getInstance().getMessageConsole()
            .println(fileCommandReader.scriptMessage("begin"));

        // execute the command loop
        fileCommandReader.commandLoop();

        fileReader.close();

    }


    //
    //  Private instance classes
    //

    /**
     * Define special "pre-prompt processing" that needs to occur
     * due to the needs of our "paging" display console
     */
    private class FileCommandLoopContext extends CommandLoopContext {


        //
        //  Private instance data
        //

        /** name of the input stream */
        private final String mName;

        /** if true, do not echo commands to console */
        private final boolean mQuiet;


        //
        //  Public constructors
        //

        /**
         * @see CommandLoopContext#CommandLoopContext(CliRunner, Reader, CliProperties)
         */
        public FileCommandLoopContext(CliRunner cliRunner, String name,
                Reader consoleReader, CliProperties properties, boolean quiet) {
            super(cliRunner, consoleReader, properties);
            mName = name;
            mQuiet = quiet;
        }


        //
        //  Public instance methods
        //

        /**
         * Echo the command, to emulate console
         * @see CommandLoopContext#getCommandline()
         */
        public String getCommandline() {
            String commandLine = super.getCommandline();
            if (!mQuiet) {
                if (commandLine != null) {
                    CliRunner.getInstance().getMessageConsole()
                        .println(commandLine);
                }
            }
            if (commandLine == null) {
                // if end of stream, let the user know the script has ended
                CliRunner.getInstance().getMessageConsole()
                    .println(scriptMessage("end"));
            }
            return commandLine;
        }

        /**
         * @param type type of "quit" this is
         * @return message that will be presented to user to confirm the script has quit
         */
        public String scriptMessage(String type) {
            return "-" + type + ":[" + mName + "]-";
        }


        //
        //  Protected instance methods
        //

        /**
         * @see CommandLoopContext#promptUser()
         */
        @Override
        protected void promptUser() {
            if (!mQuiet) {
                super.promptUser();
            }
        }

        /**
         * @return quit flag - true if command loop(s) should terminate
         */
        @Override
        protected boolean isCliQuit() {
            if (mQuit) {
                // let the user know the script has prematurely ended
                CliRunner.getInstance().getMessageConsole()
                    .println(scriptMessage("quit"));
            }
            return mQuit;
        }

    }
}
