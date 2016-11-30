package tools;

import java.util.ArrayList;

public class DBTable {
	private ArrayList<DBColumn> columns;
	private ArrayList<String> primaryKeys;
	private ArrayList<String> foreignKeys;
	private String name;
	
	public DBTable(String name){
		columns = new ArrayList<DBColumn>();
		primaryKeys = new ArrayList<String>();
		foreignKeys = new ArrayList<String>();
		this.name = name;
	}
	
	public DBColumn getColumn(int column){
		return columns.get(column);
	}
	
	public DBColumn getColumn(String column){
		for(DBColumn col:columns){
			if(col.getName().equals(column))
				return col;
		}
		return null;
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
	
	public void addForeignKey(String column){
		foreignKeys.add(column);
	}
	
	public ArrayList<String> getAllForeignKeys(){
		return foreignKeys;
	}
	
	public void addColumn(String name, int type){
		columns.add(new DBColumn(name, type));
	}
	
	public String getName(){
		return name;
	}
	
	public ArrayList<String> getRow(int row){
		ArrayList<String> temp = new ArrayList<String>();
		for(int cIndex = 0; cIndex < columns.size(); cIndex++){
			String data = columns.get(cIndex).getData(row); 
			if(data != null) temp.add(data);

			//System.out.print(columns.get(cIndex).getData(row)+ " ");
		}
		//System.out.println();
		return temp;
	}
}
