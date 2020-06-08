package clishell.util;

/**
 *
 *  Interface for an object that supports a "getProperty" method accepting
 *  a string and returning an object of the parameterized type T.
 *
 */
public interface PropertyGettable<T> {

    /**
     * @param propertyName name of "property" to retrieve from this object
     * @return property value
     */
    public T getProperty(String propertyName);

}
