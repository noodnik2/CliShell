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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import clishell.ex.CliPluginMethodException;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;

public final class CliMethod {


    //
    // Public class data
    //

    /**
     * SUPPORTED_CLIMETHODFORMS - array specifying the set of
     * currently supported java method types, according to
     * classification specified by <code>CliMethodForm</code>;
     * method operations on this class for other types may not
     * work (especially the <code>invoke()</code> method and its
     * variants)
     */
    public static final CliMethodForm[] SUPPORTED_CLIMETHODFORMS = {
        CliMethodForm.VOID_NOPARAM
    ,   CliMethodForm.VOID_STRINGS
    ,   CliMethodForm.VOID_STRINGARRAY
    ,   CliMethodForm.VOID_OPTIONS
    ,   CliMethodForm.VOID_OPTIONS_STRINGS
    ,   CliMethodForm.VOID_OPTIONS_STRINGARRAY
    ,
    };


    //
    // Private instance data
    //

    /** mMethod - the java method backing the <code>CliMethod</code> */
    private final Method mMethod;

    /**
     * mMethodForm
     * the <code>CliMethodForm</code> value classifying the type (aka "form")
     * of the java method - mainly used for purposes of method invocation
     */
    private final CliMethodForm mMethodForm;

    /**
     * Number of required user parameters, or -1 if "variable"
     * (e.g., will be "variable" for all <code>CliMethodForm</code>s
     * that pass "_STRINGS")
     */
    private final int mNumUserParameters;


    //
    // Public constructor methods
    //

    /**
     * constructs a <code>CliMethod</code> wrapping the specified java method
     * will immediately inspect the java method in order to compute the
     * method's <code>CliMethodForm</code>, which can then be requested by
     * using <code>getCliMethodForm()</code>
     * @param method java method to wrap in this <code>CliMethod</code> object
     */
    public CliMethod(Method method) {
        mMethod = method;
        mMethodForm = computeCliMethodForm();
        mNumUserParameters = computeCliNumUserParameters();
    }


    //
    // Public instance methods
    //

    /**
     * @return the java method wrapped by this <code>CliMethod</code>
     */
    public Method getMethod() {
        return mMethod;
    }

    /**
     * @return the <code>CliMethodForm</code> classifier of the wrapped
     * java method
     */
    public CliMethodForm getCliMethodForm() {
        return mMethodForm;
    }

    /**
     * @return the numUserParameters
     */
    public int getNumUserParameters() {
        return mNumUserParameters;
    }

    /**
     * @param instance object instance to invoke method on
     * @param options object containing command line options
     * @param args CLI arguments (args[0] is CLI command, args[1..n]
     * are CLI command-line arguments as individual tokens); at least
     * args[0] is required
     * @param nCommandArgs number of arguments at front of <code>args</code>
     * comprising "command name" part of command (note that the command can
     * be invoked by the user via different command names, so this value
     * will not be known until execution time)
     * @return object returned by invoked method
     * @throws CliRunnerException unhandled exception
     * @see #invokeMethod(Object, Object[])
     */
    public Object invokeCliCommand(Object instance, CliCommandOptions options,
        String[] args, int nCommandArgs) throws CliRunnerException {

        String[] params = new String[] {};
        if (mMethodForm != CliMethodForm.VOID_NOPARAM) {
            // strip of "command name" from argument array
            if (args.length > nCommandArgs) {
                params = Arrays.copyOfRange(args, nCommandArgs, args.length);
            }
        }

        if (
            (mMethodForm == CliMethodForm.VOID_STRINGS)
                || (mMethodForm == CliMethodForm.VOID_NOPARAM)) {
            return invokeMethod(instance, params);
        }

        if (mMethodForm == CliMethodForm.VOID_STRINGARRAY) {
            return invokeMethod(instance, new Object[] {params});
        }

        if (mMethodForm == CliMethodForm.VOID_OPTIONS_STRINGS) {
            Object[] invokeParams = new Object[params.length + 1];
            invokeParams[0] = options;
            for (int i = 0; i < params.length; i++) {
                invokeParams[i + 1] = params[i];
            }
            return invokeMethod(instance, invokeParams);
        }

        if (mMethodForm == CliMethodForm.VOID_OPTIONS) {
            return invokeMethod(instance, new Object[] {options });
        }

        if (mMethodForm == CliMethodForm.VOID_OPTIONS_STRINGARRAY) {
            return invokeMethod(instance, new Object[] {options, params });
        }

        throw new CliRunnerException("unsupported method form ("
                + mMethodForm
                + ") for '"
                + getMethodSignature()
                + "'");
    }

    /**
     * @param instance object instance
     * @return object returned by invoked method
     * @throws CliRunnerException unhandled CLI Method exception
     * @see #invokeMethod(Object, Object[])
     */
    public Object invoke(Object instance) throws CliRunnerException {

        if (mMethodForm != CliMethodForm.VOID_NOPARAM) {
            throw new CliRunnerException("unsupported method form ("
                    + mMethodForm
                    + ") for '"
                    + getMethodSignature()
                    + "'");
        }

        return invokeMethod(instance, null);
    }

    /**
     * @return human-readable "method signature" describing method's
     * argument(s) and return type
     */
    public String getMethodSignature() {
        Class<?>[] methodParameterTypes = mMethod.getParameterTypes();
        StringBuffer stringBuffer = new StringBuffer(mMethod.getDeclaringClass().getName());
        stringBuffer.append(".");
        stringBuffer.append(mMethod.getName());
        stringBuffer.append("(");
        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (i > 0) {
                stringBuffer.append(",");
            }
            stringBuffer.append(methodParameterTypes[i].getSimpleName());
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    /**
     * Calls native "invoke" method of wrapped java method on the specified object
     * instance and returns result.  Wraps call in a "try / catch" to standardize
     * the exception handling so that only a <code>CliRunnerException</code> is thrown.
     * @param instance object instance to invoke method on; may be
     * <code>null</code> if static method is called
     * @param args arguments to pass to object; may be <code>null</code> if no
     * arguments are being passed, and may be shorter than required number of
     * arguments required by call (in which case null values will be supplied
     * for all final method arguments which are not supplied).
     * @return object returned by invoked method
     * @throws CliRunnerException unhandled exception
     * @see java.lang.reflect.Method#invoke(Object, Object...)
     */
    public Object invokeMethod(Object instance, Object[] args)
        throws CliRunnerException {

        try {
            // get actual parameter types of java method
            Class<?>[] parameterTypes = mMethod.getParameterTypes();
            // local copy of user's invocation arguments
            Object[] invokeArgs = args;
            // if method takes arguments...
            if (parameterTypes.length > 0) {
                // if no arguments were passed...
                if (args == null) {
                    // create empty argument array of correct length
                    invokeArgs = new Object[parameterTypes.length];
                } else {
                    // validate that user didn't pass too many arguments
                    if (args.length > parameterTypes.length) {
                        throw new CliRejectedInputException("too many arguments supplied");
                    }
                    // pad arguments to correct length if necessary
                    if (args.length < parameterTypes.length) {
                        invokeArgs = Arrays.copyOf(args, parameterTypes.length);
                    }
                }
            }
            return mMethod.invoke(instance, invokeArgs);
        } catch(IllegalAccessException iae) {
            throw new CliRunnerException("access denied to method: '"
                + getMethodSignature() + "'", iae);
        } catch(InvocationTargetException ite) {
            Throwable exceptionToPropagate = ite.getCause();
            if (exceptionToPropagate instanceof CliRunnerException) {
                // avoid adding another exception wrapper if
                // called method already generated a CliRunnerException
                throw (CliRunnerException) exceptionToPropagate;
            }
            if (exceptionToPropagate == null) {
                exceptionToPropagate = ite;
            }
            throw new CliPluginMethodException(getMethodSignature()
                + ": "
                + exceptionToPropagate.getMessage(),
                    exceptionToPropagate);
        }
    }


    //
    // Private instance methods
    //

    /**
     * @return <code>CliMethodForm</code> (enum) value corresponding to method's
     * argument & return type format.  Returns <code>CliMethodForm.UNKNOWN</code>
     * if <code>method</code> does not have a recognized <code>CliMethodForm</code>.
     */
    private CliMethodForm computeCliMethodForm() {

        Class<?> methodReturnType = mMethod.getReturnType();
        if (methodReturnType == void.class) {

            Class<?>[] argTypes = mMethod.getParameterTypes();
            if (argTypes.length == 0) {
                return CliMethodForm.VOID_NOPARAM;
            }

            boolean hasOptions = false;
            int firstUserArgIndex = 0;
            if (CliCommandOptions.class.isAssignableFrom(argTypes[0])) {
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
            if (firstUserArgIndex < argTypes.length) {
                if (argTypes[firstUserArgIndex] == String[].class) {
                    if (firstUserArgIndex == (argTypes.length - 1)) {
                        isStringArray = true;
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
     * precondition: <code>mMethodForm</code> and <code>mMethod</code> instance
     * members are correctly assigned
     * @return the number of parameters required by this method, or -1 if it accepts
     * a variable, indeterminate number of parameters
     */
    private int computeCliNumUserParameters() {

        if (mMethodForm == CliMethodForm.VOID_NOPARAM) {
            // no arguments passed
            return 0;
        }

        if (mMethodForm == CliMethodForm.VOID_OPTIONS) {
            // no arguments passed
            return 0;
        }

        if (mMethodForm == CliMethodForm.VOID_STRINGS) {
            // all arguments are used by the user parameters
            return mMethod.getParameterTypes().length;
        }

        if (mMethodForm == CliMethodForm.VOID_OPTIONS_STRINGS) {
            // one argument is reserved for the options
            return mMethod.getParameterTypes().length - 1;
        }

        // method accepts an indeterminate number of arguments
        return -1;
    }

}
