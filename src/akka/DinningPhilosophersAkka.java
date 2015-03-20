package akka;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import java.io.Serializable;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

public class DinningPhilosophersAkka {

    private static final int NUMBER_OF_PERSONS = 5;
    private static final int NUMBER_OF_TRIES_TILL_DEATH = 100;

    private static class TakeStick implements Serializable {
        public final int stickID;
        TakeStick(int stickID) { this.stickID = stickID; }
    }

    private static class PutStick implements Serializable {
        public final Stick stick;
        PutStick(Stick stick) { this.stick = stick; }
    }

    private static class Stick implements Serializable {
        public final int id;
        public Stick(int id) { this.id = id; }
    }

    private static class StickNotAvailable implements Serializable {
        public final int id;
        StickNotAvailable(int id) { this.id = id; }
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

        /**
         * @param stickID
         * @param isLeft true = left, false = right
         * @return true, if stick was received; false otherwise
         * @throws Exception
         */
        private boolean tryToTakeStick(int stickID, boolean isLeft) throws Exception {
            Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
            Future<Object> future = Patterns.ask(table, new TakeStick(stickID), timeout);
            Object result = Await.result(future, timeout.duration());

            if (result instanceof Stick) {
                if (isLeft) { this.left = (Stick) result; }
                else { this.right = (Stick) result; }

                System.out.printf("Actor %s picked up stick #%s \n\r", philosopherID, ((Stick)result).id);
                return true;

            } else if (result instanceof StickNotAvailable) {
                stickID = ((StickNotAvailable) result).id;
                System.out.printf("Actor %s tried to take stick %s, but it's busy \n\r", philosopherID, stickID);
                putSticks();
            }

            return false;
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

        private boolean takeBothSticks() throws Exception {
            final int leftStickID = (philosopherID + NUMBER_OF_PERSONS - 1) % (NUMBER_OF_PERSONS - 1);
            final int rightStickID = (philosopherID + NUMBER_OF_PERSONS) % (NUMBER_OF_PERSONS - 1);

            boolean tookLeft = tryToTakeStick(leftStickID, true);
            boolean tookRight = tryToTakeStick(rightStickID, false);

            return tookLeft && tookRight;
        }

        private void eat() throws Exception {
            long numOfTries = 0 ;

            while (!(takeBothSticks())) {
                if (numOfTries > NUMBER_OF_TRIES_TILL_DEATH) {
                    System.out.println(String.format("Actor %s died hungry...", philosopherID));
                    context().stop(self());
                }
                numOfTries++;
            }
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
        }
    }

    public static class TableActor extends UntypedActor {
        private final Stick[] sticks;

        public TableActor() {
            sticks = new Stick[NUMBER_OF_PERSONS];

            for (int i = 0; i < NUMBER_OF_PERSONS; i++) {
                Stick stick = new Stick(i);
                sticks[i] = stick;
            }
        }

        @Override
        public void onReceive(Object o) throws Exception {

            if (o instanceof TakeStick) {
                int stickID = ((TakeStick) o).stickID;
                checkAndSendStick(stickID);
            }

            if (o instanceof PutStick) {
                Stick stick = ((PutStick) o).stick;
                sticks[stick.id] = stick;
            }
        }

        private void checkAndSendStick(int stickID) {
            Stick stick = sticks[stickID];

            if (stick == null) {
                getSender().tell(new StickNotAvailable(stickID), self());
            } else {
                sticks[stickID] = null;
                getSender().tell(stick, self());
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        ActorRef table = system.actorOf(Props.create(TableActor.class), "table");
        ActorRef philosophers[] = new ActorRef[NUMBER_OF_PERSONS];

        for (int i = 0; i < NUMBER_OF_PERSONS; i++) {
            philosophers[i] = system.actorOf(Props.create(PhilosopherActor.class, i, table), new Integer(i).toString());
        }

        for (ActorRef philosopher : philosophers) {
            philosopher.tell(new LetsDinner(), ActorRef.noSender());
        }

    }
}
