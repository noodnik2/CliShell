/**
 * 
 *
 *  
 *
 *
 * 
 * @author MRoss
 * 
 */

package wccommon.cli;

import java.lang.reflect.Method;
import java.util.Map;

public final class CliUtils {
    
    /**
     * Can't instantiate - utility class
     */
    private CliUtils() {
        // nothing to do
    }
    
    /**
     * @param method method whose <code>CliMethodForm</code> is being queried
     * @return <code>CliMethodForm</code> (enum) value corresponding to specified
     * method's argument / return type format.  Returns <code>CliMethodForm.UNKNOWN</code>
     * if <code>method</code> does not have a recognized <code>CliMethodForm</code>.
     * @throws CliException
     */
    public static CliMethodForm getMethodForm(Method method)
    throws CliException {

        Class<?> methodReturnType = method.getReturnType(); 
        if (methodReturnType == void.class) {

            Class<?>[] argTypes = method.getParameterTypes();
            if (argTypes.length == 0) {
                return CliMethodForm.VOID_NOPARAM;
            }

            boolean hasOptions = false;
            int firstUserArgIndex = 0;
            if (Map.class.isAssignableFrom(argTypes[0])) {
                hasOptions = true;
                firstUserArgIndex = 1;
            }

            boolean isAllStrings = false;
            for (int i = firstUserArgIndex; i < argTypes.length; i++) {
                isAllStrings = true;
                if (argTypes[i] != String.class) {
                    isAllStrings = false;
                    break;
                }
            }

            if (isAllStrings) {
                if (hasOptions) {
                    return CliMethodForm.VOID_OPTIONS_STRINGS;
                }
                return CliMethodForm.VOID_STRINGS;
            }

            boolean isStringArray = false;
            if (!isAllStrings) {
                if (firstUserArgIndex < argTypes.length) {
                    if (argTypes[firstUserArgIndex] == String[].class) {
                        if (firstUserArgIndex == (argTypes.length - 1)) {
                            isStringArray = true;
                        }
                    }
                }
            }

            if (hasOptions && isStringArray) {
                return CliMethodForm.VOID_OPTIONS_STRINGARRAY;
            }
            
            if (hasOptions) {
                return CliMethodForm.VOID_OPTIONS;
            }
            
            if (isStringArray) {
                return CliMethodForm.VOID_STRINGARRAY;
            }
            
            return CliMethodForm.UNKNOWN;
        }
        
        return CliMethodForm.UNKNOWN;
    }

    
    /**
     * @param method method for whom invocation signature is requested 
     * @return human-readable "method signature" describing <code>method</code>'s
     * argument(s) and return type
     */
    public static String getMethodSignature(Method method) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        StringBuffer stringBuffer = new StringBuffer(method.getDeclaringClass().getName());
        stringBuffer.append(".");
        stringBuffer.append(method.getName());
        stringBuffer.append("(");
        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (i > 0) {
                stringBuffer.append(",");
            }
            stringBuffer.append(methodParameterTypes[i].getName());
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

}
