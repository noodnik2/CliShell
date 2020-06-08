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
public abstract class CliRunnerCommandBase implements CliRunnerCommand {
    
    /** */
    private String mName;
    
    /** */
    private String mSyntaxDiagram;

    /**
     * @param name
     * @param syntax
     */
    public CliRunnerCommandBase(String name, String syntaxDiagram) {
        mName = name;
        mSyntaxDiagram = syntaxDiagram;
    }
    
    /*
     * (non-Javadoc)
     * @see wccommon.CliRunnerCommand#getName()
     */
    public String getName() {
        return mName;
    }
    
    /*
     * (non-Javadoc)
     * @see wccommon.CliRunnerCommand#getSyntaxDiagram()
     */
    public String getSyntaxDiagram() {
        return mSyntaxDiagram;
    }

    /*
     * (non-Javadoc)
     * @see wccommon.CliRunnerCommand#processCommand(java.lang.String[])
     */
    public abstract void processCommand(String args[]) throws CliRunnerException;
    
}

