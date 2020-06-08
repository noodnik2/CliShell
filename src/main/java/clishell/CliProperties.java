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

import java.util.Properties;

import clishell.reflection.Instantiator;
import clishell.util.PropertyGettable;
import clishell.util.PropertyReferenceResolver;


/**

    Custom Extension to <code>java.util.Properties</code> for
    CliShell, supporting following enhancements:

   <dl>
     <dt>
        getProperties enhancements:
     </dt>
     <dd>
        <br />
        <ol>
          <li>
              Resolution of property references - the overridden <code>getProperty</code>
              methods will resolve references to other properties using the syntax:
              <code>${</code><i>property-name</i><code>}</code>
          </li>
          <li>
              Two new <code>getProperty</code> methods: <code>getProperty(String name, Class<T> klass)</code>
              and a variant of that taking a <code>defaultValue</code> parameter - can be
              used to return new instances of objects using values configured in the
              property file
          </li>
        </ol>
     </dd>
     <br />
     <dt>
        getDottedProperty enhancements:
     </dt>
     <dd>
       <br />
       New flavor of "get property" methods: "getDottedProperty"

       A "dotted property name" is a property name comprising zero
       or more "components", separated by a dot ('.') character.

       A form of "property inheritance" is supported by applying
       the following rules to dotted properties:
       <br />

       <ol>
         <li>
            lookup property using the dotted property name
            and return the value if found
         </li>
         <li>
            while there exists a penultimate component (one prior
            to the last) of the dotted property name, remove it
            and look for a property value using the modified
            name, returning it if found
         </li>
         <li>
            return the value of the property named by the last
            component of the dotted property name
         </li>
       </ol>
       <br />
       Example:
       <br />
       Suppose you have the following properties defined:
       <pre>
       name=default value
       prod.name=prod value
       dev.name=dev value
       prod.special.name=special prod value
       </pre>
       Using <code>getDottedPropertyWithInheritance(String)</code>,
       the following lookups and return values should be demonstrated:
       <br />

       <table border=1 cellpadding=3>
         <tr>
           <th align=left>name</th>
           <th align=left>value</th>
           <th align=left>notes</th>
         </tr>
         <tr>
           <td>name</td>
           <td>default value</td>
           <td>&nbsp;</td>
         </tr>
         <tr>
            <td>prod.name</td>
            <td>prod value</td>
            <td>&nbsp;</td>
         </tr>
         <tr>
            <td>uat.name</td>
            <td>default value</td>
            <td>since 'uat.name' was not defined, it stripped off 'uat.' and tried just 'name'</td>
         </tr>
         <tr>
            <td>prod.special.thing.name</td>
            <td>special prod value</td>
            <td>stripped off 'thing.' and found the value of 'prod.special.name'</td>
         </tr>
       </table>

     </dd>
   </dl>

 */
public class CliProperties extends Properties implements PropertyGettable<String> {


    //
    // Public instance data
    //

    /** serialization magic constant */
    public static final long serialVersionUID = 2137893580L;

    /** resolver for embedded property references */
    private final PropertyReferenceResolver mPropertyReferenceResolver
        = new PropertyReferenceResolver("${", "}");


    //
    //  Public constructors
    //

    /**
     * @see Properties#Properties()
     */
    public CliProperties() {
        // nothing to do for default constructor
    }

    /**
     * @see Properties#Properties(Properties)
     */
    public CliProperties(Properties properties) {
        super(properties);
    }


    //
    //  Public @Override instance methods
    //

    /**
     * @param name property name
     * @return value value returned by {@link #getProperty(String, String)}
     * where first argument passed is <code>name</code> and second
     * argument passed is <code>(String) null</code>.
     */
    @Override
    public String getProperty(String name) {
        return mPropertyReferenceResolver.resolvePropertyReferences(super.getProperty(name), this);
    }

    /**
     * @param name property name
     * @param defaultValue default value to return if property
     * specified by <code>name</code> is not defined
     * @return value of property specified by <code>name</code>,
     * after resolving any embedded references, returning
     * <code>defaultValue</code> if property <code>name</code>
     * is not defined.
     * @see Properties#getProperty(String, String)
     */
    @Override
    public String getProperty(String name, String defaultValue) {
        return mPropertyReferenceResolver.resolvePropertyReferences(
            super.getProperty(name, defaultValue), this);
    }

    //
    //  Public instance methods
    //  CliProperties enhancements to java.util.Properties
    //

    //
    //  Additional overloads to base "getProperty" methods
    //


    /**
     * Same as {@link #getProperty(String)}, except will construct
     * and return an object of type <code>T</code> initialized with
     * the property value.  If the property value is <code>null</code>,
     * then will return <code>null</code>.
     * @throws IllegalArgumentException could not create object of type
     * <code>T</code> from property value (see nested <code>Throwable</code>).
     * @see Properties#getProperty(String)
     */
    public <T> T getProperty(String name, Class<T> klass)
        throws IllegalArgumentException {
        String propertyValueString = getProperty(name);
        if (propertyValueString == null) {
            return null;
        }
        return Instantiator.newObjectFromString(propertyValueString, klass);
    }

    /**
     * Same as {@link #getProperty(String)}, except will construct
     * and return an object of type <code>T</code> initialized with
     * the property value.  If the property value is <code>null</code>,
     * then will return <code>defaultValue</code>.
     * @throws IllegalArgumentException could not create object of type
     * <code>T</code> from property value (see nested <code>Throwable</code>).
     * @see Properties#getProperty(String, String)
     */
    public <T> T getProperty(String name, T defaultValue, Class<T> klass)
        throws IllegalArgumentException {
        T propertyValue = getProperty(name, klass);
        return (propertyValue != null) ? propertyValue : defaultValue;
    }

    /**
     * @param componentNames component part(s) of property name, in order
     * @param off starting index within <code>componentNames</code>
     * to use when building the dotted property name
     * @param len number of elements within <code>componentNames</code>
     * to use when building the dotted property name
     * @return complete property name after assembling from specified
     * elements of <code>componentNames</code>
     * specified in <code>componentNames</code>.
     */
    public String getDottedPropertyName(String[] componentNames, int off, int len) {
        StringBuffer propertyNameBuffer = new StringBuffer();
        boolean nonInitial = false;
        for (int i = 0; i < len; i++) {
            if (nonInitial) {
                propertyNameBuffer.append('.');
            } else {
                nonInitial = true;
            }
            propertyNameBuffer.append(componentNames[i + off]);
        }
        return propertyNameBuffer.toString();
    }

    /**
     * @param componentNames component part(s) of property name, in order
     * @return complete property name after assembling from all
     * elements of <code>componentNames</code>
     */
    public String getDottedPropertyName(String... componentNames) {
        return getDottedPropertyName(componentNames, 0, componentNames.length);
    }

    /**
     * @param dottedPropertyName name of "dotted" property
     * @return value from property
     */
    public String getDottedPropertyWithInheritance(String dottedPropertyName) {

        // break dotted property name into components
        String[] componentNames = dottedPropertyName.split("\\.");

        // if there were no components in the name, value is null
        if (componentNames.length == 0) {
            return null;
        }

        // grab "name" part - final component - of dotted property name
        String propertyNameComponent = componentNames[componentNames.length - 1];

        // construct "prefix" part in a loop starting with initial component(s)
        // of dotted property name, removing the last component for each subsequent
        // loop, until either find a property value, or there are no more components
        // to remove (in which case, return null)
        for (int i = componentNames.length - 1; i > 0; i--) {
            // construct a new candidate "dotted property name" using just
            // the initial 'i' components of original dotted property name
            String candidatePropertyName = getDottedPropertyName(
                getDottedPropertyName(componentNames, 0, i),
                propertyNameComponent
            );
            // if we find a value using the candidate name, return it
            String propertyValue = getProperty(candidatePropertyName);
            if (propertyValue != null) {
                return propertyValue;
            }

        }

        // try to find this property with no prefix
        return getProperty(propertyNameComponent);
    }

    /**
     * Same as {@link #getDottedPropertyWithInheritance(String)}, except will
     * construct return {@code defaultValue} instead of <code>null</code>
     * @param dottedPropertyName name of "dotted" property
     * @param defaultValue default value to return if property
     * specified by <code>dottedPropertyName</code> is not defined
     * @return value of property specified by <code>dottedPropertyName</code>
     */
    public String getDottedPropertyWithInheritance(String dottedPropertyName,
            String defaultValue) {
        String propertyValue = getDottedPropertyWithInheritance(dottedPropertyName);
        return (propertyValue != null) ? propertyValue : defaultValue;
    }

    /**
     * Same as {@link #getDottedPropertyWithInheritance(String)}, except
     * will construct and return an object of type <code>T</code> initialized
     * with the returned property value.  If the property value is <code>null</code>,
     * then will return <code>null</code>.
     * @return object of specified type initialized with property value
     * @throws IllegalArgumentException could not create object of type
     * <code>T</code> from property value (see nested <code>Throwable</code>).
     */
    public <T> T getDottedPropertyWithInheritance(String dottedPropertyName,
            Class<T> klass) throws IllegalArgumentException {
        String propertyValueString = getDottedPropertyWithInheritance(dottedPropertyName);
        if (propertyValueString == null) {
            return null;
        }
        return Instantiator.newObjectFromString(propertyValueString, klass);

    }

    /**
     * Same as {@link #getDottedPropertyWithInheritance(String, String)}, except
     * will construct and return an object of type <code>T</code> initialized
     * with the returned property value.
     * @return object of specified type initialized with property value
     * or default value (if property value was <code>null</code>)
     * @throws IllegalArgumentException could not create object of type
     * <code>T</code> from property value or default value (see nested
     * <code>Throwable</code> for details).
     */
    public <T> T getDottedPropertyWithInheritance(String dottedPropertyName,
            T defaultValue, Class<T> klass) throws IllegalArgumentException {
        T propertyValue = getDottedPropertyWithInheritance(dottedPropertyName, klass);
        return (propertyValue != null) ? propertyValue : defaultValue;
    }

    /**
     * @param stringWithPropertyReferences
     * @return <code>stringWithPropertyReferences</code> with
     * any / all property references resolved
     */
    public String resolve(String stringWithPropertyReferences) {
        return mPropertyReferenceResolver.resolvePropertyReferences(
                stringWithPropertyReferences, this);
    }


}
