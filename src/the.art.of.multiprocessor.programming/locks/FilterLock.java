package the.art.of.multiprocessor.programming.locks;

public class FilterLock extends AbstractLock {

    private final int level[];
    private final int victim[];

    FilterLock(int numLevels, int numVictims) {
        level = new int[numLevels];
        victim = new int[numVictims];
    }

    @Override
    public void lock() {
        int me = (int) Thread.currentThread().getId();

        for (int i = 0; i < level.length; i++) {
            while (conflictExists(me, i));
        }
    }

    @Override
    public void unlock() {

    }

    private boolean conflictExists(int me, int myLevel) {

        boolean conflictExists = false;
        for (int i = myLevel; i < victim.length; i++) {
            int k = victim[i];

            if (k != me && k != 0) return true;
        }

        return conflictExists;
    }
}
