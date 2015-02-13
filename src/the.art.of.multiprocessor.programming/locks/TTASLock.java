package the.art.of.multiprocessor.programming.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock extends AbstractLock {

    private final AtomicBoolean lock = new AtomicBoolean(false);

    @Override
    public void lock() {

        while (true) {
            if (!lock.get()) {
                while (lock.getAndSet(true)) {};
            }
        }
    }

    @Override
    public void unlock() {
        lock.set(false);
    }
}

