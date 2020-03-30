package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
    	
    priorityQueue = new PriorityQueue<>();
    
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	
    /////////////////*Borys Anichin*//////////////////////////////////////////////////////////
	long nowTime = Machine.timer().getTime();
	
	if (priorityQueue.peek() == null)
		return;
				
	if (nowTime < priorityQueue.peek().getWakeTime())
	{
		return;
	}
	else
	{
		while( (priorityQueue.peek() != null) && (nowTime >= priorityQueue.peek().getWakeTime()) )
		{
			ThreadObject to = priorityQueue.poll();
			to.getThread().ready();
		}
	}
    /////////////////*Borys Anichin*//////////////////////////////////////////////////////////
	
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	
	long wakeTime = Machine.timer().getTime() + x;
	KThread thread = KThread.currentThread();
	
	///////////////////*Borys Anichin*////////////////////
	boolean interStatus = Machine.interrupt().disable();
	priorityQueue.add(new ThreadObject(wakeTime, thread));
	thread.sleep();
	Machine.interrupt().restore(interStatus);
    ///////////////////*Borys Anichin*////////////////////
	
    }
    //delete
    public static void selfTest() {

        System.out.print("Enter Alarm.selfTest\n");	

    	Runnable r = new Runnable() {
    	    public void run() {
                    KThread t[] = new KThread[10];

                    for (int i=0; i<10; i++) {
                         t[i] = new KThread(new AlarmTest(160 + i*20));
                         t[i].setName("Thread" + i).fork();
                    }
                    for (int i=0; i<10000; i++) {
                        KThread.yield();
                    }
                }
        };

        KThread t = new KThread(r);
        t.setName("Alarm SelfTest");
        t.fork();
        KThread.yield();

        t.join();

        System.out.print("Leave Alarm.selfTest\n");	

        }
    private static class AlarmTest implements Runnable {
    	AlarmTest(long x) {
    	    this.time = x;
    	}
    	
    	public void run() {

            System.out.print(KThread.currentThread().getName() + " alarm\n");	
            ThreadedKernel.alarm.waitUntil(time);
            System.out.print(KThread.currentThread().getName() + " woken up \n");	

    	}

        private long  time; 
        }
    //delete
    
    private Queue<ThreadObject> priorityQueue;
    
}
