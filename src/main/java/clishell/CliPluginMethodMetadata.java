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



/**
 *
 * Stores metadata about a java method that can be called by the CLI.
 *
 */
public class CliPluginMethodMetadata
    implements Comparable<CliPluginMethodMetadata> {


    //
    //  Private instance data
    //

    /**
        mCliMethod - object containing method to be executed by this command,
                     containing utility methods to describe & invoke it
     */
    private CliMethod mCliMethod;

    /**
     * "Natural Order" override (used by "compareTo" for ordering command
     * within plugin)
     */
    private String mNaturalOrderOverride;


    //
    //  Public instance methods
    //

    /**
     * @return the naturalOrderOverride
     */
    public String getNaturalOrderOverride() {
        return mNaturalOrderOverride;
    }

    /**
     * @param naturalOrderOverride the naturalOrderOverride to set
     */
    public void setNaturalOrderOverride(String naturalOrderOverride) {
        mNaturalOrderOverride = naturalOrderOverride;
    }

    /**
     * @return the cliMethod
     * @see CliPluginMethodMetadata#mCliMethod
     */
    public CliMethod getCliMethod() {
        return mCliMethod;
    }

    /**
     * @param cliMethod the cliMethod to set
     * @see CliPluginMethodMetadata#mCliMethod
     */
    public void setCliMethod(CliMethod cliMethod) {
        mCliMethod = cliMethod;
    }

    /**
     * @return human-readable standard representation of command name or names
     */
    public String getDisplayName() {
        return mCliMethod.getMethod().getName();
    }

    /**
     * @param that object to which to compare this
     */
    public int compareTo(CliPluginMethodMetadata that) {

        //
        // order null objects at the end
        //

        if (that == null) {
            return 1;
        }

        //
        //  Get ordering to use
        //

        Integer thatNumericOrder = null;
        String thatOrder = that.getNaturalOrderOverride();
        if (thatOrder != null) {
            try {
                thatNumericOrder = Integer.decode(thatOrder);
            } catch(NumberFormatException nfe) {
                // not needed; but, to keep checkstyle happy, do something
                thatNumericOrder = null;
            }
        } else {
            thatOrder = that.getDisplayName();
        }

        Integer thisNumericOrder = null;
        String thisOrder = this.getNaturalOrderOverride();
        if (thisOrder != null) {
            try {
                thisNumericOrder = Integer.decode(thisOrder);
            } catch(NumberFormatException nfe) {
                // not needed; but, to keep checkstyle happy, do something
                thisNumericOrder = null;
            }
        } else {
            thisOrder = this.getDisplayName();
        }

        //
        //  Compare according to order
        //

        // if both evaluate to numeric expressions, use numeric comparison
        if ((thisNumericOrder != null) && (thatNumericOrder != null)) {
            return thisNumericOrder.compareTo(thatNumericOrder);
        }

        // else use alphanumeric comparison
        return thisOrder.compareTo(thatOrder);
    }

}
