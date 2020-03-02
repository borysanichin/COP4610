package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 *
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer priority from waiting threads
	 *					to the owning thread.
	 * @return	a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum &&
				priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;    

	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param	thread	the thread whose scheduling state to return.
	 * @return	the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			/* Mohammadkian Maroofi */
			// getThreadState(thread).acquire(this);
			ThreadState state = getThreadState(thread); 

			/* remove self from stateHolder's resource list when transferring priority */
			if (this.stateHolder != null && this.transferPriority) {
				this.stateHolder.pqCollection.remove(this);
			}
			this.stateHolder = state;             

			state.acquire(this);

		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			/* Mohammadkian Maroofi */
			if (waitQueue.isEmpty()) {
				return null;
			}
			/* remove self from stateHolder's resource list when transferring priority */
			if (this.stateHolder != null && this.transferPriority)  
			{
				this.stateHolder.pqCollection.remove(this);
			}

			KThread firstThread = pickNextThread();
			if (firstThread != null) {
				waitQueue.remove(firstThread);
				getThreadState(firstThread).acquire(this);
			}

			return firstThread;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		/* Mohammadkian Maroofi */
		/* Changed the return type from ThreadState to KThread */
		protected KThread pickNextThread() {
			/* Mohammadkian Maroofi */
			KThread nextThread = null;

			for (Iterator<KThread> ts = waitQueue.iterator(); ts.hasNext();) {  
				KThread thread = ts.next(); 
				int priority = getThreadState(thread).getEffectivePriority();

				if (nextThread == null || priority > getThreadState(nextThread).getEffectivePriority()) { 
					nextThread = thread;
				}
			}

			return nextThread;
		}

		/* Following Methods Added by Mohammadkian Maroofi */
		public int getEffectivePriority() {
			
			if (transferPriority == false) {
				return priorityMinimum;
			}

			if (priorityTrigger) {
				effectivePriority = priorityMinimum; 
				for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
					KThread thread = it.next(); 
					int priority = getThreadState(thread).getEffectivePriority();
					if ( priority > effectivePriority) { 
						effectivePriority = priority;
					}
				}
				priorityTrigger = false;
			}

			return effectivePriority;
		}

		public void setPriorityTrigger() {
			if (transferPriority == false) {
				return;
			}

			priorityTrigger = true;

			if (stateHolder != null) {
				stateHolder.setPriorityTrigger();
			}
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			/* Mohammadkian Maroofi */
			for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                KThread currentThread = it.next(); 
                int  priority = getThreadState(currentThread).getPriority();

                System.out.print("Thread: " + currentThread 
                                    + "\t  Priority: " + priority + "\n");
            }
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;

		/** The queue  waiting on this resource */
		private LinkedList<KThread> waitQueue = new LinkedList<KThread>();

		/** The ThreadState corresponds to the stateHolder of the resource */
		private ThreadState stateHolder = null;

		/** Set to true when a new thread is added to the queue, 
		 *  or any of the queues in the waitQueue flag themselves as priorityTrigger */
		private boolean priorityTrigger;

		/** The cached highest of the effective priorities in the waitQueue. 
		 *  This value is invalidated while priorityTrigger is true */
		private int effectivePriority; 

	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue
	 * it's waiting for, if any.
	 *
	 * @see	nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;

			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
		/* Mohammdkian Maroofi */
		public int getEffectivePriority() {
			int maxEffective = this.priority;
			if (priorityTrigger) {
				for (Iterator<ThreadQueue> it = pqCollection.iterator(); it.hasNext();) {  
					PriorityQueue pg = (PriorityQueue)(it.next()); 
					int effective = pg.getEffectivePriority();
					if (maxEffective < effective) {
						maxEffective = effective;
					}
				}
			}
			return maxEffective;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			/* Mohammadkian Maroofi */
			setPriorityTrigger();
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			/* Mohammadkian Maroofi */
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.assertTrue(waitQueue.waitQueue.indexOf(thread) == -1);

			waitQueue.waitQueue.add(thread);
			waitQueue.setPriorityTrigger();

			// set waitingOn
			waitOnQueue = waitQueue;

			// if the waitQueue was previously in myResource, remove it 
			// and set its stateHolder to null
			// When will this IF statement be executed?
			if (pqCollection.indexOf(waitQueue) != -1) {
				pqCollection.remove(waitQueue);
				waitQueue.stateHolder = null;
			}
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			/* Mohammadkian Maroofi */
			Lib.assertTrue(Machine.interrupt().disabled());

			pqCollection.add(waitQueue);

			// clean waitOnQueue if waitQueue is just waiting
			if (waitQueue == waitOnQueue) {
				waitOnQueue = null;
			}

			// effective priority may be varied, set priorityTrigger flag
			setPriorityTrigger();
		}	

		/* Mohammadkian Maroofi */
		/* Mutually recursive setPriorityTrigger() 
		 * method with the one implemented in the PriorityQueue class
		 */
		public void setPriorityTrigger() {
			if (priorityTrigger) {
				return;
			}

			priorityTrigger = true;

			PriorityQueue pg = (PriorityQueue) waitOnQueue;
			if (pg != null) {
				pg.setPriorityTrigger();
			}

		}

		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority; 

		/* Mohammadkian Maroofi */
		protected int effectivePriority;

		/** Collection of PriorityQueues that signify the Locks or other
		 *  resource that this thread currently holds */
		protected LinkedList<ThreadQueue> pqCollection = new LinkedList<ThreadQueue>();  

		/** PriorityQueue corresponding to resources that this thread has attempted to acquire but failed */
		protected ThreadQueue waitOnQueue; 

		/** Set to true when this thread's priority is changed, 
		 * or when one of the queues in pqCollection flags itself as priorityTrigger */
		private boolean priorityTrigger = false;  
	}
}
