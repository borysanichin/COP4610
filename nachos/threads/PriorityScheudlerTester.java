package nachos.threads;

import nachos.machine.*;

/* Mohammadkian Maroofi */

/* following class is Implemented in order to
 *  perform a selfTest call in ThreadedKernel.selfTest() 
 *  */
public class PriorityScheudlerTester {

	public static void selfTest() {
		TestPrioprityScheduler();
	}


	public static void TestPrioprityScheduler() {
		Lib.debug(dbgFlag, "Entering PSTest");
		PriopritySchedulerTest1();
		PriopritySchedulerTest2();
		Lib.debug(dbgFlag, "Leaving PSTest");
	}


	
	
	/**
	 * Following method will create several instances of KThread, and later on assigns
	 * different priorities to them, some cases by assigning an integer (between 1 to 7 inclusively).
	 */
	
	public static void PriopritySchedulerTest1() {
		System.out.print("PriopritySchedulerTest1\n");

		Runnable testRunnable = new Runnable() {
			public void run() { 
				int i = 0;
				while(i < 10) { 
					System.out.println("*** test loop " + i);
					i++;
				}
				
			}
		}; 


		KThread testThread;
		testThread = new KThread(testRunnable);
		testThread.setName("TestThread1");
		testThread.fork();
		// Assigning priority
		ThreadedKernel.scheduler.setPriority(testThread, (int) 2);

		KThread testThread2;
		testThread2 = new KThread(testRunnable);
		testThread2.setName("TestThread2");
		// Assigning priority
		ThreadedKernel.scheduler.setPriority(testThread2, (int) 3);
		testThread2.fork();

		testThread.join();

		KThread t[] = new KThread[10];
		for (int i=0; i<10; i++) {
			t[i] = new KThread(testRunnable);
			t[i].setName("Thread" + i).fork();
			// Assigning priority
			ThreadedKernel.scheduler.setPriority(t[i], (int)((i+1)%8));
		}
		KThread.yield();
	}

	/* Three implemented Runnable classes to be used in second test case for PriorityScheduling */
	private static class lowThreadRunnable implements Runnable  {

		lowThreadRunnable(Lock lock, boolean isOpen) {
			this.lock = lock;
			this.isOpen = isOpen;
		}

		public void run() { 
			lock.acquire();
			while (this.isOpen == false) {
				System.out.print("LOW thread BLOCKED\n");
				KThread.currentThread().yield();
			}
			this.isOpen = false;
			System.out.print("LOW thread RELEASED\n");
			lock.release();
		}

		Lock lock;
		static public boolean isOpen = false;
	} 

	private static class highThreadRunnable implements Runnable  {

		highThreadRunnable(Lock lock) {
			this.lock = lock;
		}

		public void run() { 
			lowThreadRunnable.isOpen = true;

			lock.acquire();
			while (lowThreadRunnable.isOpen == true) {
				System.out.print("HIGH thread BLOCKED\n");
				KThread.currentThread().yield();
			}

			lowThreadRunnable.isOpen = true;
			System.out.print("HIGH thread RELEASED\n");
			lock.release();
		}

		Lock lock;
		static public boolean isOpen = false;
	} 

	private static class medThreadRunnable implements Runnable  {
		medThreadRunnable() {
		}

		public void run() { 
			while(lowThreadRunnable.isOpen == false) {
				System.out.print("MEDIUM thread BLOCKED\n");
				KThread.currentThread().yield();
			}

			System.out.print("MEDIUM thread RELEASED\n");
			System.out.println("TEST OK");
		}
	}

	/**
	 * VAR4: Create a scenario to hit the priority inverse problem.
	 * Verify the highest thread is blocked by lower priority thread.
	 */
	public static void PriopritySchedulerTest2() {
		System.out.print("PriopritySchedulerTest2\n");

		Lock lock = new Lock();

		// low priority thread closes the door
		KThread low = new KThread(new lowThreadRunnable(lock, false));
		low.fork();
		low.setName("low");
		ThreadedKernel.scheduler.setPriority(low, 1);
		KThread.currentThread().yield();

		// High priority thread "high" waits for low priority thread "low" because they use the same lock.

		// high priority thread opens the door
		KThread high = new KThread(new highThreadRunnable(lock));
		high.fork();
		high.setName("high");
		ThreadedKernel.scheduler.setPriority(high, 7);

		// medium priority thread waits for closing the door
		KThread medium = new KThread(new medThreadRunnable());
		medium.fork();
		medium.setName("medium");
		ThreadedKernel.scheduler.setPriority(medium, 6);

		KThread.currentThread().yield();
	}

	static private char dbgFlag = 't';
}

