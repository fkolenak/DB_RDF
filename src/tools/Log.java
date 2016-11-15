package tools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class for simple logging
 * 
 * @author Jan Schropfer
 *
 */
public class Log {
	File log;
	BufferedWriter out = null;
	private static Log instance;
	
	/** Constructor */
	private Log(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyddMM_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime()) + ".log");
		log = new File("./logs/" + dateFormat.format(cal.getTime()) + ".log");
	}
	
	/** Get log instance	 */
	public static Log getInstance() {
        if (instance == null) {
        	System.out.print("creating log file ");
            instance = new Log();
        }
        return instance;
    }
	
	/** Open log */
	public void openLog(){
		FileWriter fstream;
		try {
			fstream = new FileWriter(log , true);
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			System.out.println("Error: couldn't write into file");
		}
	}
	
	/** Write single line to log	 */
	public void writeLine(String logLine){
		try {
			openLog();
			out.write(logLine);
			out.newLine();
			closeLog();
		} catch (IOException e) {
			System.out.println("Error while writting into log");
		}
	}
	
	/** Close log */
	public void closeLog(){
		if(out != null) {
	        try {
				out.close();
			} catch (IOException e) {
				System.out.println("Error while closing log");
			}
	    }
	}
}
