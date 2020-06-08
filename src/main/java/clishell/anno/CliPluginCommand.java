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

import clishell.CliMethodForm;

/**
 * @author mross
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CliPluginCommand {

    /** name of command (if goes by only one name) */
    String name() default "";

    /** alternative names of command (if more than one) */
    String[] names() default { };

    /** syntax of command */
    String syntax() default "";

    /** help text for command */
    String[] helptext() default { };

    /**
     * Override for "natural order" of command within plugin
     * May be either numeric (@see {@link Integer#decode(String)})
     * or string.  If numeric, ordering will be upon numeric value,
     * otherwise alphanumeric ordering will be undertaken.  If not
     * specified, user displayable name of command (calculated
     * value) will be used for "natural ordering" of commands.
     */
    String ordering() default "";

    /** format of plugin method called */
    CliMethodForm methodForm() default CliMethodForm.UNKNOWN;

    /** options supported by command (in Unix-"getopt" style notation) */
    String options() default "";

    /** minimum number of arguments expected (negative value indicates "no minimum") */
    int minargs() default -1;

    /** maximum number of arguments expected (negative value indicates "no maximum") */
    int maxargs() default -1;
}
