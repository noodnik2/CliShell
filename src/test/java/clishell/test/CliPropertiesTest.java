

package clishell.test;

import java.io.IOException;
import java.io.StringReader;

import clishell.CliProperties;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * Tests for class <code>clishell.CliProperties</code>
 *
 */
public class CliPropertiesTest {


    //
    //  Public instance test methods
    //

    @Test
    public void testGetDottedPropertyWithInheritanceString() throws IOException {

        CliProperties cliProperties = getInitializedCliProperties();

        Assert.assertEquals("case 1: no prefix, property defined - direct hit",
            "hi from val1", cliProperties
                .getDottedPropertyWithInheritance("val1"));

        Assert.assertEquals("case 2: one component prefix, property defined - direct it",
            "hi from one.val1", cliProperties
                .getDottedPropertyWithInheritance("one.val1"));

        Assert.assertEquals("case 3 like case 1 but different property: no prefix, property defined - direct hit",
            "hi there val3", cliProperties
                .getDottedPropertyWithInheritance("val3"));

        Assert.assertNull("case 4: no prefix, property not defined - miss",
            cliProperties.getDottedPropertyWithInheritance("val4nodef"));

        Assert.assertEquals("case 5: single component prefix, property not defined - inherit from no prefix",
            "hi there val3", cliProperties
                .getDottedPropertyWithInheritance("one.val3"));

        Assert.assertEquals("case 6: two component prefix, property defined - direct it",
            "hi there val3", cliProperties
                .getDottedPropertyWithInheritance("one.two.val3"));

        Assert.assertEquals("case 7: two component prefix, property not defined - inherit from one component prefix",
            "one.val2 says hello", cliProperties
                .getDottedPropertyWithInheritance("one.two.val2"));

    }

    @Test
    public void testGetDottedPropertyWithInheritanceStringString() throws IOException {

        CliProperties cliProperties = getInitializedCliProperties();

        Assert.assertNull("case 1: miss, with null default value",
            cliProperties
                .getDottedPropertyWithInheritance("val4nodef", (String) null));

        Assert.assertEquals("case 2: miss, with default value",
            "default value for val4nodef", cliProperties
                .getDottedPropertyWithInheritance("val4nodef", "default value for val4nodef"));

    }

    @Test
    public void testPropertyReferenceResolution() throws IOException {

        CliProperties cliProperties = getInitializedCliProperties();

        final String expectedOneLevelResullt = "hello there hi from one.val1 nice!";
        Assert.assertEquals("case 1: one level substitution",
                expectedOneLevelResullt,
            cliProperties.getProperty("pps.1"));

        final String expectedTwoLevelResullt = "hi hello there hi from one.val1 nice! yea!";
        Assert.assertEquals("case 2: two level substitution",
                expectedTwoLevelResullt,
            cliProperties.getProperty("pps.2"));

        Assert.assertEquals("case 3: one and two level substitution",
            "[" + expectedTwoLevelResullt + "+" + expectedOneLevelResullt + "]",
            cliProperties.getProperty("pps.3"));

    }


    //
    //  Private instance methods
    //

    /**
     * @return test properties object, loaded with test properties
     */
    private CliProperties getInitializedCliProperties() throws IOException {
        final String[] testPropertiesSource = new String[] {
            "val1=hi from val1"
        ,   "val2=val2 hi there"
        ,   "val3=hi there val3"
        ,   "one.val1=hi from one.val1"
        ,   "one.val2=one.val2 says hello"
        ,   "one.two.val1=hi there, one.two.val1"
        ,   "pps.1=hello there ${one.val1} nice!"
        ,   "pps.2=hi ${pps.1} yea!"
        ,   "pps.3=[${pps.2}+${pps.1}]"
        ,
        };
        CliProperties cliProperties = new CliProperties();
        cliProperties.load(new StringReader(toString(testPropertiesSource)));
        return cliProperties;
    }

    /**
     * @param stringArray string array
     * @return catenation of entries from string array,
     * each separated by system line separator sequence
     */
    private String toString(String[] stringArray) {
        StringBuffer stringBuffer = new StringBuffer();
        boolean nonInitial = false;
        String newlineSeq = System.getProperty("line.separator");
        for (String stringValue : stringArray) {
            if (nonInitial) {
                stringBuffer.append(newlineSeq);
            } else {
                nonInitial = true;
            }
            stringBuffer.append(stringValue);
        }
        return stringBuffer.toString();
    }

}
