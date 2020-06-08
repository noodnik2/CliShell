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
public class CliRunnerExitException extends CliPluginMethodException {

    /**
     * 
     */
    private static final long serialVersionUID = 1324987242L;

    /**
     *
     */
    public CliRunnerExitException() {
        //
    }

    /**
     * @param message exception message
     */
    public CliRunnerExitException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliRunnerExitException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
