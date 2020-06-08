package clishell.plugins;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class SysCommandInvoker {


    //
    //  Private instance data
    //

    /** flag that can be set in order to abort "forever" waits */
    private boolean mAbortWaitFlag;


    //
    //  Public class methods
    //

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // create an invoker to execute the command
        SysCommandInvoker syscmdInvoker = new SysCommandInvoker();

        // if no arguments specified by user,
        // try to find the shell command in use
        if (args.length == 0) {
            String shellCommandName = syscmdInvoker.getShellCommand();
            if (shellCommandName == null) {
                throw new IOException("cannot find system shell command");
            }
            args = new String[] {shellCommandName };
        }

        System.out.println(Arrays.toString(args));

        int rc = syscmdInvoker.system(args);

        System.out.println("R(" + rc + ");");

    }


    //
    //  Public instance methods
    //

    /**
     * @return shell command name - this <i>should</i> (though
     * if improperly configured, cannot be guaranteed to) be a
     * valid command to pass as the first argument to the
     * {@link #system(OutputStream, OutputStream, InputStream, String...)}
     * method if you want to invoke the system's currently
     * defined command shell; will be <code>null</code> if shell
     * command name cannot be determined for current environment
     */
    public String getShellCommand() {

        String osName = System.getProperty("os.name");

        if (osName == null) {
            // can't figure out shell if we don't know
            // which O/S we're running under
            return null;
        }

        // find the name of the environment variable that
        // specifies the shell
        String[] knownPreferredShellVarSpecs = {
            "windows|ComSpec"
        ,   "linux|SHELL"
        ,
        };
        String shellVarName = null;
        String osNameLower = osName.toLowerCase();
        for (String knownPreferredShellVarSpec : knownPreferredShellVarSpecs) {
            String[] osAndShellVarName = knownPreferredShellVarSpec.split("\\|");
            if (osNameLower.indexOf(osAndShellVarName[0].toLowerCase()) >= 0) {
                shellVarName = osAndShellVarName[1];
                break;
            }
        }

        if (shellVarName == null) {
            // can't figure out which shell if we don't know
            // the name of the SHELL environment variable
            // in this O/S specifying the shell command name
            return null;
        }

        String shellName = System.getenv(shellVarName);

        // can only do it if the environment variable has a value
        if (shellName == null) {
            // can't figure out which shell if we don't know
            // the shell command name
            return null;
        }

        return shellName;

    }

    /**
     * Sets the flag that will cause "waits" done by the
     * {@link #system(OutputStream, OutputStream, InputStream, String...)}
     * call (and its derivatives) to abort.
     */
    public void abortWait() {
        mAbortWaitFlag = true;
    }

    /**
     * Same as {@link #system(OutputStream, OutputStream, InputStream, String...)}
     * but passes <code>null</code>s for selecting default streams for stdout,
     * stderr and stdin
     * @see #system(OutputStream, OutputStream, InputStream, String...)
     */
    public int system(String... args) throws IOException {
        return system(null, null, null, args);
    }

    /**
     * Execute system command; note this command will block until the
     * specified system command / process (and any children it creates)
     * has (have) terminated.
     * @param out output stream to which processes "stdout" will be routed;
     * if <code>null</code> is passed, will use <code>System.out</code>
     * @param err output stream to which processes "stderr" will be routed
     * if <code>null</code> is passed, will use <code>System.err</code>
     * @param in input stream from which processes "stdin" will be routed
     * if <code>null</code> is passed, will use <code>System.in</code>
     * @param args array of system command & its arguments (must not be
     * <code>null</code> and length of this array must be greater than zero)
     * @return return code from system command
     * @throws IOException unhandled exception generated while executing
     * method, or invalid argument(s) were passed to it
     * @throws NullPointerException <code>args</code> parameter passed
     * was <code>null</code> or contained <code>null</code> value(s)
     */
    public int system(OutputStream out, OutputStream err, InputStream in,
            String... args) throws IOException {

        // validate user arguments passed...

        if (args == null) {
            throw new NullPointerException("null args parameter");
        }

        if (args.length == 0) {
            throw new IOException("empty args parameter");
        }

        for (String arg : args) {
            if (arg == null) {
                throw new NullPointerException("null args entry");
            }
        }

        // reset the wait flag so that we will do
        // waits - forever if necessary - to achieve our goals
        mAbortWaitFlag = false;

        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = processBuilder.start();

        Thread errThread = newPipeThread(process.getErrorStream(),
                (err != null) ? err : System.err);

        Thread outThread = newPipeThread(process.getInputStream(),
                (out != null) ? out : System.out);

        InputStream consoleInput = new NonCloseableInputStream(
                (in != null) ? in : System.in);

        Thread inThread = newPipeThread(consoleInput, process.getOutputStream());

        // look at the loop logic below; this initial
        // value will never be returned
        int rc = -1;

        // wait for process to finish
        for (boolean waiting = true; waiting && !mAbortWaitFlag;) {
            try {
                rc = process.waitFor();
                waiting = false;
            } catch(InterruptedException ie) {
                ;   // nothing to do here
            }
        }

        // "close" the stdin filter
        consoleInput.close();

        // wait for the I/O threads to terminate
        waitForThreadToDie(outThread);
        waitForThreadToDie(errThread);
        waitForThreadToDie(inThread);

        // notify the user if any of the threads are still alive
        if (outThread.isAlive() || errThread.isAlive() || inThread.isAlive()) {
            new PrintStream(err).println("WARNING: "
                + getClass().getName()
                + ".system() aborted wait; thread(s) remain");
        }

        // return process return code to caller
        return rc;
    }


    //
    //  Private class methods
    //

    /**
     * Wait (forever if necessary unless the <code>mAbortWaitFlag</code>
     * has been set) for the thread indicated by <code>thread</code> to die.
     * @param thread thread to wait for its termination
     */
    private void waitForThreadToDie(Thread thread) {
        while(thread.isAlive() && !mAbortWaitFlag) {
            try {
                thread.join();
            } catch(InterruptedException ie) {
                ;   // no action required, just continue to loop
            }
        }
    }

    /**
     * Creates and starts a thread that will copy the input stream specified by
     * <code>src</code> to the output stream specified by <code>dest</code>.
     * The thread will terminate when either: (1) the input stream reaches an
     * "end of stream" condition; or, (2) there is an I/O error reading from
     * the input stream or writing to the output stream.
     * @param src
     * @param dest
     * @return
     */
    private Thread newPipeThread(final InputStream src, final OutputStream dest) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    int read;
                    while((read = src.read(buffer)) >= 0) {
                        dest.write(buffer, 0, read);
                        dest.flush();
                    }
                } catch (IOException e) {
                    ;   // nothing to do, just let the thread die
                }
            }
        });
        thread.start();
        return thread;
    }


    //
    //  Private class classes
    //

    /**
     * Input stream filter which uses polling technique to simulate
     * non-blocking input stream (I've spent WAY too long trying to
     * develop a non-blocking reader for Stdin, so this is what you
     * get ;-), and which will <u>not</u> close the underlying stream
     * when the {@link #close()} method is called.
     */
    private static class NonCloseableInputStream extends FilterInputStream {


        //
        //  Private class data
        //

        /** number of milliseconds to wait before "polling" for input */
        private static final int POLL_MILLISECONDS = 100;


        //
        //  Private instance data
        //

        /** this flag is set when this input stream filter is "closed" */
        private boolean mClosedFlag;


        //
        //  Public instance constructors
        //

        /**
         * @param wrapped input stream to be wrapped by this one
         */
        public NonCloseableInputStream(InputStream wrapped) {
            super(wrapped);
        }


        //
        //  Public instance methods
        //

        /**
         * @see InputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (!streamHasData()) {
                return -1;
            }
            return in.read(buf, off, len);
        }

        /**
         * @see InputStream#read()
         */
        @Override
        public int read() throws IOException {
            if (!streamHasData()) {
                return -1;
            }
            return in.read();
        }

        /**
         * NOTE: does not close wrapped stream, by design!
         * @see InputStream#close()
         */
        @Override
        public void close() {
            mClosedFlag = true;
        }


        //
        //  Private instance methods
        //

        /**
         * @return true if data is present on input stream,
         * or false means input stream was "closed"
         */
        private boolean streamHasData() throws IOException {

            while(!mClosedFlag && available() == 0) {
                try {
                    Thread.sleep(POLL_MILLISECONDS);
                } catch(InterruptedException ie) {
                    ;
                }
            }

            return mClosedFlag ? false : true;
        }

    }

}
