package generator;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public abstract class Generator<T> extends Thread implements Iterable<T>, Iterator<T> {

    public static class StopIteration extends RuntimeException {
        private static final long serialVersionUID = 2384769048034L;
    }
    public static class HaltIteration extends RuntimeException {
        private static final long serialVersionUID = 2384769048034L;
    }

    private static enum Control {
        DATA,
        STOP
    };

    private static final class Packet<T> {

        private Control control;
        private T item;

        public Packet(T item) {
            this.item = item;
            this.control = Control.DATA;
        }

        public Packet(Control control) {
            this.item = null;
            this.control = control;
        }
    }

    private Thread producer = null;

    private T nextItem;

    private BlockingQueue<Packet<T>> queue = null;

    private boolean stopped = false;

    private boolean hasNextCalled = false;

    public Generator() {
        this.queue = new SynchronousQueue<Packet<T>>();
        this.producer = this;
    }

    public Generator(int size) {
        this.queue = new ArrayBlockingQueue<Packet<T>>(size);
        this.producer = this;
    }

    protected abstract void generate();

    public Iterator<T> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {

        if (this.stopped) {
            return false;
        }

        if (this.hasNextCalled) {
            return true;
        }

        // TODO atomicize this transition
        this.hasNextCalled = true;

        if (this.producer.getState() == Thread.State.NEW) {
            this.producer.start();
        }

        Packet<T> nextPacket;
        try {

            nextPacket = queue.take();

        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (nextPacket.control == Control.DATA) {
            this.nextItem = nextPacket.item;
            return true;
        }

        this.stopped = true;
        return false;

    }

    public T next() {

        if (this.stopped) {
            throw new StopIteration();
        }

        if (!this.hasNextCalled) {
            if (!hasNext()) {
                throw new StopIteration();
            }
        }

        this.hasNextCalled = false;
        return this.nextItem;

    }

    protected void yield(T nextItem) {

        final Packet<T> nextPacket = new Packet<T>(nextItem);
        try {
            queue.put(nextPacket);
        } catch (final InterruptedException e) {
            throw new HaltIteration();
        }
    }

    public void halt() {
        this.stopped = true;
        this.producer.interrupt();
    }

    public void close() {
        halt();
    }

    private void terminate() {
        try {
            this.queue.put(new Packet<T>(Control.STOP));
        } catch (final InterruptedException e) {
            throw new HaltIteration();
        }
    }

    public void run() {
        try {
            generate();
            terminate();
        } catch (final HaltIteration e) {
            // TODO
        }
    }

}
