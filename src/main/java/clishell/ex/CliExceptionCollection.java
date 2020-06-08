/**
 * Command Line Interface Harness - Exceptions
 */

package clishell.ex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * <code>CliExceptionCollection</code> is a specialization of a <code>CliException</code>
 * that also maintains a collection of "incidental exceptions" - exceptions that were
 * thrown secondary to the initial exception that caused an instance of this class to
 * be generated.  Code that wishes to try "cleanup operations" while handling an
 * exception might encounter other exceptions which they might wish to catch, but
 * nonetheless record.  This collection allows them to record those secondary exceptions
 * thrown while cleaning up from an initial exception.
 */
public class CliExceptionCollection extends CliException implements Collection<Throwable> {

    /** serial version ID of this class */
    private static final long serialVersionUID = 13342535920L;

    private final List<Throwable> mThrowableList = new ArrayList<Throwable>();

    /** default constructor */
    public CliExceptionCollection() {
    }

    /**
     * @param message exception message
     * @see Exception#Exception(String)
     */
    public CliExceptionCollection(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param throwable nested exception
     * @see Exception#Exception(String, Throwable)
     */
    public CliExceptionCollection(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * @param message exception message
     * @param throwableCollection list of throwables to copy into collection
     */
    public CliExceptionCollection(String message,
            Collection<Throwable> throwableCollection) {
        super(message);
        addAll(throwableCollection);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(Throwable e) {
        return mThrowableList.add(e);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends Throwable> c) {
        return mThrowableList.addAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        mThrowableList.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return mThrowableList.contains(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return mThrowableList.containsAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return mThrowableList.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    @Override
    public Iterator<Throwable> iterator() {
        return mThrowableList.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        return mThrowableList.remove(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return mThrowableList.removeAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return mThrowableList.retainAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    @Override
    public int size() {
        return mThrowableList.size();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    @Override
    public Object[] toArray() {
        return mThrowableList.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return mThrowableList.toArray(a);
    }

}
