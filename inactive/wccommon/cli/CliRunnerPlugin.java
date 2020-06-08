/**
 * 
 *
 *  
 *
 *
 * 
 * @author MRoss
 * 
 */

package wccommon.cli;


public interface CliRunnerPlugin {

    /**
     * called when CLI is initialized
     * @throws CliRunnerException
     */
    void init() throws CliRunnerException;
    
    /**
     * called when CLI is shut down
     * @throws CliRunnerException
     */
    void fini() throws CliRunnerException;
    
}
