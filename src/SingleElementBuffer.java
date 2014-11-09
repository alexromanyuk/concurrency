import java.lang.Integer;

public class SingleElementBuffer {
    private Integer value;

    public synchronized Integer get() throws InterruptedException {
        while (value == null) {
            this.wait();
        }

        System.out.printf("Thread %s get: %s \n\r", Thread.currentThread().getId(), value);

        Integer tmp = value;
        value = null;
        this.notifyAll();
        return tmp;
    }

    public synchronized void put(Integer input) throws InterruptedException {
        while (value != null) {
            this.wait();
        }

        this.value = input;
        this.notifyAll();

        System.out.printf("Thread %s put: %s \n\r", Thread.currentThread().getId(), value);
    }

    public static void main(String... args) {
        SingleElementBuffer buffer = new SingleElementBuffer();

        for (int threadsNum = 0; threadsNum < 5; threadsNum++) {
            new Thread(new Producer(buffer)).start();
            new Thread(new Consumer(buffer)).start();
        }
    }
}

class Producer implements Runnable {
    private SingleElementBuffer buffer;

    Producer(SingleElementBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int value=0; value < 100; value++) {
                buffer.put(value);
            }
        } catch (InterruptedException ignore) {/*NOP*/}
    }
}

class Consumer implements Runnable {
    private SingleElementBuffer buffer;

    Consumer(SingleElementBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Integer value = buffer.get();
            }
        } catch (InterruptedException ignore) {/*NOP*/}
    }
}

