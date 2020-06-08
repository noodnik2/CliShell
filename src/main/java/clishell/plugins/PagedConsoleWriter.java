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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;


/**
 *
 * Console Writer wrapper class used to support output pagination
 *
 */
public class PagedConsoleWriter extends Writer {


    //
    //  Private instance variables
    //

    /** the underlying writer */
    private final Writer mWriter;

    /** the number of lines per page */
    private int mLinesPerPage;

    /** the current line output line number */
    private int mCurrentLineNumber;

    /** buffered reader device used for obtaining user's go-ahead to continue */
    private final BufferedReader mInputReader
        = new BufferedReader(new InputStreamReader(System.in));

    /** suppresses all remaining output for current page if this flag is set */
    private boolean mSuppressPageOutput;

    //
    //  Public constructors
    //

    /**
     * @param underlyingWriter writer to which this writer-wrapper will send its output to
     * @param pagesize size of page
     */
    public PagedConsoleWriter(Writer underlyingWriter, int pagesize) {
        mWriter = underlyingWriter;
        mCurrentLineNumber = 0;
        mLinesPerPage = pagesize;
    }


    //
    //  Public instance methods
    //

    /**
     * @return the current line number
     */
    public int getCurrentLineNumber() {
        return mCurrentLineNumber;
    }

    /**
     * Sets the current line number.
     * Note that setting the line number to the value of "0" has a special
     * meaning of "top of page", and resets any output suppression which may
     * have been signaled for the (previous) page.
     * @param currentLineNumber new line number to set
     */
    public void setCurrentLineNumber(int currentLineNumber) {
        if (currentLineNumber == 0) {
            mSuppressPageOutput = false;
        }
        mCurrentLineNumber = currentLineNumber;
    }

    /**
     * @return the linesPerPage
     */
    public int getLinesPerPage() {
        return mLinesPerPage;
    }

    /**
     * @param linesPerPage the linesPerPage to set
     */
    public void setLinesPerPage(int linesPerPage) {
        mLinesPerPage = linesPerPage;
    }


    //
    //  Public implementations / overrides of base class methods
    //

    /* (non-Javadoc)
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
        mWriter.close();
    }

    /* (non-Javadoc)
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        mWriter.flush();
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (mSuppressPageOutput) {
            return;
        }
        int leftOff = off;
        int fence = off + len;
        while(true) {
            int newlineOff = find(cbuf, leftOff, fence, '\n');
            if (newlineOff < 0) {
                mWriter.write(cbuf, leftOff, fence - leftOff);
                break;
            }
            mWriter.write(cbuf, leftOff, newlineOff - leftOff + 1);
            leftOff = newlineOff + 1;
            if ((++mCurrentLineNumber) == mLinesPerPage) {
                mWriter.flush();
                promptUser("--more--");
                mCurrentLineNumber = 0;
            }
        }
    }

    /**
     * @param cbuf character buffer array to search
     * @param off offset to begin search within array
     * @param fence "fence" offset; the first offset beyond the bounds of the array
     * @param ch the character to find within the array
     * @return the offset at which the character was found between (off, fence],
     * or -1 if not found
     */
    private int find(char[] cbuf, int off, int fence, char ch) {
        for (int i = off; i < fence; i++) {
            if (cbuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Optionally prints a prompt string, and waits for user to press Enter.
     * If user enters a word starting with 'q' before pressing enter, then
     * sets the "suppress page output" flag, thereby suppressing all further
     * output which may be generated for the current page.
     * @param prompt prompt to display to user, indicating we're waiting for input
     * @throws IOException
     */
    private void promptUser(String prompt) throws IOException {
        if (prompt != null) {
            mWriter.write(prompt);
        }
        mWriter.flush();
        if (mInputReader.readLine().trim().startsWith("q")) {
            // mark signal if user wants to "quit" the rest of this page
            mSuppressPageOutput = true;
        }
    }
}
