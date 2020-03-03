/*Saif Khan*/
package nachos.threads;
import java.util.*; 
import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator 
{
    /**
	 * Allocate a new communicator.
	 */
    private Condition 	speakerQueue; 
	private Condition 	speakers; 
	private Condition 	listenerQueue; 
	private Condition 	listener;
	//this is used for the while loops and to alose ensure that the speaker or listener are not speaking
	//and listening
	private boolean 	waitingListen; 
	private boolean 	waitingSpeaker; 
	//This is the message that is recieved. It's either ture or false
	private boolean 	receivedMessage; 
	//the integer to transfer
	private int 		transferredInt;
	private Lock 		sharedLock;
	
	public Communicator() 
	{
		sharedLock = new Lock();
		listenerQueue = new Condition(sharedLock);
		speakerQueue = new Condition(sharedLock);
		listener = new Condition(sharedLock);
		speakers = new Condition(sharedLock);
		waitingListen = false;
		waitingSpeaker = false;
		receivedMessage = false;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 *
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 *
	 * @param	word	the integer to transfer.
	 */

	public void speak(int word) 
	{
	    //we get the lock
		sharedLock.acquire();
		
		while(waitingSpeaker)
		{ 
			//put speaker to sleep as it is waiting
			speakerQueue.sleep();
		}
        //this ensures that any added speakers do not speak
		waitingSpeaker = true; 
		transferredInt = word;
		
		while(!receivedMessage|| !waitingListen) //if we havent recieved a message 
		{
			listener.wake(); 
			speakers.sleep(); 
		}
        //set false to ensure that the speaker and listener can get to the queue
		waitingListen = false; 
		waitingSpeaker = false; 
		receivedMessage = false;
		//we wake up the queue so that the listener and speaker can get there then release lock.
		speakerQueue.wake(); 
		listenerQueue.wake();
		sharedLock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return
	 * the <i>word</i> that thread passed to <tt>speak()</tt>.
	 *
	 * @return	the integer transferred.
	 */    
	public int listen() 
	{
		sharedLock.acquire();
		while(waitingListen)
		{
			listenerQueue.sleep();
		}

		waitingListen = true;
		// Until listenerWaiting is set to false, the process below is inaccessable to other listners	

		while(!waitingSpeaker){ //no speaker, go into loop
			listener.sleep(); //set this thread to be the first thread to recieve a message
		}

		//There is 1 speaker Sending message
		speakers.wake(); // wake up the sleeping speaker in sendingQueue
		receivedMessage = true;
		sharedLock.release();
		return transferredInt;
	}
	
	
	
	
	
	
	
	/*
	TESTING METHOD
	*/
	
	public static void selfTest()
  {
		final Communicator comtest = new Communicator();
    
    KThread secondThread = new KThread(new Runnable() 
    {
      public void run() 
      {
      System.out.println("The second thread is listening");
      comtest.listen();
      System.out.println("The second thread is done listening");
		  }
    });
		
    KThread firstThread = new KThread(new Runnable() 
    {
      public void run() 
      {
			System.out.println("The first thread is speaking");
			commu.speak(2);
		  System.out.println("The first thread is done speaking");
		  }
    });
		firstThread.fork();
		secondThread.fork();
		firstThread.join();
		secondThread.join();
   }
 }

	
}
