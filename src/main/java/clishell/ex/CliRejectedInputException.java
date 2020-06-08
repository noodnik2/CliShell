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

package clishell.ex;


/**
 *
 * Signals the rejection of user input (i.e., CLI commands,
 * plugin metadata, etc.) by any part of the CLI.
 *
 */
public class CliRejectedInputException extends CliRunnerException {

    /**
     *
     */
    private static final long serialVersionUID = 577241L;

    /**
     *
     */
    public CliRejectedInputException() {
        // nothing to do
    }

    /**
     * @param message exception message
     */
    public CliRejectedInputException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliRejectedInputException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
