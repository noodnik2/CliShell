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

import java.io.PrintWriter;
import java.util.Properties;

import clishell.CliRunner;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.anno.CliPluginInitializer;
import clishell.anno.CliPluginMain;
import clishell.ex.CliInvariantViolationException;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;

@CliPlugin(
     name = "console-input"
, version = "0.1.$Rev: 8950 $"
)
public class ConsoleInputPlugin {


    //
    // Private instance data
    //

    /** messagedisplay console consolewriter when turned "on" */
    private PagedConsoleWriter mMessageDisplayConsoleWriter;

    /** messagedisplay console printwriter (wraps the console writer) when turned "on" */
    private PrintWriter mMessageDisplayConsole;

    /** command loop context object */
    private ConsoleCommandLoopContext mConsoleCommandLoopContext;


    //
    //  Public plugin "main" methods
    //

    /**
     * Plugin initialization
     */
    @CliPluginInitializer
    public void init() {
        mConsoleCommandLoopContext = new ConsoleCommandLoopContext();
    }

    /**
     * Performs CLI console input command loop
     */
    @CliPluginMain
    public void main() throws CliRunnerException {
        CliRunner cliRunner = CliRunner.getInstance();

        // look up configured properties
        Properties cliRunnerProperties = cliRunner.getProperties();
        String signonBannerString = cliRunnerProperties.getProperty("signon-banner");

        // "sign on" if there is such a string configured
        if ((signonBannerString != null) && (!"".equals(signonBannerString.trim()))) {
            cliRunner.getMessageConsole().println(signonBannerString);
        }

        // execute the command loop using a customized console command reader
        mConsoleCommandLoopContext.commandLoop();
    }

    /**
     * Performs cleanup for this module,
     * before it's unloaded
     */
    @CliPluginFinalizer
    public void fini() {

        // signal termination of console command loop
        mConsoleCommandLoopContext.quit();

        // deactivate any active message console
        deactivateMessageConsole();
    }

    //
    //  Public plugin command methods
    //

    @CliPluginCommand(
          name = "set messagedisplay pagesize"
    ,   syntax = "{ <n> | off }"
    , helptext = {
            "Activates or deactivates message display paging.",
            "Deactivates if <n> is 0 or if 'off' is specified"
            }
    ,  minargs = 1
    ,  maxargs = 1
    )
    public void setMessageDisplay(String argument)
        throws CliRejectedInputException, CliRunnerException {

        CliRunner cliRunner = CliRunner.getInstance();

        int pagesize = 0;
        if (!"off".equalsIgnoreCase(argument.trim())) {
            pagesize = Integer.parseInt(argument.trim());
        }

        // deactivate any current message console
        deactivateMessageConsole();

        if (pagesize == 0) {
            // if user wants it deactivated, we're done
            return;
        }

        mMessageDisplayConsoleWriter = new PagedConsoleWriter(cliRunner
            .getMessageConsole(), pagesize);
        mMessageDisplayConsole = new PrintWriter(mMessageDisplayConsoleWriter);
        cliRunner.setMessageConsole(mMessageDisplayConsole);

    }

    /**
     * @param num number of lines to generate
     */
    @CliPluginCommand(
        name = "gen lines"
    , syntax = "<n>"
    , helptext = { "test utility method that generates output of <n> lines;",
                "only useful for testing 'set messagedisplay pagesize' command",
                "(to be removed later)"
                }
    )
    public void genLines(String num) {

        CliRunner cliRunner = CliRunner.getInstance();

        for (int i = 0; i < Integer.valueOf(num); i++) {
            cliRunner.getMessageConsole().println("test " + i);
        }
    }


    //
    //  Private instance classes
    //

    /**
     * Define special "pre-prompt processing" that needs to occur
     * due to the needs of our "paging" display console
     */
    private class ConsoleCommandLoopContext extends CommandLoopContext {

        /**
         * start a "new console output page" prior
         * to prompting for a command
         * @see CommandLoopContext#promptUser()
         */
        public void promptUser() {

            if (mMessageDisplayConsoleWriter != null) {
                // start new "output page" if there's a paging console
                mMessageDisplayConsoleWriter.setCurrentLineNumber(0);
            }

            // prompt the "user" to enter a command
            super.promptUser();

        }

    }

    /**
     * Deactivates any active message console
     */
    private void deactivateMessageConsole() {

        // if message console not active, nothing to do
        if (mMessageDisplayConsole == null) {
            return;
        }

        // force discontinuance of using wrapping writer
        mMessageDisplayConsoleWriter = null;

        try {
            // ask CliRunner to stop using it
            CliRunner.getInstance().unsetMessageConsole(mMessageDisplayConsole);
        } catch(CliRunnerException cre) {
            throw new CliInvariantViolationException(
                "programming error; unset message display console", cre);
        } finally {
            mMessageDisplayConsole = null;
        }

    }

}
