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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import clishell.CliProperties;
import clishell.CliRunner;
import clishell.ex.CliRunnerException;


/**
 *
 *
 */
public class CommandLoopContext {


    //
    //  Private instance data
    //

    /** CLI runner to dispatch commands to */
    private final CliRunner mCliRunner;

    /** buffered input reader for obtaining command lines */
    private final BufferedReader mCommandReader;

    /** holds the command prompt (if any; may be <code>null</code>) */
    private final String mCommandPrompt;

    /** holds properties defining the context of the reader */
    private final CliProperties mCliProperties;

    /** if set, command loop will terminate */
    private boolean mQuit;


    //
    //  Public constructors
    //

    /**
     *
     */
    public CommandLoopContext() {
        this(CliRunner.getInstance());
    }

    /**
     * @param cliRunner CLI runner to dispatch commands to
     */
    public CommandLoopContext(CliRunner cliRunner) {
        this(cliRunner, new InputStreamReader(System.in));
    }

    /**
     * @param cliRunner CLI runner to dispatch commands to
     * @param reader stream reader from which to obtain command lines
     */
    public CommandLoopContext(CliRunner cliRunner, Reader reader) {
        this(cliRunner, reader, cliRunner.getProperties());
    }

    /**
     * @param cliRunner CLI runner to dispatch commands to
     * @param reader stream reader from which to obtain command lines
     * @param properties property set to use for the context of this reader
     */
    public CommandLoopContext(CliRunner cliRunner, Reader reader,
            CliProperties properties) {
        mCliRunner = cliRunner;
        if (reader instanceof BufferedReader) {
            mCommandReader = (BufferedReader) reader;
        } else {
            mCommandReader = new BufferedReader(reader);
        }
        mCliProperties = properties;
        mCommandPrompt = mCliProperties.getProperty("command-prompt", ">> ");
    }


    //
    //  Public instance methods
    //

    /**
     * Reads and executes commands until the quit flag is set
     * @throws CliRunnerException unhandled exception
     */
    public void commandLoop()
        throws CliRunnerException {

        // loop while there are commands
        mQuit = false;
        while(!mCliRunner.isCliQuit() && !isCliQuit()) {

            // read command line
            String commandline = getCommandline();
            if (commandline == null) {
                // end of input stream
                break;
            }

            // perform any user preprocessing on the command line
            String finalCommandLine = preprocessCommandline(commandline);

            // dispatch command to the CLI if preprocessing didn't "swallow" it
            if (finalCommandLine != null) {
                mCliRunner.dispatchCommand(finalCommandLine);
            }
        }

    }

    /**
     * Signals that the command loop should terminate
     * instead of reading another command
     */
    public void quit() {
        mQuit = true;
    }


    //
    //  Protected instance methods
    //

    /**
     * @return quit flag - if true, command loop will terminate
     * instead of reading the next command
     */
    protected boolean isCliQuit() {
        return mQuit;
    }

    /**
     * Issue command prompt
     */
    protected void promptUser() {

        // prompt for input if there is a prompt string
        if (mCommandPrompt != null) {
            mCliRunner.getMessageConsole().print(mCommandPrompt);
            mCliRunner.getMessageConsole().flush();
        }
    }

    /**
     * Issue command prompt and read the next command line from
     * the user or the input stream
     * @return next command line from input or <code>null</code> if end of input
     * or error while reading input
     */
    protected String getCommandline() {
        try {
            promptUser();
            return mCommandReader.readLine();
        } catch(IOException ioex) {
            return null;
        }
    }

    /**
     * @param commandLine "raw" command line, before preprocessing
     * @return "final" command line to be executed by CLI dispatcher,
     * or <code>null</code> if command was "swallowed" by pre-processor
     */
    protected String preprocessCommandline(String commandLine) {

        // resolve any property references in command line
        String resolvedCommandline = mCliProperties.resolve(commandLine);

        // ignore empty lines
        String trimmedResolvedCommandLine = resolvedCommandline.trim();
        if ("".equals(trimmedResolvedCommandLine.trim())) {
            return null;
        }

        // ignore "comment" lines
        if (trimmedResolvedCommandLine.startsWith("#")) {
            return null;
        }

        // return final command line to be dispatched
        return resolvedCommandline;
    }

}