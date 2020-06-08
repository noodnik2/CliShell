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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import clishell.ex.CliInvariantViolationException;

/**
 *
 * Writer maintaining the "tail" (whose size is specified) of the stream of
 * characters written to it, in a buffer that is accessible either by calling
 * the "toString()" method, of by using a <code>java.io.Reader</code> that is
 * available via the special <code>getReader()</code> method.
 *
 */
public class TailCharBufferWriter extends Writer {


    //
    // Private instance data
    //

    /** the character buffer */
    private final char[] mBuffer;

    /** index of tail of buffer */
    private int mTailIndex;

    /** index of head of buffer */
    private int mHeadIndex;

    /** reader for accessing buffer */
    private final Reader mBufferReader;


    //
    //  Public constructors
    //

    /**
     * @param bufferSize size of buffer
     */
    public TailCharBufferWriter(int bufferSize) {
        mBuffer = new char[bufferSize];
        mBufferReader = new BufferReader();
        reset();
    }


    //
    //  Public methods
    //

    /**
     * @see Writer#close()
     */
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * @see Writer#flush()
     */
    public void flush() throws IOException {
        // nothing to do
    }

    /**
     * @see Writer#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write((int) cbuf[off + i]);
        }
    }

    /**
     * @see Writer#write(int)
     */
    public void write(int c) {

        // drop character into tail position in buffer
        mBuffer[mTailIndex] = (char) c;

        // increment tail position to account for character
        incrTailIndex();

        // if tail has "wrapped around" to head, then increment head
        // (i.e., drop oldest character in buffer)
        if (mHeadIndex == mTailIndex) {
            incrHeadIndex();
        }
    }

    /**
     * Resets the buffer so that you can use it again without throwing away the already allocated buffer.
     */
    public void reset() {
        mHeadIndex = 0;
        mTailIndex = 0;
    }

    /**
     * @return string containing all of buffer contents
     * buffer contents are consumed
     */
    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        int len;
        char[] cbuf = new char[4096];
        try {
            while ((len = mBufferReader.read(cbuf)) > 0) {
                stringWriter.write(cbuf, 0, len);
            }
        } catch(IOException ioex) {
            // should never happen, since this reader never throws
            throw new CliInvariantViolationException("I/O error during read of TailCharBufferWriter buffer");
        }
        return stringWriter.toString();
    }

    /**
     * @return buffer reader
     */
    public Reader getReader() {
        return mBufferReader;
    }


    //
    //  Private instance methods
    //

    /**
     * bump head index by one, wrapping as necessary
     */
    private void incrHeadIndex() {
        mHeadIndex++;
        if (mHeadIndex == mBuffer.length) {
            mHeadIndex = 0;
        }
    }

    /**
     * bump head index by one, wrapping as necessary
     */
    private void incrTailIndex() {
        mTailIndex++;
        if (mTailIndex == mBuffer.length) {
            mTailIndex = 0;
        }
    }


    //
    //  Private instance classes
    //

    /**
     *
     */
    private class BufferReader extends Reader {

        /* (non-Javadoc)
         * @see java.io.Reader#close()
         */
        @Override
        public void close() throws IOException {
            // nothing to do
        }

        /* (non-Javadoc)
         * @see java.io.Reader#read(char[], int, int)
         */
        @Override
        public int read(char[] cbuf, int off, int cbufLen) throws IOException {

            // read as many characters as are ready in the buffer,
            // up to the limit specified by the user
            int readLen = 0;
            while(readLen < cbufLen) {
                int c = read();
                if (c < 0) {
                    break;
                }
                cbuf[off + readLen] = (char) c;
                readLen++;
            }

            // NOTE: never return "end of stream" condition;
            // just "no characters available now" (non-blocking read)
            return readLen;
        }

        /* (non-Javadoc)
         * @see java.io.Reader#read()
         */
        @Override
        public int read() throws IOException {

            // if there are no characters ready to be read, then
            // return "end of stream" condition
            if (mHeadIndex == mTailIndex) {
                return -1;
            }

            // get (oldest) character at the head of the buffer
            int cReturn = (int) mBuffer[mHeadIndex];

            // account for having read it - remove it from buffer
            incrHeadIndex();

            // return the characters
            return cReturn;
        }

    }

}
