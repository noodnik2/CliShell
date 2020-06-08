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
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


/**
 *
 * Writer wrapper class used to support event-based
 * gathering of output lines.
 *
 * @author mross
 *
 */
public class LineEventWriter extends Writer {


    //
    //  Private instance variables
    //

    /** buffer to hold contents of current line */
    private final StringBuffer mLineBuffer = new StringBuffer();

    /** collection of line listeners */
    private final Collection<StringValueListener> mListeners = new LinkedList<StringValueListener>();

    /** this is the sequence of characters which we will use to delineate "lines" */
    private final char[] mEndOfLineSequence;


    //
    //  Public constructors
    //

    /**
     * Initialize <code>LineEventWriter</code> with single newline as end of line sequence.
     */
    public LineEventWriter() {
        mEndOfLineSequence = new char[] {'\n' };
    }

    /**
     * @param eolSequence end of line sequence used to delineate lines
     */
    public LineEventWriter(char[] eolSequence) {
        mEndOfLineSequence = eolSequence.clone();
    }


    //
    //  Public instance methods
    //

    /**
     * @param stringValueListener listener to be notified when lines
     * arrive on output stream
     */
    public void addListener(StringValueListener stringValueListener) {
        mListeners.add(stringValueListener);
    }

    /**
     * @param stringValueListener listener to be notified when
     * lines arrive on output stream
     * @return object that was removed, or <code>null</code> if
     * could not find an object to remove
     */
    public StringValueListener removeListener(StringValueListener stringValueListener)
    {
        for (Iterator<StringValueListener> i = mListeners.iterator(); i.hasNext();) {
            StringValueListener currentStringValueListener = i.next();
            if (stringValueListener == currentStringValueListener) {
                i.remove();
                return currentStringValueListener;
            }
        }
        return null;
    }


    //
    //  Public implementations / overrides of base class methods
    //

    /**
     * There is no underlying writer for this class,
     * so this is a no-operation.
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
        ;   // no underlying writer to close
    }

    /**
     * There is no underlying writer for this class,
     * so this is a no-operation.
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        ;   // no underlying writer to flush
    }

    /**
     * We buffer data written to this writer until we receive the
     * end of line sequence, then push completed "lines" to the
     * various listeners which we have.
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

        int leftOff = off;
        int fence = off + len;

        // loop over data, processing "lines" from it
        while(true) {

            // look for end of line sequence
            int newlineOff = findEndOfLineOffset(cbuf, leftOff, fence,
                    mEndOfLineSequence);

            // if newline not found, take rest of line and exit loop
            if (newlineOff < 0) {
                mLineBuffer.append(cbuf, leftOff, fence - leftOff);
                break;
            }

            // end of line was found; take up to end of line sequence,
            // and push current line to all listeners
            mLineBuffer.append(cbuf, leftOff, fence - leftOff);
            pushStringValueToListeners(mLineBuffer.toString());
            mLineBuffer.setLength(0);

            // bump our current pointed in cbuf past the end of line sequence
            leftOff = newlineOff + mEndOfLineSequence.length;
        }

    }


    //
    //  Private instance methods
    //

    /**
     * @param stringValue string value to push to listeners
     * @throws IOException unhandled exception while delivering
     * <code>stringValue</code> to one of the listeners
     * NOTE: <code>stringValue</code> may not be delivered to some
     * listener(s) when this exception is thrown.
     */
    private void pushStringValueToListeners(String stringValue) throws IOException {
        for (StringValueListener lineEventListener : mListeners) {
            lineEventListener.stringValueNotification(stringValue);
        }
    }

    /**
     * @param cbuf character buffer array to search
     * @param off offset to begin search within array
     * @param fence "fence" offset; the first offset beyond the bounds of the array
     * @param eolSeq end of line sequence
     * @return the offset of the end of line sequence within <code>cbuf</code>
     * between (off, fence], or -1 if not found
     */
    private int findEndOfLineOffset(char[] cbuf, int off, int fence, char[] eolSeq) {
        int lSeq = eolSeq.length;
        int newFence = fence - lSeq + 1;
        for (int cBufIndex = off; cBufIndex < newFence; cBufIndex++) {
            int eolSeqIndex;
            for (eolSeqIndex = 0; eolSeqIndex < lSeq; eolSeqIndex++) {
                if (cbuf[cBufIndex + eolSeqIndex] != eolSeq[eolSeqIndex]) {
                    break;
                }
            }
            if (eolSeqIndex == lSeq) {
                return cBufIndex;
            }
        }
        return -1;
    }

}
