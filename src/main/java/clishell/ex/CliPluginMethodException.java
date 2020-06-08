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
 * Exception thrown from a CLI Plugin Method.
 * Does not normally cause CLI to abend.
 *
 */
public class CliPluginMethodException extends CliRunnerException {

    /**
     *
     */
    private static final long serialVersionUID = 1324987245L;

    /**
     *
     */
    public CliPluginMethodException() {
        //
    }

    /**
     * @param message exception message
     */
    public CliPluginMethodException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliPluginMethodException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
