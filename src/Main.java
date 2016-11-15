import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import tools.DBTable;

public class Main {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  	
	static String url;
	static String user     = "db2";
	static String password = "db2";
	static String serverName = "students.kiv.zcu.cz";
	static String dbName = "db2";
	static String portNumber = "3306";
	private static ArrayList<DBTable> tables;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		//Connecting to database
		/*Properties connectionProps = new Properties();
	    connectionProps.put("user", user);
	    connectionProps.put("password", password);*/
	    url = "jdbc:oracle:thin:"+user+"/"+password+"@"+serverName+/*"/"+dbName+*/":"+portNumber+":ORCL";
		try {
			
			Connection conn = DriverManager.getConnection(url);
			
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			
			//Retrieving tables
			
			String   catalog          = null;
			String   schemaPattern    = null;
			String   tableNamePattern = null;
			String[] types            = null;
			tables = new ArrayList<DBTable>();
			
			ResultSet result = databaseMetaData.getTables(
			    catalog, schemaPattern, tableNamePattern, types );

			while(result.next()) {
			    String tableName = result.getString(3);
			    tables.add(new DBTable(tableName));
			}
			
			String columnNamePattern = null;
			
			//Retrieving columns for tables in database
			for(int tIndex = 0; tIndex < tables.size(); tIndex++){
				
				catalog           = null;
				schemaPattern     = null;
				tableNamePattern  = tables.get(tIndex).getName();
				columnNamePattern = null;


				result = databaseMetaData.getColumns(
				    catalog, schemaPattern,  tableNamePattern, columnNamePattern);

				while(result.next()){
				    String columnName = result.getString(4);
				    int    columnType = result.getInt(5);
				    tables.get(tIndex).addColumn(columnName, columnType);
				}
				
				catalog   = null;
				String schema    = null;
				String tableName = tables.get(tIndex).getName();

				result = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);

				while(result.next()){
				    String columnName = result.getString(4);
				    tables.get(tIndex).addPrimaryKey(columnName);
				}
				
				for(int cIndex = 0; cIndex < tables.get(tIndex).getAllColumns().size(); cIndex++){
					result = getColumnData(conn, tables.get(tIndex).getName(), tables.get(tIndex).getColumn(cIndex).getName());
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
		String query = "select " + column + "from " + table;

		try {
			stmt = con.createStatement();
		    ResultSet result = stmt.executeQuery(query);
		    return result;
		} catch (SQLException e ) {
			System.out.println("Something went wrong");
		} finally {
			if (stmt != null) { stmt.close(); }
		}
		
		return null;
	}

}
