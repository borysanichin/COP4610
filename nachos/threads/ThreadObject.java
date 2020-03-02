package nachos.threads;

/////////////////////////////////////////////////////////////
////////*Author: Borys Anichin*//////////////////////////////
public class ThreadObject implements Comparable<ThreadObject>{
	private long wakeTime;
	private KThread thread;
	
	public ThreadObject(long wakeTime, KThread thread)
	{
		this.wakeTime = wakeTime;
		this.thread = thread;
	}
	
	public long getWakeTime() {
		return wakeTime;
	}
	
	public KThread getThread() {
		return thread;
	}
	
	@Override
	public int compareTo(ThreadObject other)
	{
		if (wakeTime < other.getWakeTime())
			return -1;
		else if (wakeTime == other.getWakeTime())
			return 0;
		else
			return 1;
	}
}
////////*Author: Borys Anichin*//////////////////////////////
/////////////////////////////////////////////////////////////
