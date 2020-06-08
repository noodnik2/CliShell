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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import clishell.CliPluginCommandMetadata;
import clishell.CommandName;
import clishell.FullCommandNameTree;
import clishell.ex.CliRunnerException;


/**
 *
 * Encapsulates data structures used to store and quickly find the set
 * of cli commands currently loaded across all plugins.
 *
 */
public class CliCommandDb {


    //
    //  Private instance data
    //

    /** map of command name to map of supporting plugins */
    private final Map<CommandName, Map<String, CliPluginCommandMetadata>> mCommandMap
        = new LinkedHashMap<CommandName, Map<String, CliPluginCommandMetadata>>();

    /** tree structure backing command names for quick navigation */
    private final FullCommandNameTree mCommandNameTree = new FullCommandNameTree();


    //
    //  Public instance methods
    //

    /**
     * @return set of full command names that match the initial token(s) of
     * the supplied <code>commandWithParameters</code>
     * @see FullCommandNameTree#findCommandNamesFromCommandLine(CommandName)
     */
    public Set<CommandName> findCommandNamesFromCommandLine(CommandName commandWithParameters) {
        return mCommandNameTree.findCommandNamesFromCommandLine(commandWithParameters);
    }

    /**
     * @param out "dumps" the command tree to the specified print writer,
     * mainly useful for debugging
     */
    public void printCommandDbAsTree(PrintWriter out) {
        mCommandNameTree.printAsTree(out);
    }

    /**
     * @param pluginName plugin supporting the commands in <code>commandMetadataCollection</code>
     * @param commandMetadataCollection collection of commands to register into command database
     * @throws CliRunnerException unhandled exception from adding command(s); NOTE: if thrown,
     * it's likely that not all commands in the collection were loaded for the plugin
     */
    public void addCommands(String pluginName,
        Collection<CliPluginCommandMetadata> commandMetadataCollection)
        throws CliRunnerException {

        // for each command in the collection...
        for (CliPluginCommandMetadata cliPluginCommandMetadata : commandMetadataCollection) {
            // register the command in the command database
            // under each of the names it's known by
            for (CommandName commandName : cliPluginCommandMetadata.getNames()) {
                addCommandName(pluginName, commandName, cliPluginCommandMetadata);
            }
        }

    }

    /**
     * Removes all commands registered for the specified plugin
     * @param pluginName unique identifier for plugin
     */
    public void removeCommands(String pluginName) {

        // iterate over a copy of the keys to the map, since we're going
        // to be deleting the map entries
        CommandName[] commandNamesToRemove = mCommandMap.keySet()
            .toArray(new CommandName[0]);

        for (CommandName commandName : commandNamesToRemove) {
            removeCommandName(pluginName, commandName);
        }

    }

    /**
     * @param pluginName unique identifier for plugin
     * @param commandName specifies command for which to retrieve metadata
     * @return CLI plugin command metadata supporting <code>commandName</code>,
     * or <code>null</code> if not found
     */
    public CliPluginCommandMetadata getCommandMetadata(String pluginName,
            CommandName commandName) {

        Map<String, CliPluginCommandMetadata> supportingPluginMap
            = mCommandMap.get(commandName);

        if (supportingPluginMap == null) {
            return null;
        }

        return supportingPluginMap.get(pluginName);
    }


    //
    //  Private, internal methods
    //

    /**
     * @param pluginName unique identifier for plugin
     * @param commandName specifies command name under which to register
     * <code>cliPluginCommandMetadata</code>, supported by <code>supportingPlugin</code>
     * @throws CliRunnerException unsupported attempt detected to re-register
     * the same-named plugin for the same named command
     */
    private void addCommandName(String pluginName, CommandName commandName,
        CliPluginCommandMetadata cliPluginCommandMetadata)
        throws CliRunnerException {

        // ensure that commandName is registered in the command name tree
        mCommandNameTree.addCommandName(commandName);

        // retrieve the map of supporting plugins for this command
        Map<String, CliPluginCommandMetadata> supportingPluginMap
            = mCommandMap.get(commandName);

        // if there is currently no supporting plugins map on file for this
        // command, then create one, and register it for retrieval next time
        if (supportingPluginMap == null) {
            // NOTE: can use HashMap (vs. LinkedHashMap or TreeMap)
            // since we don't care about order of supporting plugins
            supportingPluginMap = new HashMap<String, CliPluginCommandMetadata>();
            mCommandMap.put(commandName, supportingPluginMap);
        }

        // if this command is already implemented by the specified plugin,
        // throw an exception, since we don't support "replacement" of already
        // registered plugins for a command
        if (supportingPluginMap.get(pluginName) != null) {
            throw new CliRunnerException("attempt to add already existing plugin command '"
                + getCanonicalPluginCommandName(pluginName, commandName)
                + "'"
            );
        }

        // register the metadata for the specified supporting plugin
        supportingPluginMap.put(pluginName, cliPluginCommandMetadata);
    }

    /**
     * Unregisters support for the specified command name by the specified
     * plugin.
     * @param pluginName unique identifier for plugin
     * @param commandName specifies command name to remove from database
     */
    private CliPluginCommandMetadata removeCommandName(String pluginName,
            CommandName commandName) {

        // get the map of supporting plugins for this command
        Map<String, CliPluginCommandMetadata> supportingPluginMap
            = mCommandMap.get(commandName);

        // retrieve metadata for the specified plugin for supporting this command
        CliPluginCommandMetadata cliPluginCommandMetadata = null;
        if (supportingPluginMap != null) {
            cliPluginCommandMetadata = supportingPluginMap.get(pluginName);
        }

        // return if the command was not found in the database,
        // or if the plugin specified does not support the command
        if (cliPluginCommandMetadata == null) {
            return null;
        }

        // remove the specified supporting plugin
        supportingPluginMap.remove(pluginName);

        // if this was the only plugin supporting this command,
        // then remove the command altogether from the database
        if (supportingPluginMap.size() == 0) {
            mCommandMap.remove(commandName);
            mCommandNameTree.removeCommandName(commandName);
        }

        // return the plugin support metadata for the command
        return cliPluginCommandMetadata;
    }

    /**
     * @param pluginName unique identifier for plugin
     * @param commandName command name as entered by user
     * @return "canonical" plugin command name (e.g. something the user will understand that
     * what's being referenced is a specific plugin's support for the indicated command)
     */
    private String getCanonicalPluginCommandName(String pluginName, CommandName commandName) {
        return pluginName + ":" + commandName;
    }

}
