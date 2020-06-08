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

import wccommon.CliRunnerCommand;
import wccommon.CliRunnerCommandBase;

public class CliRunnerSample implements CliRunnerPlugin {

    static class CliRunnerSampleCommand extends CliRunnerCommandBase {
        
        public CliRunnerSampleCommand(String name, String syntax) {
            super(name, syntax);
        }

        /*
         * (non-Javadoc)
         * @see wccommon.CliRunnerCommand#processCommand(java.lang.String[])
         */
        public void processCommand(String[] args) throws CliRunnerException {
            for (int i = 0; i < args.length; i++) {
                System.out.println("" + (i+1) + ".) '" + args[i] + "'");
            }
        }
        
    }
    
    /*
     * (non-Javadoc)
     * @see wccommon.CliRunnerCommandsPlugin#init()
     */
    public void init() {
        System.out.println(this.getClass().getName() + " signing on");
        CopyOfCliRunner.getInstance().addCommands(getCommandList());        // was "CliRunner"
    }

    /*
     * (non-Javadoc)
     * @see wccommon.CliRunnerCommandsPlugin#fini()
     */
    public void fini() {
        System.out.println(this.getClass().getName() + " signing off");
        // nothing to do
    }
    
    /**
     * @return
     */
    private CliRunnerCommand[] getCommandList() {
        return new CliRunnerCommand[] {
            new CliRunnerSampleCommand("hi", "says hi")
        };
    }
    
}
