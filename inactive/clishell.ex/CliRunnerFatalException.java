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
public class CliRunnerFatalException extends CliRunnerExitException {

    /**
     * 
     */
    private static final long serialVersionUID = 1324987244L;

    /**
     * @param message exception message
     */
    public CliRunnerFatalException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     */
    public CliRunnerFatalException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
