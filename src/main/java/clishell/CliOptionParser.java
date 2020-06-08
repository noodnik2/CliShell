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

import java.util.Arrays;

import clishell.ex.CliRejectedInputException;

/**
 *
 * Parser for CLI command Options.
 * Design patterned after that of traditional Unix "getopt" command.
 *
 */
public class CliOptionParser {


    //
    //  Private instance types
    //

    /** */
    private enum OType { HASARG, NOARG };


    //
    //  Private instance data
    //

    /** */
    private boolean mUnknownOptionsAllowed;

    /** */
    private final String mAllowedOptionLetters;

    /** */
    private final OType[] mAllowedOptionTypes;


    //
    // Public instance constructors
    //

    /**
     * @param optionSyntax string specifying option syntax, modeled after
     * the traditional Unix "getopt" "optstring" format
     * @throws CliRejectedInputException <code>optionSyntax</code> was not valid
     * @see <code>getopt</code> <a href="http://compute.cnr.berkeley.edu/cgi-bin/man-cgi?getopts+1"
     * >"man" page</a>
     */
    public CliOptionParser(String optionSyntax) throws CliRejectedInputException {

        char[] optionLetterWorkArray = new char[optionSyntax.length()];
        OType[] optionTypeWorkArray = new OType[optionSyntax.length()];

        int optionSyntaxInputIndex = 0;
        int optionSelectorutputIndex = 0;

        char[] optionSyntaxCharArray = optionSyntax.toCharArray();
        while(true) {
            if (optionSyntaxInputIndex == optionSyntaxCharArray.length) {
                break;
            }
            char optionLetter = optionSyntaxCharArray[optionSyntaxInputIndex++];
            if (optionLetter == '*') {
                mUnknownOptionsAllowed = true;
                continue;
            }
            if (!Character.isLetter(optionLetter)) {
                throw new CliRejectedInputException("invalid option letter '"
                    + optionLetter
                    + "'");
            }
            OType optionType = OType.NOARG;
            if (optionSyntaxInputIndex < optionSyntaxCharArray.length) {
                if (optionSyntaxCharArray[optionSyntaxInputIndex] == ':') {
                    optionType = OType.HASARG;
                    optionSyntaxInputIndex++;
                }
            }
            optionLetterWorkArray[optionSelectorutputIndex] = optionLetter;
            optionTypeWorkArray[optionSelectorutputIndex] = optionType;
            optionSelectorutputIndex++;
        }

        mAllowedOptionLetters = String.valueOf(optionLetterWorkArray,
                0, optionSelectorutputIndex);
        mAllowedOptionTypes = Arrays.copyOf(optionTypeWorkArray,
                optionSelectorutputIndex);

    }

    /**
     * @param options output map of parsed options and possibly option values
     * @param firstOptionIndex index of the first potential "option" token within
     * the string array <code>commandNameAsWordArray</code>.
     * @return index same value as given in <code>firstOptionIndex</code> if no
     * options are found, or the index of the first non-option token within the
     * string array <code>commandNameAsWordArray</code>.
     * @throws CliRejectedInputException illegal option or option syntax encountered
     * during parsing of option(s)
     */
    public int parseOptions(CliCommandOptions options,
        int firstOptionIndex, String[] commandNameAsWordArray)
        throws CliRejectedInputException {

        int nextIndex = firstOptionIndex;
        while (nextIndex < commandNameAsWordArray.length) {
            String optionWord = commandNameAsWordArray[nextIndex];
            if (optionWord.length() == 0 || optionWord.charAt(0) != '-') {
                break;
            }
            nextIndex++;
            if (optionWord.length() == 2) {
                // if option not grouped, may have an option argument
                char optionLetter = optionWord.charAt(1);
                String optionValue = null;
                assertAllowedOption(optionLetter);
                if (optionTakesArgument(optionLetter)) {
                    if (nextIndex == commandNameAsWordArray.length) {
                        throw new CliRejectedInputException("required argument to option '-"
                            + optionLetter
                            + "' not supplied");
                    }
                    optionValue = commandNameAsWordArray[nextIndex++];
                }
                options.setOption(optionLetter, optionValue);
                continue;
            }

            // grouped options may not have an option argument
            for (Character optionLetter : optionWord.substring(1).toCharArray()) {
                assertAllowedOption(optionLetter);
                if (optionTakesArgument(optionLetter)) {
                    throw new CliRejectedInputException("option '-"
                        + optionLetter
                        + "' requires an argument and therefore cannot be grouped with other options"
                    );
                }
                options.setOption(optionLetter);
            }
        }
        return nextIndex;
    }

    /**
     * @param optionLetter option selector parsed from command line
     * @throws CliRejectedInputException <code>optionLetter</code> is not recognized
     */
    private void assertAllowedOption(char optionLetter)
        throws CliRejectedInputException {

        if (
            (mAllowedOptionLetters.indexOf(optionLetter) >= 0)
                || mUnknownOptionsAllowed) {
            return;
        }

        throw new CliRejectedInputException("option '-"
            + optionLetter
            + "' not allowed");
    }

    /**
     * @param optionLetter option selector parsed from command line
     * @return <code>true</code> iff indicated option requires an argument
     */
    private boolean optionTakesArgument(char optionLetter) {
        int allowedOptionIndex = mAllowedOptionLetters.indexOf(optionLetter);
        if (allowedOptionIndex < 0) {
            return false;  // i.e., if "any" options are allowed
        }
        return mAllowedOptionTypes[allowedOptionIndex] == OType.HASARG;
    }

}
