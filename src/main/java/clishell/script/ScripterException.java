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

package clishell.script;

/**
 *
 *  Encapsulates exceptions thrown by custom "Scripter" API
 *
 */
public class ScripterException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 8252060230372021520L;

    /**
     * @see Exception#Exception()
     */
    public ScripterException() {
        super();
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public ScripterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see Exception#Exception(String)
     */
    public ScripterException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public ScripterException(Throwable cause) {
        super(cause);
    }

}
