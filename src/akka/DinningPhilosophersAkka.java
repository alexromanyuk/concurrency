package akka;

import akka.actor.*;
import scala.concurrent.duration.Duration;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DinningPhilosophersAkka {

    protected static final int personsNumber = 5;

    private static class TakeSticks implements Serializable {
        public final int philosopherID;
        TakeSticks(int philosopherID) { this.philosopherID = philosopherID; }
    }

    private static class PutStick implements Serializable {
        public final Stick stick;
        PutStick(Stick stick) { this.stick = stick; }
    }

    private static class Stick implements Serializable {
        public final int id;
        public Stick(int id) { this.id = id; }
    }

    private static class LetsDinner implements Serializable {}

    public static class PhilosopherActor extends UntypedActor {
        private final int philosopherID;
        private final ActorRef table;

        private Stick left;
        private Stick right;

        public PhilosopherActor(int philosopherID, ActorRef table) {
            this.table = table;
            this.philosopherID = philosopherID;
        }

        private void sleep(int millis) {
            context().system().scheduler().scheduleOnce(
                    Duration.create(millis, TimeUnit.MILLISECONDS),
                    ActorRef.noSender(), null,
                    context().system().dispatcher(), null);
        }

        private void takeSticks() { table.tell(new TakeSticks(philosopherID), self()); }

        private void putSticks() {
            if (left != null) {
                table.tell(new PutStick(left), self());
                left = null;
            }

            if (right != null) {
                table.tell(new PutStick(right), self());
                right = null;
            }
        }

        private void eat() {
            //Try to acquire sticks
            while (left == null || right == null) { takeSticks(); }
            System.out.printf("Actor #%s eating... \n\r", philosopherID);
            putSticks();
        }

        private void think() {
            putSticks();
            System.out.printf("Actor %s thinking \n\r", philosopherID);
            sleep(50);
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof LetsDinner) {
                eat();
                think();
            }
            else if (message instanceof Stick) {
                Stick stick = ((Stick)message);
                System.out.printf("Pick up stick #%s \n\r", stick.id);
            }
        }
    }

    public static class TableActor extends UntypedActor {
        private final Stick[] sticks;

        public TableActor() {
            sticks = new Stick[personsNumber];

            for (int i = 0; i < personsNumber; i++) {
                Stick stick = new Stick(i);
                sticks[i] = stick;
            }
        }

        @Override
        public void onReceive(Object o) throws Exception {

            if (o instanceof TakeSticks) {
                int philosopherID = ((TakeSticks) o).philosopherID;

                int leftStickID = (philosopherID + personsNumber - 1) % (personsNumber - 1);
                int rightStickID = (philosopherID + personsNumber) % (personsNumber - 1);

                Stick leftStick = sticks[leftStickID];
                Stick rightStick = sticks[rightStickID];

                sticks[leftStickID] = null;
                sticks[rightStickID] = null;

                getSender().tell(leftStick, self());
                getSender().tell(rightStick, self());
            }

            if (o instanceof PutStick) {
                Stick stick = ((PutStick) o).stick;
                sticks[stick.id] = stick;
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        ActorRef table = system.actorOf(Props.create(TableActor.class), "table");
        ActorRef philosophers[] = new ActorRef[personsNumber];

        for (int i = 0; i < personsNumber; i++) {
            philosophers[i] = system.actorOf(Props.create(PhilosopherActor.class, i, table), new Integer(i).toString());
        }

        while (true) {
            for (ActorRef philosopher : philosophers) {
                philosopher.tell(new LetsDinner(), ActorRef.noSender());
            }
        }

//        system.shutdown();
    }
}
