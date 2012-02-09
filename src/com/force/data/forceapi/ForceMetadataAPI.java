package com.force.data.forceapi;

import java.util.ArrayList;
import java.util.HashMap;

import com.force.data.vo.FieldDefinition;
import com.force.data.vo.TableSchema;
import com.sforce.soap.enterprise.DescribeGlobalResult;
import com.sforce.soap.enterprise.DescribeGlobalSObjectResult;
import com.sforce.soap.enterprise.DescribeSObjectResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Field;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.DeploymentStatus;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SharingModel;
import com.sforce.ws.ConnectorConfig;

/*
 * Purpose: This class encapsulates all the logic to make the Force.com Metadata API Calls.
 *          This includes Login and holding the Session information in the Connection objects.
 *          This class will also convert the wrapper value objects into Force.com Metadata Objects.
 */

public class ForceMetadataAPI 
{
	private ConnectorConfig connFig;
	private EnterpriseConnection entConn;
	private MetadataConnection metaConn;
	private boolean loggedIn = false;
	
	/*
	 * Method: login
	 * Purpose: This method will take in the Salesforce Login Credentials. If the user is currently logged in, we will not re-login, just return true.
	 *          If the user is not logged in, then we setup our Connections. The Connections objects are created by the Force.com Web Service Kit.
	 *          The WSC Jars can be found on Google Code at http://code.google.com/p/sfdc-wsc/
	*/
	public boolean login(String sfUser, String sfPass) throws Exception
	{
		try
		{
			if(!loggedIn) //If we arent logged in, then login
			{
				connFig = new ConnectorConfig();
				connFig.setUsername(sfUser);
				connFig.setPassword(sfPass);
				connFig.setAuthEndpoint("https://login.salesforce.com/services/Soap/c/22.0"); // Set the Login URL
		
				entConn = new EnterpriseConnection(connFig);
				LoginResult lr = entConn.login(sfUser,sfPass);
				
				connFig.setServiceEndpoint("https://na3-api.salesforce.com/services/Soap/m/23.0"); //Set the Service Endpoint. Endpoints need to be changed between Metadata API and Enterprise API
				metaConn = new MetadataConnection(connFig);
				loggedIn = true;
			}
		}catch(Exception e)
		{
			throw e;
		}
		return true;
	}
	
	/*
	 * Name: downloadSchema
	 * Purpose: This method will download the Force.com schema. We will return a wrapper object which holds the Schema Information for displaying.
	 * 
	 */
	public ArrayList<TableSchema> downloadSchema(String sfUser, String sfPass) throws Exception
	{
		ArrayList<TableSchema> cloudTableSchema = new ArrayList<TableSchema>();
		login(sfUser, sfPass);
		connFig.setServiceEndpoint("https://na3.salesforce.com/services/Soap/c/22.0");
		DescribeGlobalResult globalDesc = entConn.describeGlobal();
		DescribeGlobalSObjectResult[] sobjects = globalDesc.getSobjects();
		String[] sobjectNames = new String[sobjects.length];
		int x = 0;
		for(DescribeGlobalSObjectResult globalSObject : sobjects)
		{
			DescribeSObjectResult sobjectResult = entConn.describeSObject(globalSObject.getName());
			TableSchema tableSchema = new TableSchema();
			tableSchema.setTableName(sobjectResult.getName());
			Field[] fields = sobjectResult.getFields();
			for(Field field : fields)
			{
				FieldDefinition fd = new FieldDefinition();
				fd.setName(field.getName());
				fd.setType(field.getType().toString());
				tableSchema.getFields().add(fd);
			}
			cloudTableSchema.add(tableSchema);
		}
		
		return cloudTableSchema;
	}
	
	/*
	 * Name: uploadSchema
	 * Purpose: Upload the Metadata API to Force.com
	 */
	public ArrayList<TableSchema> uploadSchema(String sfUser, String sfPass, ArrayList<TableSchema> localTableSchema) throws Exception
	{
		login(sfUser, sfPass);
		connFig.setServiceEndpoint("https://na3-api.salesforce.com/services/Soap/m/23.0");
		System.out.println("Start Upload");
		
		ArrayList<Metadata> metadataRequest = new ArrayList();
		ArrayList<Metadata> metadataRequest2 = new ArrayList();
		for(TableSchema localTable : localTableSchema)
		{
			System.out.println("Table Name == " + localTable.getTableName());
			CustomObject cus = buildCustomObject(localTable);
			metadataRequest.add(cus);
			
			ArrayList<FieldDefinition> fields = localTable.getFields();
			for(FieldDefinition field : fields)
			{
				CustomField customField = buildCustomField(field,cus);
				metadataRequest2.add(customField);
			}
		}
		
		if(metadataRequest.size() > 0)
		{
			sendMetadataRequest(metadataRequest);
		}
		if(metadataRequest2.size() > 0)
		{
			sendMetadataRequest(metadataRequest2);
		}
		return localTableSchema;
	}
	
	
	/*
	 * Name: buildCustomObject
	 * Purpose: Create the CUstom Object based on the Schema information from MySQL
	 */
	private CustomObject buildCustomObject(TableSchema localTable)
	{
		CustomObject cus = new CustomObject();
		cus.setFullName(localTable.getTableName() + "__c");
		cus.setLabel(localTable.getTableName());
		cus.setPluralLabel(localTable.getTableName() + "s");
		
		//Each field in Force.com Needs a Name field. By Default, we are going to create AutoNumbers
		CustomField cf = new CustomField();
		cf.setType(FieldType.AutoNumber);
		cf.setLabel("AutoID");
		cus.setNameField(cf);
		cus.setDeploymentStatus(DeploymentStatus.Deployed);
		cus.setSharingModel(SharingModel.ReadWrite);
		return cus;
	}
	
	/*
	 * Name: buildCustomField
	 * Purpose: Create the metadata customfield based on the data type from MySQL
	 */
	
	private CustomField buildCustomField(FieldDefinition field, CustomObject cus)
	{
		CustomField customField = new CustomField();
		customField.setFullName(cus.getFullName() + "." + field.getName() + "__c");
		customField.setLabel(field.getName());
		if(field.getType().contains("VARCHAR"))
		{
			customField.setType(FieldType.Text);
			customField.setLength(field.getLength());
		}else if(field.getType().contains("INT"))
		{
			customField.setType(FieldType.Number);
			customField.setScale(0);
			customField.setPrecision(field.getLength());
		}else if(field.getType().contains("DECIMAL") || field.getType().contains("FLOAT"))
		{
			customField.setType(FieldType.Number);
			customField.setScale(field.getPrecision());
			customField.setPrecision(field.getLength());
		}else if(field.getType().contains("DATETIME"))
		{
			customField.setType(FieldType.DateTime);
		}else if(field.getType().contains("DATE"))
		{
			customField.setType(FieldType.Date);
		}else
		{
			customField.setType(FieldType.Text);
			customField.setLength(field.getLength());
		}
		return customField;
	}
	
	/*
	 * Name: sendMetadataRequest
	 * Purpose: This method will fire the Asynchronous Request to the Metadata API. It will retry to check on the request status every 3 seconds.
	 */
	private void sendMetadataRequest(ArrayList<Metadata> metadataRequest) throws Exception
	{
		Metadata[] reqList = new Metadata[metadataRequest.size()];
		reqList = metadataRequest.toArray(reqList);
		AsyncResult[] results = metaConn.create(reqList); // Send the request to Force.com to create the metadata
		String[] ids = new String[results.length];
		int y = 0;
		for(AsyncResult result: results)
		{
			System.out.println(result.toString());
			ids[y] = result.getId();
			System.out.println("AsyncID == " + result.getId());
			y++;
		}
		boolean done = false;
        long waitTimeMilliSecs = 3000; //Set the time to wait to query for the Metadata API Status
        AsyncResult[] arsStatus = null;
        while (!done) //Loop until the response is completed either successfully or as a failure
        {
        	Thread.sleep(waitTimeMilliSecs); //Sleep and then retry
        	
        	arsStatus = metaConn.checkStatus(ids); // Make a call to Force.com to check the status of the requests
        	if(arsStatus == null)
        	{
        		done = true;
        	}
        	if(arsStatus != null)
        	{
	        	for(AsyncResult arsResult : arsStatus)
	        	{
	        		System.out.println(arsResult.toString());
	        		if(arsResult.isDone())
	        		{
	        			done = true;
	        		}
	        	}
        	}
        }		
	}
	
}
