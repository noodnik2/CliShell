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

import java.io.Reader;


/**
 *
 *
 */
public class CliCommandParser extends StringListParser {

    /**
     * @param reader
     * @see StringListParser#StringListParser(Reader)
     */
    public CliCommandParser(Reader reader) {
        super(reader);
    }

}
