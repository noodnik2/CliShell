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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import clishell.NamedPlugin;
import clishell.ex.CliRejectedInputException;

/**
 *
 * Encapsulates data structures used to store and quickly search plugins
 * to / from the set of plugins currently loaded into the CLI.
 *
 */
public class CliPluginDb<T extends NamedPlugin> {


    //
    //  Private instance data
    //

    /**
     * Maps plugin names to plugin instances
     * NOTE: use of <code>LinkedHashMap</code> to preserve ordering of entries
     */
    private final Map<String, T> mPluginMap = new LinkedHashMap<String, T>();


    //
    //  Public methods
    //

    /**
     * @param pluginName name of plugin to remove
     * @param namedPlugin metadata for plugin
     * @throws CliRejectedInputException could not add specified plugin;
     * a plugin with the same identifier (<code>pluginName</code>)
     * is already contained in the plugin database.
     */
    public void addPlugin(String pluginName, T namedPlugin)
        throws CliRejectedInputException {
        if (mPluginMap.get(pluginName) != null) {
            throw new CliRejectedInputException("plugin named '"
                + pluginName
                + "' already loaded");
        }
        mPluginMap.put(pluginName, namedPlugin);
    }

    /**
     * @param pluginName name of plugin to remove
     * @return metadata for plugin removed, or <code>null</code> if not found
     */
    public T removePlugin(String pluginName) {
        return mPluginMap.remove(pluginName);
    }

    /**
     * @param pluginName name of plugin to retrieve
     * @return metadata for specified plugin, or <code>null</code> if not found
     */
    public T getPlugin(String pluginName) {
        return mPluginMap.get(pluginName);
    }

    /**
     * @return iterable to list of plugins in the order they
     * were loaded into the plugin database
     */
    public Iterable<T> cliPlugins() {
        return new ArrayList<T>(mPluginMap.values());
    }

    /**
     * @return iterable to names of plugins in the order they
     * were loaded into the database
     */
    public Iterable<String> cliPluginNames() {
        return new ArrayList<String>(mPluginMap.keySet());
    }


    //
    // Package-private methods
    //

    /**
     * @return number of plugins loaded
     */
    int getPluginCount() {
        return mPluginMap.size();
    }

}
