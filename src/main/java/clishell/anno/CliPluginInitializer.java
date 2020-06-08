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

package clishell.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Initializer plugin methods are methods that will be called by the CLI
 * immediately upon loading the plugin, in the order specified by the
 * "ordering" attribute.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CliPluginInitializer {

    /**
     * Override for "natural order" of command within plugin
     * May be either numeric (@see {@link Integer#decode(String)})
     * or string.  If numeric, ordering will be upon numeric value,
     * otherwise alphanumeric ordering will be undertaken.
     */
    String ordering() default "";

}
