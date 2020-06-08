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

package wccommon;

import wccommon.cli.CliRunnerException;

/**
 * 
 * @author mross
 *
 */
public interface CliRunnerCommand {

    /**
     * @return command name (i.e. first word user would type to
     * invoke command)
     */
    String getName();
    
    /**
     * @return simple, one-line syntax diagram for command
     */
    String getSyntaxDiagram();
    
    /**
     * @param args
     * @throws CliRunnerException
     */
    void processCommand(String args[]) throws CliRunnerException;
    
}

