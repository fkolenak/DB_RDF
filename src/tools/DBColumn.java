package tools;

import java.util.ArrayList;

public class DBColumn {
	private ArrayList<String> data;
	private String name;
	private int type;
	public DBColumn(String name, int type){
		data = new ArrayList<String>();
		this.name = name;
		this.type = type;
	}
	
	public void addData(String columnData){
		data.add(columnData);
	}
	
	public String getData(int row){
		return data.get(row);
	}
	
	public ArrayList<String> getData(){
		return data;
	}
	
	public String getName(){
		return name;
	}
	
	public int getType(){
		return type;
	}
	
	public void printData(){
		System.out.print("{ ");
		for(int i = 0; i < data.size(); i++){
			if(i < data.size()-1)
				System.out.print(data.get(i)+" , ");
			else
				System.out.print(data.get(i)+" }");
		}
	}
}
