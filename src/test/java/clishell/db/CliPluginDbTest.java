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

package clishell.db;

import junit.framework.Assert;

import clishell.NamedPlugin;
import clishell.ex.CliRejectedInputException;
import org.junit.Test;


/**
 *
 *
 *
 */
public class CliPluginDbTest {

    @Test
    public void testOne() throws CliRejectedInputException {
        CliPluginDb<NamedPluginTest> cliPluginDb = createCliPluginDb(new String[] {"hi" });
        Assert.assertEquals(cliPluginDb.getPluginCount(), 1);
        cliPluginDb.removePlugin("hi");
        Assert.assertEquals(cliPluginDb.getPluginCount(), 0);
    }


    /**
     * @return
     */
    private CliPluginDb<NamedPluginTest> createCliPluginDb(String[] pluginNames)
        throws CliRejectedInputException {

        CliPluginDb<NamedPluginTest> cliPluginDb
            = new CliPluginDb<NamedPluginTest>();

        for (String pluginName : pluginNames) {
            NamedPluginTest namedPluginTest = new NamedPluginTest(pluginName);
            cliPluginDb.addPlugin(pluginName, namedPluginTest);
        }

        return cliPluginDb;
    }

    /**
     *
     *
     *
     */
    class NamedPluginTest implements NamedPlugin {

        private final String mName;

        public NamedPluginTest(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

    }


}
