package Thread;

import org.bytedeco.javacv.Frame;

import java.util.concurrent.LinkedBlockingDeque;

public class BlockingBuffer {
    private int mCapacityOfQueue = 100;
    private LinkedBlockingDeque mQeues = null;

    public BlockingBuffer(int mCapacityOfQueue) {
        this.mCapacityOfQueue = mCapacityOfQueue;
        this.mQeues = new LinkedBlockingDeque<>(mCapacityOfQueue);
    }

    public synchronized boolean isEmpty() {
        return mQeues.isEmpty();
    }

    public synchronized void clear() {
        mQeues.clear();
    }

    public synchronized int size() {
        return mQeues.size();
    }

    public <T> boolean push(T data) throws InterruptedException {
        if (mQeues.size() >= mCapacityOfQueue) {
            Object object = mQeues.takeFirst();
            if (object instanceof Frame) {
                ((Frame) object).close();
            }
        }
        return mQeues.offerLast(data);
    }

    public <T> T pop() throws InterruptedException {
        if (mQeues.size() > 0) {
            return (T) mQeues.takeLast();
        }
        return null;
    }

    public <T> T takeFirst() throws InterruptedException {
        if (mQeues.size() > 0) {
            return (T) mQeues.takeFirst();
        }
        return null;
    }

    public <T> T getLast() throws InterruptedException {
        if (mQeues.size() > 0) {
            return (T) mQeues.getLast();
        }
        return null;
    }

    public <T> T getFirst() throws InterruptedException {
        if (mQeues.size() > 0) {
            return (T) mQeues.getFirst();
        }
        return null;
    }


}
