package akka;

import akka.actor.*;

import java.io.Serializable;

public class DinningPhilosophersAkka {

    private static class GetStick implements Serializable {}

    private static class Stick implements Serializable {
        public final int id;
        public final boolean isFree;
        public Stick(int id, boolean isFree) {
            this.id = id;
            this.isFree = isFree;
        }
    }

    public static class PhilosopherActor extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof Stick) {
                Stick stick = ((Stick)message);
                System.out.printf("Got stick #%s, isFree: %s", stick.id, stick.isFree);
            }
        }
    }

    public static class TableActor extends UntypedActor {
        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof GetStick) {
                getSender().tell(new Stick(111, false), self());
            }
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        ActorRef table = system.actorOf(Props.create(TableActor.class), "table");
        ActorRef philosopher = system.actorOf(Props.create(PhilosopherActor.class), "Sasha");

        table.tell(new GetStick(), philosopher);
        system.shutdown();
    }
}
