package tools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import main.Config;

/**
 * Class for simple logging
 * 
 * @author Jan Schropfer
 *
 */
public class Log {
	private File log;
	private BufferedWriter out = null;
	private static Log instance;
	
	/** Constructor */
	private Log(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyddMM_HH_mm_ss");
		Calendar cal = Calendar.getInstance();
		String fileName = dateFormat.format(cal.getTime()) + ".log";
		if(!Config.get("logName").equals(""))
			fileName = Config.get("logName");
		System.out.println(fileName);
		new File(Config.get("logPath")).mkdirs();
		log = new File(Config.get("logPath")+"/" + fileName);
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
