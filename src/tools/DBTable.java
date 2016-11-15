package tools;

import java.util.ArrayList;

public class DBTable {
	private ArrayList<DBColumn> columns;
	private ArrayList<String> primaryKeys;
	private String name;
	
	public DBTable(String name){
		columns = new ArrayList<DBColumn>();
		primaryKeys = new ArrayList<String>();
		this.name = name;
	}
	
	public DBColumn getColumn(int column){
		return columns.get(column);
	}
	
	public ArrayList<DBColumn> getAllColumns(){
		return columns;
	}
	
	public void addColumn(DBColumn column){
		columns.add(column);
	}
	
	public void addPrimaryKey(String column){
		primaryKeys.add(column);
	}
	
	public ArrayList<String> getAllPrimaryKeys(){
		return primaryKeys;
	}
	
	public void addColumn(String name, int type){
		columns.add(new DBColumn(name, type));
	}
	
	public String getName(){
		return name;
	}
}
