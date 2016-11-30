package main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;

import tools.Log;
import tools.StopWatch;

public class Config {
	private static HashMap<String, String> config;
	private static StopWatch stopwatch = StopWatch.getInstance();
	private static String CONFIG_FILE_PATH = "./config2";
	
	static{
		try {
			config = (HashMap<String, String>) Files
			.readAllLines(Paths.get(CONFIG_FILE_PATH))
			.stream()
			.filter(line -> !line.startsWith("#"))
			.filter(line -> line.contains("="))
			.collect(Collectors.toMap(line -> line.substring(0, line.indexOf('=')),
					line -> line.substring(line.indexOf('=')+1)));
		} catch (IOException e) {
			System.out.println("Error while parsing config file");
		}
		
	}
	
	public static String get(String s){
		return config.get(s);
	}
	
}