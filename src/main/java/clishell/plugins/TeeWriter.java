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
 * Inspired by the Unix "tee" command, this Writer
 * will send everything written to it to any number
 * of other writers that are registered with it.
 *
 * @author mross
 *
 */
public class TeeWriter extends Writer {


    //
    //  Private instance variables
    //

    /** collection of underlying writers */
    private final Collection<WriterInfo> mWriters
        = new LinkedList<WriterInfo>();


    //
    //  Public class classes
    //

    /**
     * Internal class to manage information about an underlying writer
     */
    public static class WriterInfo {

        /** true iff we "own" this writer */
        private final boolean mOwned;

        /** underlying writer */
        private final Writer mWriter;

        /**
         * @param writer underlying writer
         * @param owned true iff we "own" this writer
         */
        public WriterInfo(boolean owned, Writer writer) {
            mWriter = writer;
            mOwned = owned;
        }

        /**
         * @return flag indicating that we "own" the underlying
         * writer if true
         */
        public boolean getOwned() {
            return mOwned;
        }

        /**
         * @return underlying writer instance
         */
        public Writer getWriter() {
            return mWriter;
        }

    }


    //
    //  Public constructors
    //

    /**
     * Default constructor adds no underlying writers
     */
    public TeeWriter() {
        ;       // nothing to do
    }

    /**
     * Calls {@link #TeeWriter(boolean, Writer...)}, passing
     * <code>true</code> as the value of the <code>owned</code>
     * parameter, meaning these underlying writer(s) will be
     * "owned" by the created instance of <code>TeeWriter</code>.
     */
    public TeeWriter(Writer... writers) {
        this(true, writers);
    }

    /**
     * @param writer1 a writer to add into the newly created <code>TeeWriter</code>
     * @param owned1 same as in {@link #addWriter(boolean, Writer...)}
     * for <code>writer1</code>
     * @param writer2 a second writer to add into the newly created
     * <code>TeeWriter</code>
     * @param owned2 same as in {@link #addWriter(boolean, Writer...)}
     * for <code>writer2</code>
     */
    public TeeWriter(boolean owned1, Writer writer1,
                     boolean owned2, Writer writer2) {
        addWriter(owned1, writer1);
        addWriter(owned2, writer2);
    }

    /**
     * @param writers zero or more underlying writer(s)
     * to initialize this <code>TeeWriter</code> instance with;
     * the writer(s) will not be "owned" by this instance,
     * therefore will not be closed when {@link #close()}
     * is called.
     * @param owned same as in {@link #addWriter(boolean, Writer...)}
     */
    public TeeWriter(boolean owned, Writer... writers) {
        addWriter(owned, writers);
    }

    /**
     * @param writerInfos zero or more underlying writer(s)
     * to initialize this <code>TeeWriter</code> instance with;
     * the writer(s) will not be "owned" by this instance,
     * therefore will not be closed when {@link #close()}
     * is called.
     */
    public TeeWriter(WriterInfo... writerInfos) {
        addWriter(writerInfos);
    }


    //
    //  Public instance methods
    //

    /**
     * @param owned true if this <code>TeeWriter</code> instance is to
     * "own" this underlying writer, meaning <code>underlyingWriter</code>
     * will be closed when {@link #close()} is called.
     * @param writers underlying writer to add to this
     * instance of <code>TeeWriter</code>
     */
    public void addWriter(boolean owned, Writer... writers) {
        for (Writer writer : writers) {
            addWriter(new WriterInfo(owned, writer));
        }
    }

    /**
     * @param writerInfos zero or more objects containing information
     * about writer(s) to add to this instance of <code>TeeWriter</code>
     */
    public void addWriter(WriterInfo... writerInfos) {
        for (WriterInfo writerInfo : writerInfos) {
            mWriters.add(writerInfo);
        }
    }

    /**
     * @param writers zero or more writer(s) previously added
     * using {@link #addWriter(boolean, Writer...)}
     */
    public void removeWriter(Writer... writers) {

        // loop over all underlying writers we're managing now
        for (Iterator<WriterInfo> i = mWriters.iterator(); i.hasNext();) {
            WriterInfo currentWriterInfo = i.next();
            Writer currentWriter = currentWriterInfo.getWriter();
            // loop over all writers user wants to remove
            for (Writer writer : writers) {
                // if one of the user's writers is managed by us now,
                if (writer == currentWriter) {
                    // remove it from our management
                    i.remove();
                    // go onto considering our next writer
                    break;
                }
            }
        }

    }


    //
    //  Public implementations / overrides of base class methods
    //

    /**
     * Closes all "owned" underlying writers, and flushes the ones
     * that are now "owned".
     * @throws IOException the last of any <code>IOException</code>s
     * which may have been thrown during the process of looping across
     * all of the underlying writers and closing or flushing them
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
        IOException ioException = null;
        for (WriterInfo writerInfo : mWriters) {
            try {
                Writer writer = writerInfo.getWriter();
                if (writerInfo.getOwned()) {
                    writer.close();
                } else {
                    writer.flush();
                }
            } catch(IOException ioex) {
                ioException = ioex;
            }
        }
        if (ioException != null) {
            throw ioException;
        }
    }

    /**
     * Flushes underlying writers.
     * @throws IOException the last of any <code>IOException</code>s
     * which may have been thrown during the process of looping across
     * all of the underlying writers and flushing them
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        IOException ioException = null;
        for (WriterInfo writerInfo : mWriters) {
            try {
                writerInfo.getWriter().flush();
            } catch(IOException ioex) {
                ioException = ioex;
            }
        }
        if (ioException != null) {
            throw ioException;
        }
    }

    /**
     * Writes data to all underlying writers.
     * @throws IOException the last of any <code>IOException</code>s
     * which may have been thrown during the process of looping across
     * all of the underlying writers and writing to them
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

        IOException ioException = null;
        for (WriterInfo writerInfo : mWriters) {
            try {
                writerInfo.getWriter().write(cbuf, off, len);
            } catch(IOException ioex) {
                ioException = ioex;
            }
        }
        if (ioException != null) {
            throw ioException;
        }

    }

}
