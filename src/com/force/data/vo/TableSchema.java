package com.force.data.vo;
import java.util.ArrayList;

public class TableSchema 
{
	public TableSchema()
	{
		this.tableName = "UnDefined";
		this.fields = new ArrayList<FieldDefinition>();
	}
	
	private String tableName;
	private ArrayList<FieldDefinition> fields;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public ArrayList<FieldDefinition> getFields() {
		return fields;
	}
	public void setFields(ArrayList<FieldDefinition> fields) {
		this.fields = fields;
	}
}
