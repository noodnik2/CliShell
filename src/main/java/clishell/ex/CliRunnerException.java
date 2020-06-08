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
 * Distinguished from <code>CliException</code> primarily due to
 * being connected foremost to the <code>CliRunner</code> program.
 *
 */
public class CliRunnerException extends CliExceptionCollection {

    /**
     *
     */
    private static final long serialVersionUID = 1324987241L;

    /**
     *
     */
    public CliRunnerException() {
        // nothing to do
    }

    /**
     * @param message exception message
     */
    public CliRunnerException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliRunnerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
