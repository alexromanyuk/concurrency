package little.book.of.semaphores;

import java.util.concurrent.Semaphore;


public class Barrier {
	private static Semaphore mutex = new Semaphore(1);
	private static Semaphore turnstile = new Semaphore(0);
	private static Semaphore turnstile2 = new Semaphore(1);
	
	private static int count = 0;
	private static final int n = 11;
	
	public static void main(String[] args) {
		Thread[] bar = new Thread[n];
		for (int i=0; i<n; i++) {
			bar[i] = new CyclicBarrier();
			bar[i].setName(new Integer(i).toString());
			bar[i].start();
		}
	}
	
	static class BarRunner extends Thread {
		@Override
		public void run() {
			System.out.printf("Thread: %s is working \n", this.getName());
			try {
				mutex.acquire();
					if (count == n-1)
						turnstile.release();
					else { 
						count++;
						System.out.printf("%s threads arrived \n", count);
					}
				mutex.release();
				
				turnstile.acquire();
				turnstile.release();
				System.out.printf("Thread: %s is after barrier \n", this.getName());
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//FIXME rewrite to use preloaded turnstile
	static class CyclicBarrier extends Thread {
		@Override
		public void run() {
				for (int i=0; i<10; i++) {
					System.out.printf("Thread: %s is working \n", this.getName());
					try {
						mutex.acquire();
							if (count == n-1) {
								turnstile.release();
								turnstile2.acquire(); 
								System.out.printf("%s threads arrived \n", count);
							}
							else { 
								count++;
								System.out.printf("%s threads arrived, permits: %s \n", count, turnstile.availablePermits());
							}
						mutex.release();
						
						turnstile.acquire();
						turnstile.release();
						System.out.printf("Thread: %s is after barrier, permits: %s \n", this.getName(),turnstile.availablePermits());
						
						mutex.acquire();
							if (count == 0) {
								turnstile.acquire();
								turnstile2.release();
								System.out.printf("little.book.of.semaphores.Barrier locked, with count: %s \n", count);
							}
							else count--;
						mutex.release();
						 
						turnstile2.acquire();
						turnstile2.release();
						
					} catch(InterruptedException ex) {
						ex.printStackTrace();
					}
				}				
			}			
	}
}
