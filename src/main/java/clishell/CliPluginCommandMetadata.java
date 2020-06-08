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

package clishell;

import java.util.Set;


/**
 *
 * @author mross
 *
 */
public class CliPluginCommandMetadata extends CliPluginMethodMetadata {


    //
    //  Private instance data
    //

    /**
     * name(s) by which this command is known to the CLI (i.e., represents
     * the set of "commands" - any of which - the user can enter to invoke
     * this command)
     */
    private Set<CommandName> mNames;

    /**
     * configured parser of option(s) supported by this command
     */
    private CliOptionParser mCliOptionParser;

    /**
     * Minimum number of arguments allowed to be supplied to this command
     */
    private int mMinArgs;

    /**
     * Maximum number of arguments allowed to be supplied to this command
     */
    private int mMaxArgs;


    //
    //  Public instance methods
    //

    /**
     * @return the minArgs
     */
    public int getMinArgs() {
        return mMinArgs;
    }

    /**
     * @param minArgs the minArgs value to set
     */
    public void setMinArgs(int minArgs) {
        mMinArgs = minArgs;
    }

    /**
     * @return the maxArgs
     */
    public int getMaxArgs() {
        return mMaxArgs;
    }

    /**
     * @param maxArgs the maxArgs value to set
     */
    public void setMaxArgs(int maxArgs) {
        mMaxArgs = maxArgs;
    }

    /**
     * @return command name(s)
     * @see CliPluginCommandMetadata#mNames
     */
    public Set<CommandName> getNames() {
        return mNames;
    }

    /**
     * @param names set of command name(s) to set
     * @see CliPluginCommandMetadata#mNames
     */
    public void setNames(Set<CommandName> names) {
        mNames = names;
    }

    /**
     * @return the configured <code>CliOptionParser</code> for this command
     */
    public CliOptionParser getOptionParser() {
        return mCliOptionParser;
    }

    /**
     * @param cliOptionParser the <code>CliOptionParser</code> to set
     */
    public void setOptionParser(CliOptionParser cliOptionParser) {
        mCliOptionParser = cliOptionParser;
    }

    /**
     * @return human-readable standard representation of command name or names
     */
    @Override
    public String getDisplayName() {
        StringBuffer printableCommandName = new StringBuffer();
        Set<CommandName> commandNames = getNames();
        if (commandNames.size() > 1) {
            printableCommandName.append("{ ");
        }
        boolean isFirstName = true;
        for (CommandName commandName : commandNames) {
            if (!isFirstName) {
                printableCommandName.append(" | ");
            } else {
                isFirstName = false;
            }
            printableCommandName.append(commandName.toString());
        }
        if (commandNames.size() > 1) {
            printableCommandName.append(" }");
        }
        return printableCommandName.toString();
    }

}
