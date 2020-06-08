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

package clishell.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.TimeZone;

/**
 *
 * @author mross
 *
 */
public final class Instantiator {


    //
    //  Public class methods
    //

    /**
     * Constructs and returns an object instance of type <code>T</code>
     * (specified by <code>klass</code>) from its constructor that accepts
     * a single parameter of type <code>java.lang.String</code>.
     * NOTE: will attempt to construct an "empty", "null" or "zeroed"
     * object instance if the value of <code>string</code> passed is <code>null</code>.
     * @throws IllegalArgumentException could not create object of type
     * <code>T</code> from property value (see nested <code>Throwable</code>).
     * @throws NullPointerException if <code>klass</code> is <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public static <T> T newObjectFromString(String string, Class<T> klass)
        throws IllegalArgumentException {

        if (klass == null) {
            throw new NullPointerException("null 'klass' argument");
        }

        if (string == null) {
            return (T) newEmptyInstance(klass);
        }

        if (klass == String.class) {
            return (T) string;
        }

        String rejectedReason = "unsupported";
        Throwable rejectedException = null;

        if (klass.isPrimitive()) {
            try {
                return (T) newPrimitiveInstance(string, klass);
            } catch(NumberFormatException nfe) {
                rejectedReason = nfe.getClass().getSimpleName();
                rejectedException = nfe;
            }
        }

        if (klass == TimeZone.class) {
            return (T) TimeZone.getTimeZone(string);
        }

        try {

            // allow construction of class instance using default constructor
            if (klass == Class.class) {
                return (T) Class.forName(string);
            }

            // allow construction of an enum value
            if (klass.isEnum()) {
                Method valueOfMethod = klass.getMethod("valueOf", new Class[] {String.class });
                return (T) valueOfMethod.invoke(null, new Object[] {string });
            }

            // otherwise attempt to construct instance from
            // class constructor that accepts a single string
            Constructor<T> stringConstructor = klass.getConstructor(new Class[] {String.class });
            return stringConstructor.newInstance(new Object[] {string });

        } catch(Exception ex) {
            rejectedException = ex;
        }

        throw new IllegalArgumentException("could not construct instance of type: '"
                + klass.getName()
                + "' from property value, reason="
                + rejectedReason, rejectedException);
    }


    //
    //  Private class methods
    //

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static Object newEmptyInstance(Class klass)
        throws IllegalArgumentException {

        if (klass == Boolean.TYPE) {
            return Boolean.FALSE;
        }

        if (klass == Byte.TYPE) {
            return Byte.valueOf((byte) 0);
        }

        if (klass == Byte.TYPE) {
            return Byte.valueOf((byte) 0);
        }

        if (klass == Character.TYPE) {
            return Character.valueOf('\0');
        }

        if (klass == Double.TYPE) {
            return Double.valueOf(0.0);
        }

        if (klass == Float.TYPE) {
            return Float.valueOf(0.0f);
        }

        if (klass == Integer.TYPE) {
            return Integer.valueOf(0);
        }

        if (klass == Long.TYPE) {
            return Long.valueOf(0L);
        }

        if (klass == Short.TYPE) {
            return Short.valueOf((short) 0);
        }

        if (klass == Short.TYPE) {
            return Short.valueOf((short) 0);
        }

        return throwUnsupportedTypeException(klass);
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static Object newPrimitiveInstance(String string, Class klass)
        throws NumberFormatException, IllegalArgumentException {

        if (klass == Boolean.TYPE) {
            return Boolean.valueOf(string);
        }

        if (klass == Byte.TYPE) {
            return Byte.valueOf(string);
        }

        if (klass == Byte.TYPE) {
            return Byte.valueOf(string);
        }

        if (klass == Character.TYPE) {
            return new Character(string.charAt(0));
        }

        if (klass == Double.TYPE) {
            return Double.valueOf(string);
        }

        if (klass == Float.TYPE) {
            return Float.valueOf(string);
        }

        if (klass == Integer.TYPE) {
            return Integer.valueOf(string);
        }

        if (klass == Long.TYPE) {
            return Long.valueOf(string);
        }

        if (klass == Short.TYPE) {
            return Short.valueOf(string);
        }

        return throwUnsupportedTypeException(klass);
    }

    /**
     * @param klass
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    private static Object throwUnsupportedTypeException(Class klass)
        throws IllegalArgumentException {

        throw new IllegalArgumentException("unsupported type: '"
                + klass.getName() + "'");
    }


    //
    //  Private instance constructors
    //

    /**
     *
     */
    private Instantiator() {
        ;       // utility class does not need to be constructed
    }

}
