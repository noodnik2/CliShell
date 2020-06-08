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
 *
 * CLI Plugin Metadata
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CliPlugin {

    String name() default "";           // name of plugin
    String description() default "";    // description of plugin
    String[] helptext() default { };    // help text for plugin
    String version() default "";        // plugin version

}
