package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList; 
import java.util.Iterator;   
import java.util.HashMap; 

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
	/**
	 * Allocate a new user kernel.
	 */
	public UserKernel() {
		super();
	}

	/**
	 * Initialize this kernel. Creates a synchronized console and sets the
	 * processor's exception handler.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());

		Machine.processor().setExceptionHandler(new Runnable() {
			public void run() { exceptionHandler(); }
		});
		/**
		 * Mohammadkian Maroofi 
		 */
		int numPhysPages = Machine.processor().getNumPhysPages(); 
		for(int i = 0; i < numPhysPages; i++) { 
			pageTable.add(i);
		}
	}

	/**
	 * Test the console device.
	 */	
	public void selfTest() {
		super.selfTest();

		//System.out.println("Testing the console device. Typed characters");
		//System.out.println("will be echoed until q is typed.");

		/*char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");*/
<<<<<<< HEAD
		//UserProcess userProcess = new UserProcess();
		//userProcess.handleSyscall(4, 0, 2, 2, 2);
		//userProcess.handleSyscall(5, 0, 2, 2, 2);

	}

	/**
	 * Returns the current process.
	 *
	 * @return	the current process, or <tt>null</tt> if no process is current.
	 */
	public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
			return null;

		return ((UThread) KThread.currentThread()).process;
	}

	/**
	 * The exception handler. This handler is called by the processor whenever
	 * a user instruction causes a processor exception.
	 *
	 * <p>
	 * When the exception handler is invoked, interrupts are enabled, and the
	 * processor's cause register contains an integer identifying the cause of
	 * the exception (see the <tt>exceptionZZZ</tt> constants in the
	 * <tt>Processor</tt> class). If the exception involves a bad virtual
	 * address (e.g. page fault, TLB miss, read-only, bus error, or address
	 * error), the processor's BadVAddr register identifies the virtual address
	 * that caused the exception.
	 */
	public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);

		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		process.handleException(cause);
	}

	/**
	 * Start running user programs, by creating a process and running a shell
	 * program in it. The name of the shell program it must run is returned by
	 * <tt>Machine.getShellProgramName()</tt>.
	 *
	 * @see	nachos.machine.Machine#getShellProgramName
	 */
	public void run() {
		super.run();

		UserProcess process = UserProcess.newUserProcess();

		String shellProgram = Machine.getShellProgramName();	
		Lib.debug('a', "Shell program: " + shellProgram);
		Lib.assertTrue(process.execute(shellProgram, new String[] {shellProgram, "0" }));

		KThread.currentThread().finish();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}



	/**
	 * Mohammadkian Maroofi
	 */
	/**
	 * Return number of a free page.
	 * If pageTable is empty, return -1 otherwise return free page number.
	 */
	public static int getFreePage() {                    
		int pageNumber = -1;                             
		Machine.interrupt().disable();                             
		if (pageTable.isEmpty() == false)                          
			pageNumber = pageTable.removeFirst();         
		Machine.interrupt().enable();                              
		return pageNumber;                               
	}                                                    
	/**
	 * Add a free page into page linked list.
	 */
	public static void addFreePage(int pageNumber) {     
		Lib.assertTrue(pageNumber >= 0                    
				&& pageNumber < Machine.processor().getNumPhysPages()); 
		Machine.interrupt().disable();                              
		pageTable.add(pageNumber);                                  
		Machine.interrupt().enable();                               
	}                                                              
	/**
	 * return next Pid
	 */
	public static int getNextPid() {                               
		int retval;                                                
		Machine.interrupt().disable();                             
		retval = ++nextPid;                                        
		Machine.interrupt().enabled();                             
		return nextPid;                                            
	}                                                              
	/**
	 * get process from process map by pid
	 */
	public static UserProcess getProcessByID(int pid) {
		return processMap.get(pid);
	}
	/**
	 * register a process to the map in Kernel 
	 */
	public static UserProcess registerProcess(int pid, UserProcess process) {  
		UserProcess insertedProcess;                               
		Machine.interrupt().disable();                             
		insertedProcess = processMap.put(pid, process);            
		Machine.interrupt().enabled();                             
		return insertedProcess;                                    
	}                                                              
	/**
	 * unregister a process in the process map 
	 */
	public static UserProcess unregisterProcess(int pid) {         
		UserProcess deletedProcess;                                
		Machine.interrupt().disable();                             
		/* Remove value for key pid  */
		deletedProcess = processMap.remove(pid);                   
		Machine.interrupt().enabled();                             
		return deletedProcess;                                     
	}  



	/** Globally accessible reference to the synchronized console. */
	public static SynchConsole console;

	// dummy variables to make javac smarter
	private static Coff dummy1 = null;

	/**
	 * Mohammadkian Maroofi
	 */

	/** maintain a global linked list of free physical pages.   */
	private static LinkedList<Integer> pageTable = new LinkedList<Integer>();  

	/** Maintain a static counter which indicates the next process ID
	 * to assign, assume that the process ID counter will not overflow.
	 */
	private static int nextPid = 0; 

	/** maintain a map which stores processes, key is pid,
	 * value is the process which holds the pid.                  
	 */
	private static HashMap<Integer, UserProcess>              
	processMap = new HashMap<Integer, UserProcess>(); 

=======
	//UserProcess userProcess = new UserProcess();
	//userProcess.handleSyscall(4, 0, 2, 2, 2);
	//userProcess.handleSyscall(5, 0, 2, 2, 2);
	//userProcess.handleSyscall(6, 0, 1, 2, 0);
	
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
>>>>>>> 75a82549501636e371d3ebcf1a7639ae806d327a
}
