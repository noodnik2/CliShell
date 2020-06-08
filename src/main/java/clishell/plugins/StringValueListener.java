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

package clishell.plugins;

import java.io.IOException;


/**
 *
 * Interface for Line Event Listeners
 * @see clishell.plugins.LineEventWriter
 *
 */
public interface StringValueListener {

    void stringValueNotification(String line) throws IOException;

}
