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
 * <code>CliException</code> is the most generalized form of application exceptions
 * generated by the CLI code; pretty much all of the CLI exceptions should derive
 * from this class, and it is advised not to throw instances of this class directly
 * in order to facilitate caller-level discernment of various classes of exceptions
 * and error conditions.
 *
 */
public class CliException extends Exception {

    /** serial version ID of this class */
    private static final long serialVersionUID = 1324987240L;

    /** default constructor */
    public CliException() {
        // nothing special
    }

    /**
     * @param message exception message
     * @see Exception#Exception(String)
     */
    public CliException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     * @see Exception#Exception(String, Throwable)
     */
    public CliException(String message, Throwable throwable) {
        super(message, throwable);
    }

}