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
				out.write("@base <" + base + "> .");
				out.newLine();
				//TODO: udìlat univerzální prefix, douèit se prefixy a významy
				if(prefix.equals("xsd")){
					out.write("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");
					out.newLine();
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
		ArrayList<DBColumn> columns = table.getAllColumns();
		String tableName = table.getName();
		int numberRows = columns.get(0).getData().size();
		try {
			out.newLine();
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
							first += primaryKeys.get(i) + "=" + table.getColumn(primaryKeys.get(0)).getData(rIndex) + ">";
						}
						else{
							first += primaryKeys.get(i) + "=" + table.getColumn(primaryKeys.get(0)).getData(rIndex) + ";";
						}
					}
				}
				else{
					first = "_:"+(char)noPrimary;
					noPrimary++;
				}
				String second = "rdf:type";
				String third = "<" + table.getName() + ">";
				out.write(first + " " + second + " " + third);
				out.newLine();
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
						
					out.write(first + " " + second + " " + third);
					out.newLine();
				}
				out.newLine();
			}
		} catch (IOException e) {
			System.out.println("Couldn't write into file");
			e.printStackTrace();
		}
	}
	
	public void closeWriting(){
		if(out != null) {
	        try {
				out.close();
			} catch (IOException e) {
				System.out.println("Error while closing log");
			}
	    }
	}
}
