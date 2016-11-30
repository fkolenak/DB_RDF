package main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import tools.DBColumn;
import tools.DBTable;
import tools.Log;
import tools.StopWatch;

public class RDF {	
	//TODO https://www.w3.org/TR/rdb-direct-mapping/
	/*
	-7 	BIT
	-6 	TINYINT
	-5 	BIGINT
	-4 	LONGVARBINARY 
	-3 	VARBINARY
	-2 	BINARY
	-1 	LONGVARCHAR
	0 	NULL
	1 	CHAR
	2 	NUMERIC
	3 	DECIMAL
	4 	INTEGER
	5 	SMALLINT
	6 	FLOAT
	7 	REAL
	8 	DOUBLE
	12 	VARCHAR
	91 	DATE
	92 	TIME
	93 	TIMESTAMP
	1111  	OTHER
	*/
	private String file;
	private String path;
	
	private File document;
	private BufferedWriter out;
	private FileWriter fstream;
	private Log log = Log.getInstance();
	private StopWatch stopwatch = StopWatch.getInstance();
	String base;
	
	public RDF(String file, String base){
		this(file, "./", base);
	}
	
	public RDF(String file, String path, String base){
		this.file = file;
		this.path = path;
		this.base = base;
		try {
			//Creating directory
			Files.createDirectories(Paths.get(path));
			
			document = new File(path + "/" + file + ".rdf");
			if(!document.exists())
				Files.createFile(Paths.get(path + "/" + file + ".rdf"));
			if(document.exists()){
				fstream = new FileWriter(document , false);
				out = new BufferedWriter(fstream);
				//TODO: udìlat univerzální prefix, douèit se prefixy a významy
			}
			else
				System.out.println("Couldn't find file");
			
		} catch (IOException e) {
			System.out.println("Error while creating directory or file");
			log.writeLine(stopwatch.getMili()+": Error while creating directory or file");
			e.printStackTrace();
		}
	}
	
	public void writeTable(DBTable table){
		ArrayList<String> primaryKeys = table.getAllPrimaryKeys();
		String [][] foreignKeys = null;
		int numberOfForeign = table.getAllForeignKeys().size();
		if(numberOfForeign > 0){
			foreignKeys = new String [numberOfForeign][5];
			for(int i = 0; i < numberOfForeign; i++){
				String[] parts = table.getAllForeignKeys().get(i).split(":");
				for(int j = 0; j < 5; j++)
					foreignKeys[i][j] = parts[j];
			}
		}
		ArrayList<DBColumn> columns = table.getAllColumns();
		String tableName = table.getName();
		int numberRows;
		if(columns.size() > 0)
			numberRows = columns.get(0).getData().size();
		else
			numberRows = 0;
		
		newLine();
		
		//For triples without primary key
		int noPrimary = 97;
		
		boolean writeForeign = false;
		if(numberOfForeign > 0)
			writeForeign = true;
		for(int rIndex = 0; rIndex < numberRows; rIndex++){

			ArrayList<String> row = table.getRow(rIndex);
			String first = "";
			if(primaryKeys.size() == 1)
				first = "<" + base + tableName + "/" + primaryKeys.get(0) + "-" + table.getColumn(primaryKeys.get(0)).getData(rIndex) + ">";
			else if(primaryKeys.size() > 1){
				first = "<" + base + tableName + "/";
				for(int i = 0; i < primaryKeys.size(); i++){
					if(i == primaryKeys.size()-1){
						first += primaryKeys.get(i) + "-" + table.getColumn(primaryKeys.get(i)).getData(rIndex) + ">";
					}
					else{
						first += primaryKeys.get(i) + "-" + table.getColumn(primaryKeys.get(i)).getData(rIndex) + "-";
					}
				}
			}
			else{
				first = "_:"+(char)noPrimary;
				noPrimary++;
			}
			String second = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
			String third = "<" + base + table.getName() + "> .";
			writeLine(first.replace(" ","_").replace(":","_") + " " + second + " " + third);
			

			for(int dIndex = 0; dIndex < row.size(); dIndex++){
				/*if(!writeForeign)
					second = "<" + base + tableName + "-" + columns.get(dIndex).getName() + ">";
				else*/
				second = "<" + base + tableName + "/" + columns.get(dIndex).getName() + ">";
				int type = columns.get(dIndex).getType();
				switch(type){
					case 1:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#string> ."; 
							break;
					case 3:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ."; 
					break;
					case 4:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#integer> ."; 
							break;
					case 6:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#float> ."; 
					break;
					case 12: third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#string> .";
							break;
					case 91:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#date> ."; 
					break;
					case 92:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#time> ."; 
					break;
					case 93:	third = "\"" + row.get(dIndex) + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ."; 
					break;
					default: third = "\"" + row.get(dIndex) + "\" .";
				}
					
				writeLine(first.replace(" ","_").replace(":","_") + " " + second.replace(" ","_").replace(":","_") + " " + third);
				if(numberOfForeign > 0){
					writeForeign = false;
					for(int fIndex = 0; fIndex < numberOfForeign; fIndex++){
						if(columns.get(dIndex).getName().equals(foreignKeys[fIndex][2])){
							writeForeign = true;
							second = "<" + base + tableName + "/ref-" + foreignKeys[fIndex][0] + ">";
							third = "<" +  base + foreignKeys[fIndex][3] + "/" + foreignKeys[fIndex][4] + "-";
							
							type = columns.get(dIndex).getType();
							switch(type){
								case 4:	third += row.get(dIndex) + "> ."; 
										break;
								case 12: third += "\"" + row.get(dIndex) + "\"> .";
										break;
								default: third += row.get(dIndex) + "> .";
							}
						}
					}
					if(writeForeign)
						writeLine(first.replace(" ","_").replace(":","_") + " " + second.replace(" ","_").replace(":","_") + " " + third);
				}
			}
			newLine();
		}
	}
	
	public void openWritting(){
		FileWriter fstream;
		try {
			fstream = new FileWriter(document , true);
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			System.out.println("Error: couldn't write into file");
			log.writeLine(stopwatch.getMili()+": Error: couldn't write into file");
			
		}
	}
	
	public void writeLine(String line){
		try {
			openWritting();
			out.write(line);
			out.newLine();
			closeWriting();
		} catch (IOException e) {
			System.out.println("Error while writting into file");
			log.writeLine(stopwatch.getMili()+": Error: couldn't write into file");
		}
	}
	
	public void newLine(){
		try {
			openWritting();
			out.newLine();
			closeWriting();
		} catch (IOException e) {
			System.out.println("Error while writting into file");
			log.writeLine(stopwatch.getMili()+": Error: couldn't write into file");
		}
	}
	
	public void closeWriting(){
		if(out != null) {
	        try {
				out.close();
			} catch (IOException e) {
				System.out.println("Error while closing file");
				log.writeLine(stopwatch.getMili()+": Error while closing file");
			}
	    }
	}
}
