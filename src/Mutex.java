import java.util.concurrent.Semaphore;

//TODO Write Multiplex
public class Mutex {

	protected final static Semaphore mutex = new Semaphore(0);
	
	public static void main(String[] args) {
		
		Thread threadA = new Counter();
		Thread threadB = new Counter();
		
		threadA.setName("A");
		threadB.setName("B");
		threadA.start();
		threadB.start();
	}
	
	static class Counter extends Thread {
		
		protected static int count; 
		
		@Override
		public void run() {
			try {
				mutex.acquire();
				count += 1;
				System.out.printf("Thread: %s Counter: %s", this.getName(), count);
				mutex.release();
			} catch(InterruptedException ex) {
				System.out.println(this.getName() + " interrupted");
			}

		}
	};
}
