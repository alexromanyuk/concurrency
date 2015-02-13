package the.art.of.multiprocessor.programming.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TASLock extends AbstractLock {

    private final AtomicBoolean lock = new AtomicBoolean(false);

    @Override
    public void lock() {
        while (lock.getAndSet(true)) {};
    }

    @Override
    public void unlock() {
        lock.set(false);
    }

}
