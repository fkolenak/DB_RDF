package tools;

/**
 * Class for measuring time in app
 * @author Jan Schropfer
 *
 */
public class StopWatch{ 
	
	private static StopWatch instance;
	
	private long start;
	private long stop;
	
	
	private StopWatch(){
		start = 0;
		stop = 0;
	}
	
	/** Get log instance	 */
	public static StopWatch getInstance() {
        if (instance == null) {
        	System.out.print("creating log file ");
            instance = new StopWatch();
        }
        return instance;
    }
	
	/** Start method */
	public void start(){ 
		start = System.nanoTime();
	}
	
	/** Stop method with result in nanoseconds */
	public long stopNano(){ 
		stop = System.nanoTime();
		return stop - start;
	}
	
	/** Stop method with result in milliseconds */
	public long stopMili(){ 
		stop = System.nanoTime();
		return (stop - start) / 1000000;
	}
	
	/** Stop method with result in seconds */
	public double stopSecond(){ 
		stop = System.nanoTime();
		double temp = ((double)(stop - start)) / 1000000000;
		temp = Math.round(temp*100);
		return temp / 100;
	}
	
	/** Method for getting current time in nanoseconds since start */
	public long getNano(){ 
		return System.nanoTime() - start;
	}
	
	/** Method for getting current time in milliseconds since start */
	public long getMili(){
		return (System.nanoTime() - start) / 1000000;
	}
	
	/** Method for getting current time in seconds since start */
	public double getSecond(){ 
		double temp = ((double)(System.nanoTime() - start)) / 1000000000;
		temp = Math.round(temp*100);
		return temp / 100;
	}
}