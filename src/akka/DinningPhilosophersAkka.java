package akka;

import akka.actor.*;
import scala.concurrent.duration.Duration;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DinningPhilosophersAkka {

    private static class TakeSticks implements Serializable {
        public final int philosopherID;
        TakeSticks(int philosopherID) { this.philosopherID = philosopherID; }
    }

    private static class PutStick implements Serializable {
        public final Stick stick;
        PutStick(Stick stick) { this.stick = stick; }
    }

    private static class StickIsBusy {}

    private static class Stick implements Serializable {
        public final int id;
        public Stick(int id) { this.id = id; }
    }

    public static class PhilosopherActor extends UntypedActor {
        private final int philosopherID;
        private final ActorRef table;

        private Stick left;
        private Stick right;

        public PhilosopherActor(int philosopherID, ActorRef table) {
            this.table = table;
            this.philosopherID = philosopherID;
        }

        private void think() {
            putSticks();
            sleep(50);
        }

        private void eat() {
            table.tell(new TakeSticks(philosopherID), self());
        }

        private void sleep(int millis) {
            context().system().scheduler().scheduleOnce(
                    Duration.create(millis, TimeUnit.MILLISECONDS),
                    ActorRef.noSender(), null,
                    context().system().dispatcher(), null);
        }

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

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof Stick) {
                Stick stick = ((Stick)message);
                System.out.printf("Got stick #%", stick.id);
            }
        }
    }

    public static class TableActor extends UntypedActor {

        private final int personsNumber = 5;
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
                Stick leftStick = sticks[philosopherID % (personsNumber - 1)];
//                Stick rightStick = sticks[philosopherID % (personsNumber - 1)]; TODO
                sticks[philosopherID] = null;

                getSender().tell(leftStick, self());
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
        ActorRef philosopher = system.actorOf(Props.create(PhilosopherActor.class), "Sasha");

//        table.tell(new TakeSticks(), philosopher); TODO
        system.shutdown();
    }
}
