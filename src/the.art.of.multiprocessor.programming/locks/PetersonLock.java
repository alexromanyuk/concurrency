package the.art.of.multiprocessor.programming.locks;

public class PetersonLock extends AbstractLock {

    private final boolean flag[];
    private int victim;

    PetersonLock() {
        flag = new boolean[2];
    }

    @Override
    public synchronized void lock() {
        int i = (int) Thread.currentThread().getId();
        int j = 1 - i;

        flag[i] = true;
        victim = i;

        while (flag[j] == true && victim == i) {
            try {
                wait();
            } catch (InterruptedException ignore) { /* NOP */}
        }
    }

    @Override
    public synchronized void unlock() {
        int i = (int) Thread.currentThread().getId();
        flag[i] = false;
    }
}
