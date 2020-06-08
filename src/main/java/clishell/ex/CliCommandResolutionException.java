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
 * @author mross
 *
 */
public class CliCommandResolutionException extends CliRunnerException {

    /**
     *
     */
    private static final long serialVersionUID = 13224323441L;

    /**
     *
     */
    public CliCommandResolutionException() {
        //
    }

    /**
     * @param message exception message
     */
    public CliCommandResolutionException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliCommandResolutionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
