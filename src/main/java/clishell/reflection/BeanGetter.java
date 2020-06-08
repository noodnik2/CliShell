

package clishell.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import clishell.util.PropertyGettable;

/**
 *  Bean Utility class
 */
public class BeanGetter implements PropertyGettable<String> {


    //
    //  Private class data
    //

//    /** logger to use for reporting warnings / error conditions */
//    private static final Logger LOGGER = Logger.getLogger(BeanGetter.class.getCanonicalName());


    //
    //  Private instance data
    //

    /** current context bean (for methods that use it) */
    private Object mBean;


    //
    //  Public instance constructors
    //

    /**
     * @param bean java bean to place into context for the {@link #getProperty(String)} method
     */
    public BeanGetter(Object bean) {
        mBean = bean;
    }

    /**
     *
     */
    public BeanGetter() {
        this(null);
    }


    //
    //  Public instance methods
    //

    /**
     * @param bean java bean to place into context for the {@link #getProperty(String)} method
     * @return this object returned to promote "chaining" if desired (user beware of potential
     * for misuse of this feature!)
     */
    public BeanGetter setBean(Object bean) {
        mBean = bean;
        return this;
    }

    /**
     * @param propertyPathName "path" to "property" whose value is to be extracted from
     * the context bean which has been set either from the constructor, or via the
     *
     */
    public String getProperty(String propertyPathName) {
        if (propertyPathName == null || mBean == null) {
            return null;
        }
        Object propertyValue = getBeanPropertyPath(mBean, propertyPathName);
        if (propertyValue != null) {
            return propertyValue.toString();
        }
        return null;
    }

    /**
     * @param bean java bean from which "property value" is to be extracted
     * @param propertyPathName "path" to "property" whose value is to be extracted from <code>bean</code>
     * @return object (aka "property") value of the property referenced by <code>propertyPathName</code>,
     * or <code>null</code> if that could not be located / retrieved for any reason
     * <dl>
     *  NOTES:
     *    <ol>
     *      <li>
     *          <code>propertyPathName</code> uses a "dotted" notation to separate components of the
     *          "path" to the desired property; e.g., "mybean1.mynestedbean2.myproperty" will return
     *          the value of the property "myproperty" from the bean retrieved from the path name
     *          "mybean.mynestedbean2".
     *      </li>
     *      <li>
     *          In the initial implementation, there is no convenient way to distinguish between "normal"
     *          an "error" cases when <code>null</code> is returned; however, "WARNING" log message(s) are
     *          printed for error cases.
     *      </li>
     *    </ol>
     * </dl>
     */
    public static Object getBeanPropertyPath(Object bean, String propertyPathName) {

        String[] propertyPathNameArray = propertyPathName.split("\\.");

        Object currentBean = bean;
        for (String propertyName : propertyPathNameArray) {
            currentBean = getBeanProperty(currentBean, propertyName);
        }

        return currentBean;
    }


    //
    //  Private instance methods
    //

    /**
     * @param bean java bean from which "property value" is to be extracted
     * @param propertyName "name" of "property" whose value is to be extracted from <code>bean</code>
     * @return value of specified bean property, or <code>null</code> if could not be found
     */
    private static Object getBeanProperty(Object bean, String propertyName) {

        if (bean == null) {
            // return null for a null bean
            return null;
        }

        // get java class object for the bean
        Class<?> beanClass = bean.getClass();

        // loop over all supported "method name prefixes", in "priority" order
        for (String propertyNamePrefix : new String[] { "get", "is", "to" }) {

            // see if a java method with indicated prefix and name exists within the class
            Method beanPropertyMethod;
            String propertyMethodName = getPropertyMethodName(propertyNamePrefix, propertyName);
            try {
                beanPropertyMethod = beanClass.getMethod(propertyMethodName, new Class[] {});
            } catch(NoSuchMethodException nsme) {
                // this method doesn't exist - try next prefix if
                continue;
            }

            // found the method!  now, let's call it
            try {
                return beanPropertyMethod.invoke(bean, new Object[] {});
            } catch(InvocationTargetException ite) {
//                LOGGER.log(Level.SEVERE, propertyName, ite);
                // continue;
            } catch(IllegalAccessException iae) {
//                LOGGER.log(Level.SEVERE, propertyName, iae);
                // continue;
            }

        }

//        LOGGER.log(Level.WARNING, "no java method found for bean property '" + propertyName + "'");

        return null;
    }

    /**
     * @param propertyPrefix prefix of "bean property"
     * @param propertyName name of "bean property"
     * @return name of Java method corresponding to <code>propertyName</code>
     */
    private static String getPropertyMethodName(String propertyPrefix, String propertyName) {

        if (propertyName == null || propertyName.trim().length() == 0) {
            return propertyPrefix;
        }

        return propertyPrefix
            + propertyName.substring(0, 1).toUpperCase()
            + propertyName.substring(1);
    }

}
