package little.book.of.semaphores;

import java.util.concurrent.Semaphore;


public class Semaphores {
	public static void main(String[] args) {
		final Semaphore available = new Semaphore(0);
		
		Thread threadA = new Thread() {
			@Override
			public void run() {
				System.out.println("ThreadA started");
				available.release();
				
			}
		};
		
		Thread threadB = new Thread() {
			@Override
			public void run() {
				try {
				available.acquire();
				System.out.println("ThreadB started");
				} catch(InterruptedException ex) {
					System.out.println("B interrupted");
				}
			}
		};
		
		threadB.start();
		threadA.start();
	}
}
