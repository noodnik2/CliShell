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

package clishell.plugins;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import clishell.CliCommandOptions;
import clishell.CliCommandParser;
import clishell.CliRunner;
import clishell.CommandName;
import clishell.FullCommandNameTree;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;
import clishell.reflection.BeanGetter;
import clishell.util.PropertyReferenceResolver;

/**
 *
 * Common and useful methods for CLI plugins
 *
 */
public final class CliPluginUtil {

    /**
     * Utility class - no instantiation necessary
     */
    private CliPluginUtil() {
        // no instantiate
    }

    /**
     * @param args
     * @param subcommands
     * @throws CliRejectedInputException
     */
    public static String retrieveAbbreviatedSubcommand(String[] args, String[] subcommands)
        throws CliRejectedInputException {
        FullCommandNameTree tree = new FullCommandNameTree();
        for (String subcommand : subcommands) {
            tree.addCommandName(new CommandName(CliCommandParser.parseTokens(subcommand)));
        }
        Set<CommandName> foundCommands = tree.findMatchingCommandNames(new CommandName(args));
        if (foundCommands.size() == 1) {
            return foundCommands.iterator().next().toString();
        }
        throw new CliRejectedInputException("unknown subcommand: '"
            + (new CommandName(args).toString())
            + "'");
    }

    /**
     * @param printWriter output destination
     * @param beanCollection collection of beans
     * @param formatString string used to format each bean to <code>printWriter</code>
     */
    public static void printBeanCollection(PrintWriter printWriter,
            Collection<? extends Object> beanCollection, String formatString) {

        BeanGetter beanGetter = new BeanGetter();

        PropertyReferenceResolver propertyReferenceResolver
            = new PropertyReferenceResolver("%{", "}");

        for (Object bean : beanCollection) {
            printWriter.println(propertyReferenceResolver
                .resolvePropertyReferences(formatString, beanGetter.setBean(bean)));
        }

    }

    /**
     * NOTE: does not close any streams - that is responsibility of the caller
     * @param options cli options in effect to modify behavior of this method:
     * <ul>
     *   <li>
     *     if 'q' option specified and <code>messageConsole</code> is not <code>null</code>,
     *     will suppress routing of message console output to existing message console
     *     (i.e., will route it only to the new <code>messageConsole</code>).  The idea is
     *     that console messages should go both to the "capture" destination and to the
     *     current destination (as a default), unless otherwise specified.
     *   </li>
     *   <li>
     *     If 'e' option specified and <code>messageConsole</code> is not <code>null</code>,
     *     then will route error console to the message console.  The idea is that normally
     *     we don't want errors to appear in the "captured" console output (as a default),
     *     unless otherwise specified.
     *   </li>
     * </ul>
     * @param inputConsole new input console to use, or <code>null</code> to use existing
     * @param messageConsole new message (and possibly error) console to use, or <code>null</code>
     * to use existing
     * @param commandTokens full command token array
     * @throws CliRunnerException unhandled exception
     */
    public static void dispatchCommand(CliCommandOptions options,
            InputStream inputConsole, PrintWriter messageConsole,
            String... commandTokens) throws CliRunnerException {

        CliRunner cliInstance = CliRunner.getInstance();

        // new error console is unchanged unless specified
        PrintWriter newErrorConsole = null;

        // new message console is as specified by user
        PrintWriter newMessageConsole = messageConsole;
        if (messageConsole != null) {
            if (!options.isOptionSet('q')) {
                // but if it's not null and 'q' option is not set,
                // then route messages also to the old console
                TeeWriter teeWriter = new TeeWriter(true, messageConsole,
                        false, cliInstance.getMessageConsole());
                newMessageConsole = new PrintWriter(teeWriter);
            }
            if (options.isOptionSet('e')) {
                newErrorConsole = newMessageConsole;
            }
        }

        cliInstance.dispatchCommand(inputConsole, newMessageConsole,
            newErrorConsole, commandTokens);

        if (newMessageConsole != null) {
            newMessageConsole.flush();
        }

        if (newErrorConsole != null) {
            newErrorConsole.flush();
        }

    }

}
