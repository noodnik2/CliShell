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

package clishell.test;

import clishell.StringListParser;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class StringListParserTest {


    @Test
    public void testOne() {

        String[][] stringListsWithExpectedTokens = {
            new String[] {"one,two", "one", "two" }
        ,   new String[] {"one two", "one", "two" }
        ,   new String[] {"one  two", "one", "two" }
        ,   new String[] {" one two ", "one", "two" }
        ,   new String[] {" one , two ", "one", "two" }
        ,   new String[] {"\"one\"\"two\"", "one", "two" }
        ,   new String[] {"\"one\" \"two\"", "one", "two" }
        ,   new String[] {"\"one\",\"two\"", "one", "two" }
        ,   new String[] {"\"one \",\" two\"", "one ", " two" }
        ,   new String[] {"\" one \",\"two \"", " one ", "two " }
        ,   new String[] {"\"one stuff\",\"two\"", "one stuff", "two" }
        ,   new String[] {"\"one\", \"two\"", "one", "two" }
        ,
        };

        char[] delimiters = {',' };

        for (String[] stringListWithExpectedTokens : stringListsWithExpectedTokens) {
            String stringList = stringListWithExpectedTokens[0];
//            System.out.println("string: \"" + stringList + "\"");
            String[] stringTokens = StringListParser
                .parseTokens(stringList, delimiters);
            Assert.assertEquals(stringList,
                 stringListWithExpectedTokens.length - 1,
                 stringTokens.length);
            int expectedTokenIndex = 1;
            for (String stringToken : stringTokens) {
//                System.out.println("  token: \"" + stringToken + "\"");
                Assert.assertEquals(stringList,
                    stringListWithExpectedTokens[expectedTokenIndex++],
                    stringToken);
            }
        }


    }
}
