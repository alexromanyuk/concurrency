package little.book.of.semaphores;

import java.util.concurrent.Semaphore;

//FIXME little.book.of.semaphores.BarrierObject
public class BarrierObject {
	private static Semaphore mutex = new Semaphore(1);
	private static Semaphore turnstile = new Semaphore(0);
	private static Semaphore turnstile2 = new Semaphore(1);
	private static final int n = 5;
	private static int count = 0;
	
	public static void phase1() throws InterruptedException {
		mutex.acquire();
			count++;
			if (count == n - 1) {
				turnstile.release(n);
				turnstile2.acquire();
			}
		mutex.release();
		
		turnstile.acquire();
	}
	
	public static void phase2() throws InterruptedException {
		mutex.acquire();
			count--;
			if (count == 0) {
				turnstile2.release(n);
				turnstile.release();
			}
		mutex.release();
		
		turnstile2.acquire();
	}
	
	public static void waitt() throws InterruptedException {
		phase1();
		phase2();
	}
	
	public static void main(String[] args) {
		for (int i=0; i<10; i++) {
			new Thread() {

				@Override
				public void run() {
					try {
						phase1();
						System.out.println("Critical section");
						phase2();
					} catch(InterruptedException ex) {
						ex.printStackTrace();
					}
				}				
			}.start();
		}
	}
}
