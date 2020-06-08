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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.anno.CliPluginInitializer;
import clishell.anno.CliPluginMain;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;

/**
 *
 * Loads and unloads CLI plugins
 *
 */
public class CliPluginLoader {


    //
    // Public instance methods
    //

    /**
     * @param pluginClassName string name specifying a plugin to be loaded
     * as of this writing, this is the fully-qualified class name of the plugin
     * class to be loaded; class must be in java's CLASSPATH
     * @param pluginNameOverride user override of the plugin's "name" (i.e., the
     * value to be returned by the "getName()" method from the plugin metadata),
     * will be <code>null</code> if no override given
     * @return cliPluginMetadata metadata for the CLI plugin that was loaded
     * @throws CliRunnerException unhandled exception thrown while attempting to load plugin
     */
    public CliPluginMetadata loadFromClassName(String pluginClassName,
            String pluginNameOverride) throws CliRunnerException {

        Class<?> pluginClass;
        try {
            pluginClass = Class.forName(pluginClassName);
        } catch(ClassNotFoundException cnfe) {
            throw new CliRunnerException("plugin class not found in classpath: '"
                + pluginClassName + "'", cnfe);
        }

        Object pluginInstance;
        try {
            pluginInstance = pluginClass.newInstance();
        } catch(InstantiationException iae) {
            throw new CliRunnerException("plugin class could not be instantiated: '"
                + pluginClassName + "'", iae);
        } catch(IllegalAccessException iae) {
            throw new CliRunnerException("plugin class definition could not be accessed: '"
                + pluginClassName + "'", iae);
        }

        return loadFromInstance(pluginInstance, pluginNameOverride);
    }

    /**
     * @param pluginInstance object instance for whom
     * <code>CliPluginMetadata</code> is being requested
     * @param pluginNameOverride user override of the plugin's "name" (i.e., the
     * value to be returned by the "getName()" method from the plugin metadata),
     * will be <code>null</code> if no override given
     * @return CliPluginMetadata metadata regarding loaded CLI plugin
     * @throws CliRunnerException thrown if could not load
     * <code>CliPluginMetadata</code> for specified <code>pluginInstance</code>
     * for any reason
     */
    public CliPluginMetadata loadFromInstance(Object pluginInstance,
            String pluginNameOverride)
        throws CliRunnerException {

        CliPlugin pluginAnnotation = pluginInstance.getClass()
            .getAnnotation(CliPlugin.class);

        if (pluginAnnotation == null) {
            throw new CliRunnerException("can't load unrecognized plugin (class '"
                + pluginInstance.getClass().getName()
                + "'; missing '@"
                + CliPlugin.class.getSimpleName()
                + "' annotation"
            );
        }


        // use "TreeSet" in order to maintain plugin commands in "natural order"
        // (i.e., according to their "compareTo()" method)
        Set<CliPluginMethodMetadata> cliInitializers = new TreeSet<CliPluginMethodMetadata>();
        Set<CliPluginMethodMetadata> cliFinalizers = new TreeSet<CliPluginMethodMetadata>();
        Set<CliPluginMethodMetadata> cliMains = new TreeSet<CliPluginMethodMetadata>();
        Set<CliPluginCommandMetadata> cliCommands = new TreeSet<CliPluginCommandMetadata>();

        // loop over all methods in the plugin instance
        for (Method method : pluginInstance.getClass().getMethods()) {

            // build a "CliMethod" object out of each
            CliMethod cliMethod = new CliMethod(method);

            // loop over all method annotations
            for (Annotation annotation : method.getAnnotations()) {

                // if method is a CliPluginCommand, add it to the list of commands
                if (annotation instanceof CliPluginCommand) {
                    // verify that we support the method's signature
                    assertMethodFormIn(cliMethod, CliMethod.SUPPORTED_CLIMETHODFORMS);
                    CliPluginCommandMetadata cliPluginCommandMetadata
                        = new CliPluginCommandMetadata();
                    loadPluginCommandMetadata(cliPluginCommandMetadata, cliMethod);
                    cliCommands.add(cliPluginCommandMetadata);
                    continue;
                }

                // if method is a CliPluginInitializer, add it to the list of initializers
                if (annotation instanceof CliPluginInitializer) {
                    assertMethodFormIn(cliMethod,
                        new CliMethodForm[] {CliMethodForm.VOID_NOPARAM });
                    CliPluginMethodMetadata cliPluginMethodMetadata
                        = new CliPluginMethodMetadata();
                    loadPluginMethodMetadata(cliPluginMethodMetadata, cliMethod,
                        ((CliPluginInitializer) annotation).ordering());
                    cliInitializers.add(cliPluginMethodMetadata);
                    continue;
                }

                // if method is a CliPluginMain, add it to the list of mains
                if (annotation instanceof CliPluginMain) {
                    assertMethodFormIn(cliMethod,
                        new CliMethodForm[] {CliMethodForm.VOID_NOPARAM });
                    CliPluginMethodMetadata cliPluginMethodMetadata
                        = new CliPluginMethodMetadata();
                    loadPluginMethodMetadata(cliPluginMethodMetadata, cliMethod,
                        ((CliPluginMain) annotation).ordering());
                    cliMains.add(cliPluginMethodMetadata);
                    continue;
                }

                // if method is a CliPluginFinalizer, add it to the list of finalizers
                if (annotation instanceof CliPluginFinalizer) {
                    assertMethodFormIn(cliMethod,
                        new CliMethodForm[] {CliMethodForm.VOID_NOPARAM });
                    CliPluginMethodMetadata cliPluginMethodMetadata
                        = new CliPluginMethodMetadata();
                    loadPluginMethodMetadata(cliPluginMethodMetadata, cliMethod,
                        ((CliPluginFinalizer) annotation).ordering());
                    cliFinalizers.add(cliPluginMethodMetadata);
                    continue;
                }

                // ignore any unknown annotations
            }
        }

        CliPluginMetadata cliPluginMetadata = new CliPluginMetadata();

        cliPluginMetadata.setPluginInstance(pluginInstance);

        cliPluginMetadata.setCommands(cliCommands);
        cliPluginMetadata.setInitializers(cliInitializers);
        cliPluginMetadata.setMains(cliMains);
        cliPluginMetadata.setFinalizers(cliFinalizers);

        if (pluginNameOverride != null) {
            cliPluginMetadata.setName(pluginNameOverride);
        }

        return cliPluginMetadata;

    }

    /**
     * @param cliPluginMetadata metadata for CLI plugin to initialize
     * @throws CliRunnerException unhandled exception thrown by any of
     * the initialization methods of the plugin; NOTE: if thrown, some
     * initialization methods may not get invoked
     */
    public void initializePlugin(CliPluginMetadata cliPluginMetadata)
        throws CliRunnerException {

        // retrieve plugin object instance
        Object pluginInstance = cliPluginMetadata.getPluginInstance();

//        System.out.println("initializing plugin: '"
//                + cliPluginMetadata.getName()
//                + "' from class: '"
//                + pluginInstance.getClass().getName()
//                + "'");

        // loop over and invoke all initialization methods found in plugin
        // NOTE: if any throw an exception, the others will not be invoked...
        for (CliPluginMethodMetadata cliPluginMethodMetadata
                : cliPluginMetadata.getInitializers()) {
            cliPluginMethodMetadata.getCliMethod().invoke(pluginInstance);
        }

    }

    /**
     * Calls the "finalizer" method(s) of thespecified CLI plugin
     * @param cliPluginMetadata metadata for CLI plugin to finalize
     * @throws CliRunnerException unhandled exception thrown by any of
     * the finalization methods of the plugin; NOTE: if thrown, some
     * finalization methods may not get invoked
     */
    public void finalizePlugin(CliPluginMetadata cliPluginMetadata)
        throws CliRunnerException {

//        System.out.println("finalizing plugin: '" + cliPluginMetadata.getName() + "'");

        Object pluginInstance = cliPluginMetadata.getPluginInstance();
        if (pluginInstance != null) {
            if (cliPluginMetadata.getFinalizers() != null) {
                for (CliPluginMethodMetadata cliPluginMethodMetadata
                        : cliPluginMetadata.getFinalizers()) {
                    cliPluginMethodMetadata.getCliMethod().invoke(pluginInstance);
                }
            }
        }

    }


    //
    // Private instance methods
    //

    /**
     * @param methodMetadata <code>CliPluginMethodMetadata</code> instance to load
     * @param cliMethod method to load into <code>methodMetadata</code>
     * @param ordering ordering to load into <code>methodMetadata</code>
     */
    private void loadPluginMethodMetadata(CliPluginMethodMetadata methodMetadata,
            CliMethod cliMethod, String ordering) {

        methodMetadata.setCliMethod(cliMethod);

        // set natural order override, if specified
        if (!"".equals(ordering.trim())) {
            methodMetadata.setNaturalOrderOverride(ordering.trim());
        }
    }

    /**
     * @param commandMetadata <code>CliPluginCommandMetadata</code> instance to load
     * @param cliMethod method to load into <code>commandMetadata</code>
     * @throws CliRejectedInputException invalid annotation metadata detected
     */
    private void loadPluginCommandMetadata(CliPluginCommandMetadata commandMetadata,
            CliMethod cliMethod) throws CliRejectedInputException {

        // get the java method, and its @CliPluginCommand annotation
        Method method = cliMethod.getMethod();
        CliPluginCommand commandAnnotation = method
            .getAnnotation(CliPluginCommand.class);

        // load the base metadata
        loadPluginMethodMetadata(commandMetadata, cliMethod,
            commandAnnotation.ordering());

        // create an options parser for the command, configured with
        // user-specified valid options set for the command
        if (!"".equals(commandAnnotation.options().trim())) {
            commandMetadata.setOptionParser(new CliOptionParser(
                commandAnnotation.options().trim()));
        }

        // create a set of names for the command, preserving the order
        // in which the names were declared
        Set<CommandName> names = new LinkedHashSet<CommandName>();

        // if a "name" annotation attribute is given, assign it as the
        // first name in the set of names by which the command is known
        if (!"".equals(commandAnnotation.name().trim())) {
            names.add(new CommandName(CliCommandParser.parseTokens(
                commandAnnotation.name().trim())));
        }

        // assign the "other" names that this command is known by
        for (String name : commandAnnotation.names()) {
            if (!"".equals(name.trim())) {
                names.add(new CommandName(CliCommandParser.parseTokens(name.trim())));
            }
        }

        // if there are still no known names for this command,
        // then assign the natural name of the method
        if (names.size() == 0) {
            names.add(new CommandName(CliCommandParser.parseTokens(method.getName())));
        }

        // record the set of names by which the command will be known
        commandMetadata.setNames(names);

        // Load number of arguments accepted for this command
        // if the method knows how many parameters it requires, then make sure that
        // what the user said in the annotation matches; otherwise, throw an error
        int maxArgs = commandAnnotation.maxargs();
        int minArgs = commandAnnotation.minargs();

        // if the method knows about the required number of parameters for itself,
        if (cliMethod.getNumUserParameters() >= 0) {
            // then any user-specified min / max must be less than or equal to that
            // number (the CLI code will pass "null" values for the argument(s) that
            // are not specified by the user)
            if (
                    ((maxArgs >= 0) && (maxArgs > cliMethod.getNumUserParameters()))
                    || ((minArgs >= 0) && (minArgs > cliMethod.getNumUserParameters()))) {
                StringBuffer errorMessageBuffer = new StringBuffer();
                errorMessageBuffer.append("@CliPluginCommand attribute(s) incompatible with actual java method");
                if (commandAnnotation.minargs() > 0) {
                    errorMessageBuffer.append(", minargs=" + commandAnnotation.minargs());
                }
                if (commandAnnotation.maxargs() > 0) {
                    errorMessageBuffer.append(", maxargs=" + commandAnnotation.maxargs());
                }
                errorMessageBuffer.append(", # actual method args=" + cliMethod.getNumUserParameters());
                errorMessageBuffer.append(": '" + cliMethod.getMethodSignature() + "'");
                throw new CliRejectedInputException(errorMessageBuffer.toString());
            }

            // make sure that we set the max/min to a reasonable
            // value, given the method's requirement
            if (minArgs < 0) {
                minArgs = cliMethod.getNumUserParameters();
            }
            if (maxArgs < 0) {
                maxArgs = cliMethod.getNumUserParameters();
            }
        }

        if ((maxArgs >= 0) && (minArgs >= 0) && (maxArgs < minArgs)) {
            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer.append("@CliPluginCommand attributes inconsistent; 'minargs' > 'maxargs'");
            errorMessageBuffer.append(" (" + minArgs + " > " + maxArgs + ")");
            errorMessageBuffer.append(": '" + cliMethod.getMethodSignature() + "'");
            throw new CliRejectedInputException(errorMessageBuffer.toString());
        }

        commandMetadata.setMaxArgs(maxArgs);
        commandMetadata.setMinArgs(minArgs);

    }

    /**
     * @param cliMethod cliMethod whose <code>CliMethodForm</code> is being checked
     * @param validMethodForms array of valid <code>CliMethodForm</code>
     * @throws CliRunnerException if method's form is not contained in
     * <code>validMethodForms</code>
     */
    private void assertMethodFormIn(CliMethod cliMethod,
        CliMethodForm[] validMethodForms) throws CliRunnerException {

        CliMethodForm cliMethodForm = cliMethod.getCliMethodForm();

        for (CliMethodForm validMethodForm : validMethodForms) {
            if (cliMethodForm == validMethodForm) {
                return;
            }
        }

        throw new CliRunnerException("unsupported method form (signature) for '"
            + cliMethod.getMethodSignature()
            + "' ("
            + cliMethodForm
            + ")");
    }

}
