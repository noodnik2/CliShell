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

import java.util.Collection;

import clishell.anno.CliPlugin;

/**
 *
 *  Class to store & manage metadata about a loaded CLI Plugin instance
 *
 */
public class CliPluginMetadata implements NamedPlugin {


    //
    // Private instance data
    //

    /** instance of "plugin" object described by this metadata */
    private Object mPluginInstance;

    /**
     * Collection of "initializers" - method(s) to be called after the plugin
     * is loaded but before any end-features of the plugin can be accessed
     */
    private Collection<CliPluginMethodMetadata> mInitializers;

    /**
     * Collection of "finalizers" - method(s) to be called before the plugin
     * can be unloaded
     */
    private Collection<CliPluginMethodMetadata> mFinalizers;

    /**
     * Collection of "main" - method(s) to be called after all initial plugins
     * have been loaded and initialized
     */
    private Collection<CliPluginMethodMetadata> mMains;

    /** Collection of CLI commands loaded by this plugin */
    private Collection<CliPluginCommandMetadata> mCommands;

    /** Name by which the CLI and the user will refer to this plugin */
    private String mName;


    //
    // Public methods
    //

    /**
     * @return name by which the plugin can be referenced by the user
     */
    public String getName() {
        if (mName != null) {
            return mName;
        }
        return getShortName();
    }

    /**
     * @return full name of plugin (returns class name of loaded plugin instance)
     */
    public String getFullName() {
        return mPluginInstance.getClass().getName();
    }

    /**
     * @return short name of plugin (returns either what user specified as
     * "name" of the plugin, or the last part of the full name)
     */
    public String getShortName() {

        CliPlugin cliPlugin = mPluginInstance.getClass()
            .getAnnotation(CliPlugin.class);

        if (!"".equals(cliPlugin.name())) {
            return cliPlugin.name();
        }
        return mPluginInstance.getClass().getSimpleName();
    }

    /**
     * @return the initializers
     */
    public Collection<CliPluginMethodMetadata> getInitializers() {
        return mInitializers;
    }

    /**
     * @return the finalizers
     */
    public Collection<CliPluginMethodMetadata> getFinalizers() {
        return mFinalizers;
    }

    /**
     * @return the mains
     */
    public Collection<CliPluginMethodMetadata> getMains() {
        return mMains;
    }

    /**
     * @return the cliPluginInstance
     */
    public Object getPluginInstance() {
        return mPluginInstance;
    }

    /**
     * @return the commands
     */
    public Collection<CliPluginCommandMetadata> getCommands() {
        return mCommands;
    }


    //
    //  Protected instance methods
    //

    /**
     * @param name the name to set
     */
    protected void setName(String name) {
        mName = name;
    }

    /**
     * @param pluginInstance the plugin instance to set
     */
    protected void setPluginInstance(Object pluginInstance) {
        mPluginInstance = pluginInstance;
    }

    /**
     * @param commands the commands to set
     */
    protected void setCommands(Collection<CliPluginCommandMetadata> commands) {
        mCommands = commands;
    }

    /**
     * @param finalizers the finalizers to set
     */
    protected void setFinalizers(Collection<CliPluginMethodMetadata> finalizers) {
        mFinalizers = finalizers;
    }

    /**
     * @param mains the mains to set
     */
    protected void setMains(Collection<CliPluginMethodMetadata> mains) {
        mMains = mains;
    }

    /**
     * @param initializers the initializers to set
     */
    protected void setInitializers(Collection<CliPluginMethodMetadata> initializers) {
        mInitializers = initializers;
    }

}
