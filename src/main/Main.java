package main;
import java.sql.*;
import java.util.ArrayList;

import tools.DBTable;
import tools.Log;
import tools.StopWatch;

public class Main {

	private static final String JDBC_DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver"; 
	private static final String JDBC_DRIVER_POSTGRESS = "org.postgresql.Driver";
	
	private static String url;
	private static String user;
	private static String password;
	private static String serverName;
	private static String portNumber;
	private static String dbSchema;
	private static String serviceName;
	private static String tablePrefix;
	
	private static ArrayList<DBTable> tables;	
	private static Log log = Log.getInstance();
	private static StopWatch stopwatch = StopWatch.getInstance();
	private static String databaseType = "oracle";
	
	public static void main(String[] args) {
		
		stopwatch.start();
		
		//Load parameters
		user = Config.get("user");
		password = Config.get("password");
		serverName = Config.get("serverName");
		portNumber = Config.get("portNumber");
		dbSchema = Config.get("dbSchema");
		serviceName = Config.get("serviceName");
		tablePrefix = Config.get("tablePrefix");
		databaseType = Config.get("dbType");
		if(databaseType.equals("post"))
			dbSchema = dbSchema.toLowerCase();
		
		try {
			log.writeLine(stopwatch.getMili()+": Loading driver");
			if(databaseType.equals("oracle"))
				Class.forName(JDBC_DRIVER_ORACLE);
			else if(databaseType.equals("post"))
				Class.forName(JDBC_DRIVER_POSTGRESS);
		} catch (ClassNotFoundException e) {
			log.writeLine(stopwatch.getMili()+": Loading driver failed");
			e.printStackTrace();
		}
		if(databaseType.equals("oracle"))
			url = "jdbc:oracle:thin:"+user+"/"+password+"@"+serverName+":"+portNumber+":"+serviceName;
		else if(databaseType.equals("post"))
			url = "jdbc:postgresql://"+serverName+":"+portNumber+"/"+serviceName+"?user="+user+"&password="+password+"&sslmode=require";
	    log.writeLine(stopwatch.getMili()+": Connecting: "+url);
		try {
			
			System.out.println("Connecting to database: " + serverName);
			Connection conn = DriverManager.getConnection(url);
			//System.out.println(conn.getSchema());
			System.out.println("Connection complete");
			log.writeLine(stopwatch.getMili()+": Successfull connection");
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			RDF rdf = new RDF(Config.get("rdfName"), Config.get("rdfPath"), Config.get("rdfBase"));
			
			String   catalog          = null;
			String   schemaPattern    = dbSchema;
			String   tableNamePattern = null;
			String[] types            = null;
			tables = new ArrayList<DBTable>();
			ArrayList<String> tableNames = new ArrayList<String>();
			
			log.writeLine(stopwatch.getMili()+": Loading database tables:\n");
			System.out.println("Loading Tables");
			ResultSet result = databaseMetaData.getTables(
						catalog, schemaPattern,  tableNamePattern, types);

			while(result.next()) {
			    String tableName = result.getString(3);
			    if(tableName.startsWith(tablePrefix) || Config.get("tablePrefix").equals("")){
			    	tables.add(new DBTable(tableName));
			    	tableNames.add(tableName);
			    	System.out.println(tableName);
			    }
			}
			System.out.println("\nTables loaded");
			log.writeLine(stopwatch.getMili()+": Database tables loaded");
			String columnNamePattern = null;
			
			//Retrieving columns for tables in database
			log.writeLine(stopwatch.getMili()+": Loading columns and data");
			System.out.println("\nLoading columns for tables\n");
			for(int tIndex = 0; tIndex < tableNames.size(); tIndex++){
				DBTable temp = new DBTable(tableNames.get(tIndex));
				catalog           = null;
				schemaPattern     = dbSchema;
				tableNamePattern  = temp.getName();	//tables.get(tIndex).getName();
				columnNamePattern = null;

				System.out.println("Loading columns for table "+tables.get(tIndex).getName());
				if(databaseType.equals("oracle"))
					result = databaseMetaData.getColumns(
							catalog, schemaPattern,  tableNamePattern, columnNamePattern);
				else if(databaseType.equals("post"))
					result = databaseMetaData.getColumns(
							catalog, schemaPattern,  tableNamePattern.toLowerCase(), columnNamePattern);
				ResultSetMetaData rsmd = result.getMetaData();
				
				System.out.println("Loading " + rsmd.getColumnCount() + " columns");

				while(result.next()){
				    String columnName = result.getString("COLUMN_NAME");
				    int    columnType = Integer.parseInt(result.getString(5));
				    System.out.println(tables.get(tIndex).getName()+": "+columnName+", "+columnType);
				    //tables.get(tIndex).addColumn(columnName, columnType);
				    temp.addColumn(columnName, columnType);
				}

				System.out.println("Columns for "+tables.get(tIndex).getName()+" loaded\n");
				
				catalog   = null;
				String schema    = dbSchema;
				String tableName = temp.getName(); //tables.get(tIndex).getName();

				System.out.println("Loading primary keys for table "+tables.get(tIndex).getName());
				result = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);
				while(result.next()){
				    String columnName = result.getString(4);
				    //tables.get(tIndex).addPrimaryKey(columnName);
				    temp.addPrimaryKey(columnName);
				    System.out.println(tables.get(tIndex).getName()+" Primary key: "+columnName);
				}
				System.out.println("Primary keys for "+tables.get(tIndex).getName()+" loaded\n");
				
				System.out.println("Foreign keys for "+tables.get(tIndex).getName());
				result = databaseMetaData.getImportedKeys(catalog, schema, tableName);
			    while (result.next()) {
			        String fkTableName = result.getString("FKTABLE_NAME");
			        String fkColumnName = result.getString("FKCOLUMN_NAME");
			        String pkTableName = result.getString("PKTABLE_NAME");
			        String pkColumnName = result.getString("PKCOLUMN_NAME");
			        String fkName = result.getString("FK_NAME");
			        System.out.println(fkName + ": " + fkTableName + "." + fkColumnName + " -> " + pkTableName + "." + pkColumnName);
			        //tables.get(tIndex).addForeignKey(fkName + ":" + fkTableName + ":" + fkColumnName + ":" + pkTableName + ":" + pkColumnName);
			        temp.addForeignKey(fkName + ":" + fkTableName + ":" + fkColumnName + ":" + pkTableName + ":" + pkColumnName);
			    }
			    System.out.println("Foreign keys for "+tables.get(tIndex).getName()+" loaded\n");
			    
				System.out.println();
				for(int cIndex = 0; cIndex < temp.getAllColumns().size(); cIndex++){ //tables.get(tIndex).getAllColumns().size(); cIndex++){
					//result = getColumnData(conn, tables.get(tIndex).getName(), tables.get(tIndex).getColumn(cIndex).getName());
					result = getColumnData(conn, temp.getName(), temp.getColumn(cIndex).getName());
					if(result != null)
					while (result.next()) {
						//tables.get(tIndex).getColumn(cIndex).addData(result.getString(1));
						temp.getColumn(cIndex).addData(result.getString(1));
					}
				}
				printTable(temp);
				rdf.writeTable(temp);
			}
			rdf.closeWriting();
			System.out.println("Columns loaded");
			log.writeLine(stopwatch.getMili()+": Columns and data loaded");
			conn.close();
			System.out.println("Connection closed\n");
			log.writeLine(stopwatch.getMili()+": Connection closed");
			
			System.out.println("RDF conversion complete");
			log.writeLine(stopwatch.getMili()+": RDF conversion complete");
			
		} catch (SQLException e) {
			System.out.println("Connection problem");
			log.writeLine(stopwatch.getMili()+": Connection failed");
			e.printStackTrace();
		}

	}
	
	public static ResultSet getColumnData(Connection con, String table, String column) throws SQLException {
		Statement stmt = null;
		String query = "select " + column + " from " + table;

		try {
			stmt = con.createStatement();
		    ResultSet result = stmt.executeQuery(query);
		    return result;
		} catch (SQLException e ) {
			System.out.println("Error while fetching data for " + table + "." + column);
			log.writeLine(stopwatch.getMili()+": Error while loading data for " + table + "." + column);
		}
		return null;
	}
	
	public static void printDatabase(){
		for(int tIndex = 0; tIndex < tables.size(); tIndex++){
			System.out.println(tables.get(tIndex).getName()+":");
			System.out.println("*---------------------------------------------------");
			for(int cIndex = 0; cIndex < tables.get(tIndex).getAllColumns().size(); cIndex++){
				System.out.print(tables.get(tIndex).getColumn(cIndex).getName() + ": ");
				tables.get(tIndex).getColumn(cIndex).printData();
				System.out.println();
			}
			if(tables.get(tIndex).getAllPrimaryKeys().size() > 0){
				System.out.println("Primary Keys:");
				for(int i = 0; i < tables.get(tIndex).getAllPrimaryKeys().size(); i++){
					System.out.println(tables.get(tIndex).getAllPrimaryKeys().get(i));
				}
			}
			if(tables.get(tIndex).getAllForeignKeys().size() > 0){
				System.out.println("Foreign Keys:");
				for(int i = 0; i < tables.get(tIndex).getAllForeignKeys().size(); i++){
					System.out.println(tables.get(tIndex).getAllForeignKeys().get(i));
				}
			}
			
			System.out.println("---------------------------------------------------*");
		}
	}
	
	public static void printTable(DBTable table){
		System.out.println(table.getName()+":");
		System.out.println("*---------------------------------------------------");
		for(int cIndex = 0; cIndex < table.getAllColumns().size(); cIndex++){
			System.out.print(table.getColumn(cIndex).getName() + ": ");
			table.getColumn(cIndex).printData();
			System.out.println();
		}
		if(table.getAllPrimaryKeys().size() > 0){
			System.out.println("Primary Keys:");
			for(int i = 0; i < table.getAllPrimaryKeys().size(); i++){
				System.out.println(table.getAllPrimaryKeys().get(i));
			}
		}
		if(table.getAllForeignKeys().size() > 0){
			System.out.println("Foreign Keys:");
			for(int i = 0; i < table.getAllForeignKeys().size(); i++){
				System.out.println(table.getAllForeignKeys().get(i));
			}
		}
		
		System.out.println("---------------------------------------------------*");
	}

}
