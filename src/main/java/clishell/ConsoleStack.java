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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import clishell.ex.CliRunnerException;

/**
 *
 * Manages "stack" of "console I/O objects of type T".
 *
 * Currently, accepts either <code>java.io.PrintWriter</code>
 * or <code>java.io.InputStream</code> objects.
 *
 */
public class ConsoleStack<T> {

    /**
     * Stack of "console" output streams
     */
    private final LinkedList<T> mConsoleStack = new LinkedList<T>();

    /**
     * @return I/O object of type T in current use by <code>CliRunner</code>
     * will return <code>null</code> if stack is empty
     */
    public T getConsole() {
        return mConsoleStack.peek();
    }

    /**
     * @param newConsole object to push onto the "stack" of console objects
     * of type T, and to begin using as the active object
     * @throws <code>NullPointerException</code> if <code>newConsole</code>
     * is <code>null</code>, or <code>IllegalArgumentException</code> if
     * <code>newConsole</code> is an instance of an unsupported class
     */
    public void setConsole(T newConsole) {
        if (newConsole == null) {
            throw new NullPointerException("invalid null parameter value"
                + "; new console object cannot be null");
        }
        T consoleObject = getConsole();
        if (consoleObject != null && consoleObject instanceof PrintWriter) {
            // flush output which may have accumulated in the current console
            ((PrintWriter) consoleObject).flush();
        }
        mConsoleStack.push(newConsole);
    }

    /**
     * Removes specified console object from stack, resulting in a new
     * "current console object of type T" if the specified object is at
     * the head of the stack
     * @param oldConsole old console object, previously set using
     * <code>setConsole()</code>
     * @throws CliRunnerException thrown if attempt to pop last console
     * object, or could not find specified console object in stack
     */
    public void unsetConsole(T oldConsole) throws CliRunnerException {

        if (mConsoleStack.size() < 2) {
            throw new CliRunnerException("attempt to pop last console object from stack");
        }

        // try use case #1; unset the object that's at the head of the stack
        if (oldConsole == mConsoleStack.peek()) {
            if (oldConsole instanceof PrintWriter) {
                // flush output which may have accumulated in the current console
                ((PrintWriter) oldConsole).flush();
            }
            mConsoleStack.pop();
            return;
        }

        // use case #2; look through the stack for the specified object,
        // and remove it if found, or throw an error if not
        Iterator<T> consoleStackIterator = mConsoleStack.iterator();
        while(consoleStackIterator.hasNext()) {
            T console = consoleStackIterator.next();
            if (oldConsole == console) {
                consoleStackIterator.remove();
                return;
            }
        }

        throw new CliRunnerException("could not unset specified console object; not found");
    }

}
