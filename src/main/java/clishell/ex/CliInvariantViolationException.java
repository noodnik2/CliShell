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
 * Runtime (unchecked) CLI exception, thrown when a program "invariant"
 * is violated, usually indicating a programming or logic error.
 *
 * Hopefully, these exceptions will never be thrown!  ;-)
 *
 */
public class CliInvariantViolationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 13243521244L;

    /**
     * @param message exception message
     */
    public CliInvariantViolationException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliInvariantViolationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
