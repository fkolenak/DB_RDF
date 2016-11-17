import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import tools.DBColumn;
import tools.DBTable;

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
	BufferedWriter out;
	FileWriter fstream;
	
	public RDF(String file, String base, String prefix){
		this(file, "./", base, prefix);
	}
	
	public RDF(String file, String path, String base, String prefix){
		this.file = file;
		this.path = path;
		try {
			//Creating directory
			Files.createDirectories(Paths.get(path));
			
			document = new File(path + "/" + file + ".rdf");
			if(!document.exists())
				Files.createFile(Paths.get(path + "/" + file + ".rdf"));
			if(document.exists()){
				fstream = new FileWriter(document , true);
				out = new BufferedWriter(fstream);
				writeLine("@base <" + base + "> .");
				//TODO: udìlat univerzální prefix, douèit se prefixy a významy
				if(prefix.equals("xsd")){
					writeLine("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
				}
			}
			else
				System.out.println("Couldn't find file");
			
		} catch (IOException e) {
			System.out.println("Error while creating directory or file");
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
		int numberRows = columns.get(0).getData().size();
		
		newLine();
		
		//For triples without primary key
		int noPrimary = 97;
		
		for(int rIndex = 0; rIndex < numberRows; rIndex++){
			ArrayList<String> row = table.getRow(rIndex);
			String first = "";
			if(primaryKeys.size() == 1)
				first = "<" + tableName + "/" + primaryKeys.get(0) + "=" + table.getColumn(primaryKeys.get(0)).getData(rIndex) + ">";
			else if(primaryKeys.size() > 1){
				first = "<" + tableName + "/";
				for(int i = 0; i < primaryKeys.size(); i++){
					if(i == primaryKeys.size()-1){
						first += primaryKeys.get(i) + "=" + table.getColumn(primaryKeys.get(i)).getData(rIndex) + ">";
					}
					else{
						first += primaryKeys.get(i) + "=" + table.getColumn(primaryKeys.get(i)).getData(rIndex) + ";";
					}
				}
			}
			else{
				first = "_:"+(char)noPrimary;
				noPrimary++;
			}
			String second = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
			String third = "<" + table.getName() + "> .";
			writeLine(first + " " + second + " " + third);
			
			for(int dIndex = 0; dIndex < row.size(); dIndex++){
				second = "<" + tableName + "#" + columns.get(dIndex).getName() + ">";
				int type = columns.get(dIndex).getType();
				switch(type){
					case 4:	third = row.get(dIndex) + " ."; 
							break;
					case 12: third = "\"" + row.get(dIndex) + "\" .";
							break;
					default: third = row.get(dIndex) + " .";
				}
					
				writeLine(first + " " + second + " " + third);
				if(numberOfForeign > 0){
					boolean writeForeign = false;
					for(int fIndex = 0; fIndex < numberOfForeign; fIndex++){
						if(columns.get(dIndex).getName().equals(foreignKeys[fIndex][2])){
							writeForeign = true;
							second = "<" + tableName + "#ref-" + foreignKeys[fIndex][0] + ">";
							third = "<" + foreignKeys[fIndex][3] + "/" + foreignKeys[fIndex][4] + "=";
							
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
						writeLine(first + " " + second + " " + third);
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
		}
	}
	
	public void newLine(){
		try {
			openWritting();
			out.newLine();
			closeWriting();
		} catch (IOException e) {
			System.out.println("Error while writting into file");
		}
	}
	
	public void closeWriting(){
		if(out != null) {
	        try {
				out.close();
			} catch (IOException e) {
				System.out.println("Error while closing file");
			}
	    }
	}
}
