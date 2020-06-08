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

import java.util.regex.Pattern;

/**
 *
 * @author mross
 *
 */
public class CommandName {


    //
    // Private class data
    //

    /** regular expression pattern used to find "white space" separating command words */
    private static final Pattern PATTERN_MATCHING_WHITESPACE = Pattern.compile(".*([\\s]+).*");


    //
    // Private instance data
    //

    /** string array representation of command name */
    private final String[] mCommandNameAsWordArray;

    /** string representation of command name */
    private final String mCommandNameAsString;


    //
    //  Public constructors
    //

    /**
     * @param commandNameAsWordArray string array representation of command name
     */
    public CommandName(String[] commandNameAsWordArray) {
        mCommandNameAsWordArray = commandNameAsWordArray.clone();
        mCommandNameAsString = getCommandStringFromWordArray(mCommandNameAsWordArray);
    }


    //
    //  Public instance methods
    //

    /**
     * @return string array representation of command name
     */
    public String[] getCommandNameAsWordArray() {
        return mCommandNameAsWordArray.clone();
    }

    /**
     * @param index index of command word within this command name
     * @return the requested command word from within the command name
     * @throws IllegalArgumentException insufficient argument(s) specified
     */
    public String getCommandArgument(int index) {
        if (mCommandNameAsWordArray.length <= index) {
            throw new IllegalArgumentException("insufficient argument(s) specified");
        }
        return mCommandNameAsWordArray[index];
    }

    //
    //  Overrides of "java.lang.Object" base class methods
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mCommandNameAsString.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CommandName)) {
            return false;
        }
        CommandName commandNameObj = (CommandName) obj;

        return mCommandNameAsString
            .equals(commandNameObj.mCommandNameAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return mCommandNameAsString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        // like "deep-copy" semantics, given that strings are immutable
        return new CommandName(mCommandNameAsWordArray);
    }


    //
    //  Private instance methods
    //

    /**
     * @param commandNameAsWordArray string array representation of command name
     * @return string representation of command name specified in <code>commandNameAsWordArray</code>
     */
    private String getCommandStringFromWordArray(String[] commandNameAsWordArray) {

        StringBuffer commandNameAsString = new StringBuffer();

        boolean isFirst = true;

        for (String commandNameWord : commandNameAsWordArray) {

            if (!isFirst) {
                commandNameAsString.append(' ');
            } else {
                isFirst = false;
            }

            if (commandNameWord == null) {
                throw new RuntimeException("null word!");
            }

            // word contains whitespace?
            if (!PATTERN_MATCHING_WHITESPACE.matcher(commandNameWord).matches()) {
                // no whitespace; no need to quote token at all
                commandNameAsString.append(commandNameWord);
                continue;
            }

            // word contains double quote?
            if (!commandNameWord.contains("\"")) {
                // whitespace but no double quote; can quote with double quote
                commandNameAsString.append("\"" + commandNameWord + "\"");
                continue;
            }

            // word contains single quote?
            if (!commandNameWord.contains("'")) {
                // whitespace, double quote, but not single quote; can quote with single quote
                commandNameAsString.append("'" + commandNameWord + "'");
                continue;
            }

            // whitespace, double quote, single quote, so quote with escaped double quote
            commandNameAsString.append("\"" + commandNameWord.replace("\"", "\\\"") + "\"");
            continue;
        }

        return commandNameAsString.toString();
    }

}
