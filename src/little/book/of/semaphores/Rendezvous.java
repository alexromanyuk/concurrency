package little.book.of.semaphores;

import java.util.concurrent.Semaphore;

//TODO read page 15 about inefficient solution
public class Rendezvous {
	public static void main(String[] args) {
		final Semaphore a1done = new Semaphore(0);
		final Semaphore b1done = new Semaphore(0);
		
		Thread threadA = new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("a1 done");
					a1done.release();
					b1done.acquire();
					System.out.println("a2 done");
				} catch(InterruptedException ex) {
					System.out.println("A interrupted");
				}
				
			}
		};
		
		Thread threadB = new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("b1 done");
					b1done.release();
					a1done.acquire();
					System.out.println("b2 done");
				} catch(InterruptedException ex) {
					System.out.println("B interrupted");
				}
			}
		};
		
		threadB.start();
		threadA.start();
	}
}
