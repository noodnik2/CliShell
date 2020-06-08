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

import clishell.CliCommandOptions;
import clishell.CliCommandParser;
import clishell.CliOptionParser;
import clishell.ex.CliRejectedInputException;
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
public class CliOptionsParserTests {


    //
    //  Private instance data
    //

    /** */
    private PrintStream mConsoleOut;


    //
    //  Public instance setup methods
    //

    /**
     *
     */
    @Before
    public void setup() {

        // select either a "real" output destination during development;
        // or select the "bit bucket" before check-in to source control

        // mConsoleOut = System.out;
        mConsoleOut = new PrintStream(
            new OutputStream() {
                public void write(int c) { }
            });
    }


    //
    //  Public instance test methods
    //

    /**
     * @throws CliRejectedInputException
     */
    @Test
    public void testOptionsParserNoExceptions() throws CliRejectedInputException {
        // no options
        doTestParser("", "cmd arg1 arg2 arg3", 0, 0, "");
        // option, but after first command argument (option undetected)
        doTestParser("", "cmd arg1 -c arg2 arg3", 0, 0, "");
        // single option, specified as not taking an argument
        doTestParser("c", "cmd arg1 -c arg2 arg3", 2, 3, "c=");
        // single option, specified as taking an argument
        doTestParser("c:", "cmd -c arg1 arg2", 1, 3, "c=arg1");
        // two options, first one only specified as taking an argument
        doTestParser("c:d", "cmd -c arg1 -d arg2 arg3", 1, 4, "d=,c=arg1");
        // multiple "any options"
        doTestParser("*", "cmd -a -b -c arg1 arg2", 1, 4, "a=,b=,c=");
        // multiple "any options" with option taking argument
        doTestParser("*d:", "cmd -a -b -c -d arg1 arg2", 1, 6, "a=,b=,c=,d=arg1");
        // multiple "any options" with option taking argument (reverse syntax from previous)
        doTestParser("d:*", "cmd -a -b -c -d arg1 arg2", 1, 6, "a=,b=,c=,d=arg1");

        // grouped options together with ungrouped options, some of which take arguments
        doTestParser("e:bcdaf", "cmd arg1 -abc -d -e arg2 -f arg3", 2, 7, "a=,b=,c=,d=,e=arg2,f=");

    }

    @Test
    public void testIsOptionSetMethod() throws CliRejectedInputException {
        doTestIsOptionSet("c:", "cmd -c arg1", 'c', true);
        doTestIsOptionSet("c:", "cmd -c arg1", 'd', false);
        doTestIsOptionSet("dc:", "cmd -c carg -d arg1", 'd', true);
        doTestIsOptionSet("dc:", "cmd -d arg1", 'd', true);
        doTestIsOptionSet("dc:", "cmd -d arg1", 'c', false);
    }

    /**
     *
     */
    @Test
    public void testOptionsParserExceptions() {
        // option, but not specified as allowed (case 1)
        doTestParserThrowsException("", "cmd arg1 -c arg2 arg3", 2, 3, "",
            CliRejectedInputException.class);
        // option, but not specified as allowed (case 1)
        doTestParserThrowsException("x:", "cmd arg1 -c arg2 arg3", 2, 4, "c=arg2",
            CliRejectedInputException.class);
        // option 'x' requires an argument and therefore cannot be grouped with other options
        doTestParserThrowsException("a:x", "cmd arg1 -xa arg2 arg3", 2, 4, "a=arg2,x=",
            CliRejectedInputException.class);
        // invalid option letter '$' (option selectors must indeed be letters in this implementation)
        doTestParserThrowsException("$", "cmd arg1", 2, 7, "a=",
            CliRejectedInputException.class);
    }


    //
    //  Private instance methods
    //

    /**
     * @param allowedOptions
     * @param commandLine
     * @param optionToTest option letter to test
     * @param expectedIsSetValue expected value for <code>isOptionSet(optionToTest)</code> return
     * @throws CliRejectedInputException
     */
    private void doTestIsOptionSet(String allowedOptions, String commandLine,
            char optionToTest, boolean expectedIsSetValue) throws CliRejectedInputException {

        String[] commandNameAsWordArray = CliCommandParser.parseTokens(commandLine);
        CliCommandOptions actualOptions = new CliCommandOptions();
        // all "commandLine" values are one word commands, so first arg index is always 1
        new CliOptionParser(allowedOptions).parseOptions(actualOptions,
                1, commandNameAsWordArray);

        Assert.assertEquals(expectedIsSetValue, actualOptions.isOptionSet(optionToTest));
    }

    /**
     * @param allowedOptions
     * @param commandLine
     * @param firstOptionIndex
     * @param expectedFirstCommandArgumentIndex
     * @param expectedMapString
     * @param expectedExceptionClass
     */
    private void doTestParserThrowsException(String allowedOptions, String commandLine,
            int firstOptionIndex, int expectedFirstCommandArgumentIndex,
            String expectedMapString, Class<? extends Throwable> expectedExceptionClass) {
        Throwable thrownException = null;
        try {
            doTestParser(allowedOptions, commandLine, firstOptionIndex,
                expectedFirstCommandArgumentIndex, expectedMapString);
        } catch(Throwable e) {
            thrownException = e;
            if (thrownException.getClass() == expectedExceptionClass) {
                return;
            }
        }
        if (thrownException != null) {
            thrownException.printStackTrace(mConsoleOut);
        }
        Assert.fail("expected to throw '"
            + expectedExceptionClass.getName()
            + "' but threw "
            + (
                (thrownException == null)
              ? "nothing"
              : ("'"
                  + thrownException.getClass().getName()
                  + "': "
                  + thrownException.getMessage()
                )
            )
        );
    }

    /**
     * @param allowedOptions
     * @param commandLine
     * @param firstOptionIndex
     * @param expectedFirstCommandArgumentIndex
     * @param expectedOptionsString
     * @throws CliRejectedInputException
     */
    private void doTestParser(String allowedOptions, String commandLine,
            int firstOptionIndex, int expectedFirstCommandArgumentIndex,
            String expectedOptionsString) throws CliRejectedInputException {

        CliCommandOptions expectedOptions = new CliCommandOptions();
        loadOptions(expectedOptions, expectedOptionsString);
        String[] commandNameAsWordArray = CliCommandParser.parseTokens(commandLine);
        CliCommandOptions actualOptions = new CliCommandOptions();
        int actualFirstCommandArgumentIndex = new CliOptionParser(allowedOptions).parseOptions(
                actualOptions, firstOptionIndex, commandNameAsWordArray);
        Assert.assertEquals(expectedFirstCommandArgumentIndex, actualFirstCommandArgumentIndex);
        Assert.assertEquals(expectedOptions, actualOptions);
    }

    /**
     * @param map
     * @param expectedMapString
     */
    private void loadOptions(CliCommandOptions options, String expectedOptionsString) {
        for (String mapEntry : expectedOptionsString.split(",")) {
            if ("".equals(mapEntry.trim())) {
                continue;
            }
            String[] nameValue = mapEntry.split("=");
            if (nameValue.length == 1) {
                options.setOption(nameValue[0].charAt(0));
            } else {
                options.setOption(nameValue[0].charAt(0), nameValue[1]);
            }
        }
    }

}
