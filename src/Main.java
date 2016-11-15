import java.sql.*;
import java.util.ArrayList;

import tools.DBTable;
import tools.Log;
import tools.StopWatch;

public class Main {

	static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";  	
	static String url;
	static String user     = "jschropf";
	static String password = "A15N0078P";
	static String serverName = "students.kiv.zcu.cz";
	static String portNumber = "1521";
	static String dbSchema = "JSCHROPF";
	static String serviceName = "students";
	private static ArrayList<DBTable> tables;
	static String tablePrefix = "FOTBAL_";
	
	static Log log = Log.getInstance();
	static StopWatch stopwatch = StopWatch.getInstance();
	
	public static void main(String[] args) {
		
		stopwatch.start();
		try {
			log.writeLine(stopwatch.getMili()+": Loading driver");
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			log.writeLine(stopwatch.getMili()+": Loading driver failed");
			e.printStackTrace();
		}
		
	    url = "jdbc:oracle:thin:"+user+"/"+password+"@"+serverName+/*"/"+dbName+*/":"+portNumber+":"+serviceName;
	    log.writeLine(stopwatch.getMili()+": Connecting: "+url);
		try {
			
			System.out.println("Connecting to database: " + serverName);
			Connection conn = DriverManager.getConnection(url);
			//System.out.println(conn.getSchema());
			System.out.println("Connection complete");
			log.writeLine(stopwatch.getMili()+": Successfull connection");
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			
			String   catalog          = null;
			String   schemaPattern    = dbSchema;
			String   tableNamePattern = null;
			String[] types            = null;
			tables = new ArrayList<DBTable>();
			
			log.writeLine(stopwatch.getMili()+": Loading database tables:\n");
			System.out.println("Loading Tables");
			ResultSet result = databaseMetaData.getTables(
			    catalog, schemaPattern, tableNamePattern, types );

			while(result.next()) {
			    String tableName = result.getString(3);
			    if(tableName.startsWith(tablePrefix)){
			    	tables.add(new DBTable(tableName));
			    	System.out.println(tableName);
			    }
			}
			System.out.println("\nTables loaded");
			log.writeLine(stopwatch.getMili()+": Database tables loaded");
			String columnNamePattern = null;
			
			//Retrieving columns for tables in database
			log.writeLine(stopwatch.getMili()+": Loading columns and data");
			System.out.println("\nLoading columns for tables\n");
			for(int tIndex = 0; tIndex < tables.size(); tIndex++){
				
				catalog           = null;
				schemaPattern     = dbSchema;
				tableNamePattern  = tables.get(tIndex).getName();
				columnNamePattern = null;

				System.out.println("Loading columns for table "+tables.get(tIndex).getName());
				result = databaseMetaData.getColumns(
				    catalog, schemaPattern,  tableNamePattern, columnNamePattern);
				
				while(result.next()){
				    String columnName = result.getString(4);
				    int    columnType = result.getInt(5);
				    System.out.println(tables.get(tIndex).getName()+": "+columnName+", "+columnType);
				    tables.get(tIndex).addColumn(columnName, columnType);
				}
				System.out.println("Columns for "+tables.get(tIndex).getName()+" loaded\n");
				
				catalog   = null;
				String schema    = dbSchema;
				String tableName = tables.get(tIndex).getName();

				System.out.println("Loading primary keys for table "+tables.get(tIndex).getName());
				result = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);
				while(result.next()){
				    String columnName = result.getString(4);
				    tables.get(tIndex).addPrimaryKey(columnName);
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
			        System.out.println(fkTableName + "." + fkColumnName + " -> " + pkTableName + "." + pkColumnName);
			        tables.get(tIndex).addForeignKey(fkTableName + "." + fkColumnName + " -> " + pkTableName + "." + pkColumnName);
			    }
			    System.out.println("Foreign keys for "+tables.get(tIndex).getName()+" loaded\n");
			    
				System.out.println();
				for(int cIndex = 0; cIndex < tables.get(tIndex).getAllColumns().size(); cIndex++){
					result = getColumnData(conn, tables.get(tIndex).getName(), tables.get(tIndex).getColumn(cIndex).getName());
					if(result != null)
					while (result.next()) {
						tables.get(tIndex).getColumn(cIndex).addData(result.getString(1));
					}
				}
			}
			System.out.println("Columns loaded");
			log.writeLine(stopwatch.getMili()+": Columns and data loaded");
			conn.close();
			System.out.println("Columns closed\n");
			log.writeLine(stopwatch.getMili()+": Connection closed");
			
			System.out.println("Printing database");
			printDatabase();
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
			System.out.println("Something went wrong");
			log.writeLine(stopwatch.getMili()+": Error while loading data");
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

}
