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

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class StringListParser {


    //
    //  Private instance data
    //

    /** */
    private StreamTokenizer mStreamTokenizer;


    //
    //  Public instance methods
    //

    /**
     * Construct string list parser that parses tokens on specified characters
     * @param reader reader to character stream to parse tokens from
     * @param delimiters specified parsing characters
     */
    public StringListParser(Reader reader, char[] delimiters) {
        commonStreamTokenizerInitialization(reader);
        for (char delimiter : delimiters) {
            mStreamTokenizer.whitespaceChars((int) delimiter, (int) delimiter);
        }
    }

    /**
     * Construct string list parser that parses tokens on whitespace
     * @param reader reader to character stream to parse tokens from
     */
    public StringListParser(Reader reader) {
        commonStreamTokenizerInitialization(reader);
        mStreamTokenizer.whitespaceChars(0, ' ');
    }

    /**
     * @return string array containing the next set of parsed tokens read from
     * the parser up until either and end-of-line, or the end-of-file condition
     * is raised for the parser's input stream; never returns <code>null</code>
     */
    public String[] readLineTokens() {

        List<String> commandList = new LinkedList<String>();
        try {
            int ttype;
            while((ttype = mStreamTokenizer.nextToken()) != StreamTokenizer.TT_EOL) {
                if (ttype == StreamTokenizer.TT_EOF) {
                    break;
                }
                if (
                    (ttype == StreamTokenizer.TT_WORD)
                        || (ttype == '"')
                        || (ttype == '\'')
                ) {
                    commandList.add(mStreamTokenizer.sval);
                    continue;
                }
            }
        } catch(IOException ioex) {
            // should never happen since parsing from String (er??)
            throw new RuntimeException("I/O error while tokenizing input command line", ioex);
        }

        return commandList.toArray(new String[commandList.size()]);
    }

    /**
     * @param inputLine string containing input line to parse
     * @param delimiters additional set of characters to act as delimiters
     * separating tokens
     * @return parsed tokens from input line, or <code>null</code> if no
     * word tokens were encountered in input line
     */
    public static String[] parseTokens(String inputLine, char[] delimiters) {
        return new StringListParser(new StringReader(inputLine), delimiters).readLineTokens();
    }

    /**
     * @param inputLine string containing input line to parse
     * @return parsed tokens from input line, or <code>null</code> if no
     * word tokens were encountered in input line
     */
    public static String[] parseTokens(String inputLine) {
        return new StringListParser(new StringReader(inputLine)).readLineTokens();
    }


    //
    //  Private instance methods
    //

    /**
     * Construct and initialize <code>mStreamTokenizer</code> instance object
     * @param reader reader to character stream to parse tokens from
     */
    private void commonStreamTokenizerInitialization(Reader reader) {

        mStreamTokenizer = new StreamTokenizer(reader);

        // reset tokenizer: set all characters as "ordinary"
        mStreamTokenizer.resetSyntax();

        // set custom, yet simple tokenizer configuration
        mStreamTokenizer.wordChars('!', '~');
        mStreamTokenizer.wordChars(128 + 32, 255);
        mStreamTokenizer.quoteChar('"');
        mStreamTokenizer.quoteChar('\'');
        mStreamTokenizer.slashStarComments(false);
        mStreamTokenizer.slashSlashComments(false);
        mStreamTokenizer.eolIsSignificant(true);

    }


}
