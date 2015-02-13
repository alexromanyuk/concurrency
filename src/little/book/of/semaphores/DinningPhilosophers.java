package little.book.of.semaphores;

import java.util.Arrays;
import java.util.Random;

//Broken
public class DinningPhilosophers {

    private static class Stick {
        private final int id;

        private boolean isFree = true;
        private long holderThread;

        Stick(int id) {
            this.id = id;
        }

        public synchronized boolean getStick() {
            if (isFree == true) {
                isFree = false;
                holderThread = Thread.currentThread().getId();
                return true;

            } else {
                return false;
            }
        }

        public synchronized void putStick() {
            if (holderThread == Thread.currentThread().getId()) {
                isFree = true;
                holderThread = -1;
            }
        }

        @Override
        public String toString() {
            return String.format("Stick #%s, isFree: %s", id, isFree);
        }
    }

    private static class Philosopher implements Runnable {

        private final Stick left;
        private final Stick right;
        private final String name;

        private final Random random = new Random();

        public Philosopher(String name, Stick left, Stick right) {
            this.name = name;
            this.left = left;
            this.right = right;
        }

        public void think() throws InterruptedException {
            left.putStick();
            right.putStick();
            System.out.println(name + " thinks...");
            Thread.sleep(random.nextInt(1000));
        }

        public void eat() throws InterruptedException {
            while (!(left.getStick() && right.getStick())) { think(); }
            System.out.println(name + " eats...");
        }

        @Override
        public String toString() { return name; }

        @Override
        public void run() {
            while (true) {
                try {

                    eat();
                    think();

                } catch (InterruptedException ignore) { /* NOP */}
            }
        }
    }

    public static void main(String[] args) {

        final String[] names = {"Sasha", "Pasha", "Oleksiy", "Andrew"};
        final int numberOfParticipants = names.length;

        Thread[] philosophers = new Thread[numberOfParticipants];
        Stick[] sticks = new Stick[numberOfParticipants];

        for (int stickId = 0; stickId < numberOfParticipants; stickId++) {
            sticks[stickId] = new Stick(stickId);
        }

        int leftStickId = numberOfParticipants - 1;
        int rightStickId = leftStickId + 1;
        for (int philosopherId = 0; philosopherId < numberOfParticipants; philosopherId++) {
            Stick left = sticks[leftStickId++ % numberOfParticipants];
            Stick right = sticks[rightStickId++ % numberOfParticipants];

            Philosopher philosopher = new Philosopher(names[philosopherId], left, right);
            philosophers[philosopherId] = new Thread(philosopher, names[philosopherId]);
        }

        for (Thread philosopher : Arrays.asList(philosophers)) {
            philosopher.start();
        }

    }

}
