import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import tools.DBTable;

public class Main {

	static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";  	
	static String url;
	static String user     = "jschropf";
	static String password = "A15N0078P";
	static String serverName = "students.kiv.zcu.cz";
	static String dbName = "db2";
	static String portNumber = "1521";
	static String dbSchema = "JSCHROPF";
	private static ArrayList<DBTable> tables;
	static String tablePrefix = "FOTBAL_";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
	    url = "jdbc:oracle:thin:"+user+"/"+password+"@"+serverName+/*"/"+dbName+*/":"+portNumber+":students";
		try {
			
			System.out.println("Connecting to db");
			Connection conn = DriverManager.getConnection(url);
			System.out.println(conn.getSchema());
			System.out.println("Connection complete");
			
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			
			String   catalog          = null;
			String   schemaPattern    = dbSchema;
			String   tableNamePattern = null;
			String[] types            = null;
			tables = new ArrayList<DBTable>();
			
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
			String columnNamePattern = null;
			
			//Retrieving columns for tables in database
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
				System.out.println("Primary keys for "+tables.get(tIndex).getName()+" loaded");
				
				System.out.println();
				for(int cIndex = 0; cIndex < tables.get(tIndex).getAllColumns().size(); cIndex++){
					result = getColumnData(conn, tables.get(tIndex).getName(), tables.get(tIndex).getColumn(cIndex).getName());
					if(result != null)
					while (result.next()) {
						tables.get(tIndex).getColumn(cIndex).addData(result.getString(1));
					}
				}
			}
			
			conn.close();
		} catch (SQLException e) {
			System.out.println("Connection problem");
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
		}
		return null;
	}

}
