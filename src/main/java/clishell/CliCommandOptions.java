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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 *  Object to store the set of commands (and associated value, if any)
 *  specified on the command line for a specific command
 */
public class CliCommandOptions {


    //
    //  Private instance data
    //

    /** map of option letter to option value (if any) */
    private final Map<Character, String> mOptionMap = new HashMap<Character, String>();


    //
    //  Public instance methods
    //

    /**
     * @return set of options actually specified
     * (use the "getOptionValue()" to get option's value, if any)
     */
    public Set<Character> getOptionSet() {
        return mOptionMap.keySet();
    }

    /**
     * @param optionLetter option to set
     */
    public void setOption(char optionLetter) {
        mOptionMap.put(optionLetter, null);
    }

    /**
     * @param optionLetter option to set
     * @param optionValue value given for option
     */
    public void setOption(char optionLetter, String optionValue) {
        mOptionMap.put(optionLetter, optionValue);
    }

    /**
     * @param optionLetter option letter to test
     * @return true iff option is set
     */
    public boolean isOptionSet(char optionLetter) {
        return mOptionMap.containsKey(optionLetter);
    }

    /**
     * @param optionLetter option letter to test
     * @return value corresponding to option (may be <code>null</code>)
     */
    public String getOptionValue(char optionLetter) {
        return mOptionMap.get(optionLetter);
    }

    /**
     * @param o object of comparison to this
     * @return true iff <code>o</code> "equals" this
     */
    public boolean equals(Object o) {

        // if it's not a derivative of CliCommandOptions, not equal
        if (!(o instanceof CliCommandOptions)) {
            return false;
        }

        // if it's the same exact object as us, it's equal
        CliCommandOptions that = (CliCommandOptions) o;
        if (that == this) {
            return true;
        }

        // if it has a different hashcode, not equal
        if (hashCode() != that.hashCode()) {
            return false;
        }

        // if it has a different number of entries, not equal
        Map<Character, String> thatMap = that.mOptionMap;
        if (mOptionMap.size() != thatMap.size()) {
            return false;
        }

        // if any of the entries in the maps differ, not equal
        for (Map.Entry<Character, String> entry : mOptionMap.entrySet()) {
            Character thisKey = entry.getKey();
            if (!thatMap.containsKey(thisKey)) {
                // doesn't contain a key that we have
                return false;
            }
            String thisValue = entry.getValue();
            String thatValue = thatMap.get(thisKey);
            if (thisValue == null) {
                if (thatValue != null) {
                    // has a value for a key that we have null for
                    return false;
                }
            } else {
                if (!thisValue.equals(thatValue)) {
                    // has a different value for a key than us
                    return false;
                }
            }
        }

        // passed all of our tests - it's equal!
        return true;
    }

    /**
     * @return string representation of options
     */
    public String toString() {
        return mOptionMap.toString();
    }

    /**
     * @return hashcode of this object's internal data
     */
    public int hashCode() {
        return mOptionMap.hashCode();
    }

}
