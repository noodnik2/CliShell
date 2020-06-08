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

package clishell.test;

import java.util.Set;

import junit.framework.Assert;

import clishell.CliCommandParser;
import clishell.CommandName;
import clishell.FullCommandNameTree;
import org.junit.Test;

/**
 *
 * @author mross
 *
 */
public class CommandManagerTest {

    private static final String[] FULLCOMMANDSTRINGS_ONE = new String[] {
        "help"
    ,   "set fill"
    ,   "load file later"
    ,   "load file now"
    ,   "load filx now"
    ,   "load fiasfd november"
    ,   "load fiasfd noy"
    ,   "lo fi no"
    ,   "load file now"
    ,   "load faq now"
    ,   "lane file now"
    ,   "set color"
    ,   "face me please now"
    ,   "face me darnit now"
    ,   "super silly dance"
    ,   "wiggle your body dora"
    ,
    };


    @Test
    public void testBasicFindCommands() {
        FullCommandNameTree fullCommandNameNode = newFullCommandNameTree(FULLCOMMANDSTRINGS_ONE);

        testBasicFindCommandsSubtest(fullCommandNameNode, "l f",
            "[load file later, load file now, load filx now, load fiasfd november, load fiasfd noy, load faq now, lo fi no, lane file now]");

        testBasicFindCommandsSubtest(fullCommandNameNode, "l f n",
            "[load file now, load filx now, load fiasfd november, load fiasfd noy, load faq now, lo fi no, lane file now]");

        // "lo" matches first word exactly; only consider that word's subtree
        testBasicFindCommandsSubtest(fullCommandNameNode, "lo f n",
            "[lo fi no]");

        testBasicFindCommandsSubtest(fullCommandNameNode, "la", "[lane file now]");
        testBasicFindCommandsSubtest(fullCommandNameNode, "l f l", "[load file later]");
        testBasicFindCommandsSubtest(fullCommandNameNode, "l fa n", "[load faq now]");
    }

    @Test
    public void testBasicAbbreviations() {
        FullCommandNameTree fullCommandNameNode = newFullCommandNameTree(FULLCOMMANDSTRINGS_ONE);
        //fullCommandNameNode.printAsTree();
        Set<CommandName> commandNames = fullCommandNameNode
            .findMatchingCommandNames(new CommandName(new String[] {"lo", "fi", "no"}));
//        for (CommandName commandName : commandNames) {
//            System.out.println("testOne: " + commandName.toString());
//        }
        Assert.assertEquals(commandNames.toString(), 1, commandNames.size());
    }

    /**
     * Simple test of ability to remove command from tree
     */
    @Test
    public void testRemovalOfCommandNameFromTree() {
        FullCommandNameTree fullCommandNameNode = newFullCommandNameTree(FULLCOMMANDSTRINGS_ONE);
        testRemovalOfCommandNameFromTreeSubtest(fullCommandNameNode, "wiggle your body dora");
        testRemovalOfCommandNameFromTreeSubtest(fullCommandNameNode, "load file later");
        testRemovalOfCommandNameFromTreeSubtest(fullCommandNameNode, "load file now");
        testRemovalOfCommandNameFromTreeSubtest(fullCommandNameNode, "help");
    }


    //
    //  Private instance methods
    //

    /**
     * @param fullCommandNameNode
     * @param commandToRemove
     */
    private void testRemovalOfCommandNameFromTreeSubtest(FullCommandNameTree fullCommandNameNode,
            String commandToRemove) {
        Assert.assertTrue(fullCommandNameNode.getComandNames().contains(getCommandNameFromString(commandToRemove)));
        fullCommandNameNode.removeCommandName(getCommandNameFromString(commandToRemove));
        Assert.assertFalse(fullCommandNameNode.getComandNames().contains(getCommandNameFromString(commandToRemove)));
    }

    /**
     * @param fullCommandNameNode
     * @param commandAbbreviation
     * @param expectedMapValue
     */
    private void testBasicFindCommandsSubtest(FullCommandNameTree fullCommandNameNode,
            String commandAbbreviation, String expectedMapValue) {
        Set<CommandName> commandNames = fullCommandNameNode.findCommandNamesFromCommandLine(getCommandNameFromString(commandAbbreviation));
        Assert.assertEquals(expectedMapValue, commandNames.toString());
    }

    /**
     * @param commandLines array of command lines to parse and add to a newly
     * instantiated <code>FullCommandNameTree</code> object that is returned
     * @return newly instantiated <code>FullCommandNameTree</code> object containing
     * command lines parsed from <code>commandLines</code>
     */
    private FullCommandNameTree newFullCommandNameTree(String[] commandLines) {

        FullCommandNameTree fullCommandNameTree = new FullCommandNameTree();

        for (String commandLine : commandLines) {
            CommandName commandName = new CommandName(CliCommandParser.parseTokens(commandLine));
            fullCommandNameTree.addCommandName(commandName);
        }

        return fullCommandNameTree;
    }

    /**
     * @param inputLine
     * @return
     */
    private CommandName getCommandNameFromString(String inputLine) {
        return new CommandName(CliCommandParser.parseTokens(inputLine));
    }

}
