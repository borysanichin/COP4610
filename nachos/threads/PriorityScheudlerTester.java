package nachos.threads;

import nachos.machine.*;

/* Mohammadkian Maroofi */

/* following class is implemenetd in order to
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
	 * Following method will create several instances of KThread, and later on assings
	 * different priorities to them, some cases by assiging an int (between 1 to 7 inclusively).
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

	private static class Runnable1 implements Runnable  {

		Runnable1(Lock lock, boolean isOpen) {
			this.lock = lock;
			this.isOpen = isOpen;
		}

		public void run() { 
			lock.acquire();
			while (this.isOpen == false) {
				System.out.print("Low thread is blocked\n");
				KThread.currentThread().yield();
			}
			this.isOpen = false;
			System.out.print("Low thread released\n");
			lock.release();
		}

		Lock lock;
		static public boolean isOpen = false;
	} 

	private static class Runnable2 implements Runnable  {

		Runnable2(Lock lock) {
			this.lock = lock;
		}

		public void run() { 
			Runnable1.isOpen = true;

			lock.acquire();
			while (Runnable1.isOpen == true) {
				System.out.print("High thread is blocked\n");
				KThread.currentThread().yield();
			}

			Runnable1.isOpen = true;
			System.out.print("High thread released\n");
			lock.release();
		}

		Lock lock;
		static public boolean isOpen = false;
	} 

	private static class Runnable3 implements Runnable  {
		Runnable3() {
		}

		public void run() { 
			while(Runnable1.isOpen == false) {
				System.out.print("Medium thread is blocked\n");
				KThread.currentThread().yield();
			}

			System.out.print("Medium thread released\n");
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
		KThread low = new KThread(new Runnable1(lock, false));
		low.fork();
		low.setName("low");
		ThreadedKernel.scheduler.setPriority(low, 1);
		KThread.currentThread().yield();

		// High priority thread "high" waits for low priority thread "low" because they use the same lock.

		// high priority thread opens the door
		KThread high = new KThread(new Runnable2(lock));
		high.fork();
		high.setName("high");
		ThreadedKernel.scheduler.setPriority(high, 7);

		// medium priority thread waits for closing the door
		KThread medium = new KThread(new Runnable3());
		medium.fork();
		medium.setName("medium");
		ThreadedKernel.scheduler.setPriority(medium, 6);

		KThread.currentThread().yield();
	}

	static private char dbgFlag = 't';
}

