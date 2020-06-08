
package clishell.util;

/**
 *
 *  Utility class to resolve references (recursively) in a string
 *
 */
public class PropertyReferenceResolver {

    /** starting and ending "bracket" strings, defining reference notation */
    private final String[] mBrackets;

    /**
     * @param startBracket start bracket string denoting start of property name
     * @param endBracket end bracket string denoting end of property name
     */
    public PropertyReferenceResolver(String startBracket, String endBracket) {
        if (startBracket == null || endBracket == null) {
            throw new NullPointerException();
        }
        mBrackets = new String[] { startBracket, endBracket };
    }

    /**
     * @param string string value, possibly containing property references to be substituted
     * @param gettableBean "context" object within which to resolve bean property references
     * @return <code>string</code> value with all property references resolved or
     * <code>null</code> if <code>string</code> was <code>null</code>
     */
    public String resolvePropertyReferences(String string, PropertyGettable<String> gettableBean) {

        if (string == null) {
            return null;
        }

        int exprStartPos = string.indexOf(mBrackets[0]);
        if (exprStartPos < 0) {
            return string;
        }

        int exprEndPos = string.indexOf(mBrackets[1], exprStartPos);
        if (exprEndPos < 0) {
            throw new IllegalArgumentException("missing closing bracket '"
                + mBrackets[1]
                + "' for property reference at offset "
                + exprStartPos
                + " in: '"
                + string
                + "'");
        }

        String propertyPathName = string.substring(exprStartPos
            + mBrackets[0].length(), exprEndPos);

        String propertyValue = gettableBean.getProperty(propertyPathName);

        return resolvePropertyReferences(string.substring(0, exprStartPos)
            + ((propertyValue == null) ? "" : propertyValue)
            + string.substring(exprEndPos + 1), gettableBean);
    }

}
