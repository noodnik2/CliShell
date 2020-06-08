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
 * Signals the encounter of invalid metadata in a plugin module
 *
 */
public class CliInvalidPluginMetadataException extends CliRunnerException {

    /**
     *
     */
    private static final long serialVersionUID = 974344281L;

    /**
     *
     */
    public CliInvalidPluginMetadataException() {
        // nothing to do
    }

    /**
     * @param message exception message
     */
    public CliInvalidPluginMetadataException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliInvalidPluginMetadataException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
