package the.art.of.multiprocessor.programming.locks;

public class BackoffLock {

    static class Backoff {
        private final int minDelay;
        private final int maxDelay;

        public Backoff(int minDelay, int maxDelay) {
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        public void backoff() throws InterruptedException {
            Thread.sleep(1);
        }
    }
}
