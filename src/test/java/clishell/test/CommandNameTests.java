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

import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.Assert;

import clishell.CliCommandParser;
import clishell.CommandName;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Classes under test:
 * <ol>
 *   <li><code>CliCommandParser</code></li>
 *   <li><code>CommandName</code></li>
 * </ol>
 *
 */
public class CommandNameTests {

    private PrintStream mConsoleOut;

    @Before
    public void setup() {

        // select either a "real" output destination during development;
        // or select the "bit bucket" before check-in to source control

        // mConsoleOut = System.out;
        mConsoleOut = new PrintStream(
            new OutputStream() { public void write(int c) { } });
    }

    @Test
    public void doStringTokenizerTests() {
        commandStringTokenizerTest(5, "hi there how are you?");
        commandStringTokenizerTest(4, "hi there \"how are\" you?");
        commandStringTokenizerTest(4, "hi there 'how are' you?");
        commandStringTokenizerTest(4, "hi there 'how are' you? ");
        commandStringTokenizerTest(4, " hi  there  ' how are '  you? ");
        commandStringTokenizerTest(4, " hi  there  \"how aren't\"  you? ");
        commandStringTokenizerTest(4, " hi  there  'how aren\"t'  you? ");
        commandStringTokenizerTest(1, "\" hi  there  'how aren\\\"t'  you? \"");
        commandStringTokenizerTest(3, "command -o option");
    }


    /**
     * @param edgeCasesTestCommandString string to test, representing an "edge case"
     */
    private void commandStringTokenizerTest(int nExpectedTokens,
            String edgeCasesTestCommandString) {
        mConsoleOut.println("     input = " + edgeCasesTestCommandString);
        CommandName commandNameBase = new CommandName(CliCommandParser
            .parseTokens(edgeCasesTestCommandString));
        Assert.assertEquals(commandNameBase.toString(), nExpectedTokens,
            commandNameBase.getCommandNameAsWordArray().length);
        printCommandName("output1", commandNameBase);
        CommandName commandNameDerived = new CommandName(CliCommandParser
            .parseTokens(commandNameBase.toString()));
        printCommandName("output2", commandNameDerived);
        Assert.assertEquals(commandNameBase, commandNameDerived);
        Assert.assertEquals(commandNameBase.toString(),
            commandNameBase.getCommandNameAsWordArray().length,
            commandNameDerived.getCommandNameAsWordArray().length);
    }


    /**
     * @param name string to represent command on displayed output
     * @param commandName command whose value is to display
     */
    private void printCommandName(String name, CommandName commandName) {
        mConsoleOut.println(name
            + "("
            + commandName.getCommandNameAsWordArray().length
            + ") = "
            + commandName.toString());
    }

}
