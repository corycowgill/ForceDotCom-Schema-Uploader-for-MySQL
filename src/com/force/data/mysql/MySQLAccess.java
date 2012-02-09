package com.force.data.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.sql.ResultSetMetaData;
import java.sql.DatabaseMetaData;
import com.force.data.vo.*;

import java.util.ArrayList;

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	public ArrayList<TableSchema> readDataBase(String user, String pass, String url, String db) throws Exception 
	{
		ArrayList<TableSchema> schema = new ArrayList<TableSchema>();
		try 
		{
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager
					.getConnection("jdbc:mysql://" + url + "/" + db + "?"
							+ "user=" + user + "&password=");
			DatabaseMetaData md = connect.getMetaData();
			ArrayList<String> tables = getDBTables(md);
			for(String table : tables)
			{
				TableSchema ts = getTableSchema(table, md);
				schema.add(ts);
			}
		} catch (Exception e) 
		{
			throw e;
		} finally 
		{
			close();
		}
		return schema;
	}
	
	private TableSchema getTableSchema(String tableName, DatabaseMetaData metaData) throws Exception
	{
		TableSchema returnSchema = new TableSchema();
		returnSchema.setTableName(tableName);
		ResultSet cols = metaData.getColumns(null, null, tableName, "%");
		ArrayList<FieldDefinition> fds = new ArrayList<FieldDefinition>();
		while(cols.next())
		{
			  String col_name = cols.getString("COLUMN_NAME");
			  String data_type = cols.getString("TYPE_NAME");
			  int colSize = cols.getInt("COLUMN_SIZE");
			  int precision = cols.getInt("DECIMAL_DIGITS");
			  System.out.println("ColumnName == " + col_name);
			  System.out.println("Type == " + data_type);
			  System.out.println("Length == " + colSize);
			  System.out.println("Precision == " + precision);
			  FieldDefinition fd = new FieldDefinition();
			  fd.setName(col_name);
			  fd.setType(data_type);
			  fd.setLength(colSize);
			  fd.setPrecision(precision);
			  fds.add(fd);
		}
		returnSchema.setFields(fds);
		return returnSchema;
	}
	
	private ArrayList<String> getDBTables(DatabaseMetaData metaData) throws SQLException 
	{
		ArrayList<String> tables = new ArrayList();
		String[] types = {"TABLE"};
		ResultSet rs = metaData.getTables(null, null, "%", types);
		while(rs.next())
		{
			String tableName = rs.getString("TABLE_NAME");
			tables.add(tableName);
			System.out.println(tableName);
		}
		return tables;
	}	
	
	private void writeMetaData(ResultSet resultSet) throws SQLException 
	{
		// 	Now get some metadata from the database
		// Result set get the result of the SQL query
		
		System.out.println("The columns in the table are: ");
		
		System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
		for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++)
		{
			System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
		}
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException 
	{
		while (resultSet.next()) 
		{
			String user = resultSet.getString("myuser");
			String website = resultSet.getString("webpage");
			String summery = resultSet.getString("summery");
			Date date = resultSet.getDate("datum");
			String comment = resultSet.getString("comments");
			System.out.println("User: " + user);
			System.out.println("Website: " + website);
			System.out.println("Summery: " + summery);
			System.out.println("Date: " + date);
			System.out.println("Comment: " + comment);
		}
	}

	// You need to close the resultSet
	private void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

}