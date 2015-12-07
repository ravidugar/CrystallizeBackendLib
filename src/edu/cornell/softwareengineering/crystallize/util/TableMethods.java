package edu.cornell.softwareengineering.crystallize.util;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

public class TableMethods {
	public static String createTable(JSONObject parameters) throws Exception {
		String tableName;
		JSONObject key;
		JSONObject throughput;
		try {
			tableName = parameters.getString("table");
		} catch (JSONException e) {
			throw new Exception("Attribute 'table' is not a String as anticipated");
		}
		try {
			key = parameters.getJSONObject("key");
		} catch (JSONException e) {
			throw new Exception("Attribute 'key' is not a JSON object as anticipated");
		}
		try {
			throughput = parameters.getJSONObject("throughput");
		} catch (JSONException e) {
			throw new Exception("Attribute 'throughput' is not a JSON object as anticipated");
		}
		
		if(tableExists(tableName)) {
			throw new Exception("Table " + tableName + " already exists");
		}
		
		AttributeDefinition keyAttribute = new AttributeDefinition();
		keyAttribute.withAttributeName(key.getString("name"));
		if(key.getString("type").equals("String")) {
			keyAttribute.withAttributeType("S");
		}
		else if(key.getString("type").equals("Number")) {
			keyAttribute.withAttributeType("N");
		}
		else {
			throw new Exception("Parameter 'type' in 'key' must be either 'String' or 'Number'");
		}
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(keyAttribute);
		
		ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		keySchema.add(new KeySchemaElement()
				.withAttributeName(key.getString("name"))
				.withKeyType(KeyType.HASH)); //Partition key
		
		CreateTableRequest request = new CreateTableRequest()
				.withTableName(tableName)
				.withKeySchema(keySchema)
				.withAttributeDefinitions(attributeDefinitions)
				.withProvisionedThroughput(new ProvisionedThroughput()
						.withReadCapacityUnits(throughput.getLong("read"))
						.withWriteCapacityUnits(throughput.getLong("write")));
		
		DynamoDB dynamoDB = new DynamoDB(DynamoDBClient.getDynamoClient());

		try {
            System.out.println("Issuing CreateTable request for " + tableName);
            Table table = dynamoDB.createTable(request);

            System.out.println("Waiting for " + tableName
                + " to be created...this may take a while...");
            table.waitForActive();
            
        } catch (Exception e) {
            throw new Exception("CreateTable request failed for " + tableName);
        }
		
		JSONObject result = new JSONObject().put("ok", true);
		return result.toString();
	}
	
	public static String deleteTable(JSONObject parameters) throws Exception {
		String tableName;
		try {
			tableName = parameters.getString("table");
		} catch (JSONException e) {
			throw new Exception("Attribute 'table' is not a String as anticipated");
		}

		if(!tableExists(tableName)) {
			throw new Exception("Table " + tableName + " does not exist");
		}
		
		Table table = DynamoDBClient.getTable(tableName);	
		table.delete();
		
		JSONObject result = new JSONObject();
		result.put("ok", true);
		return result.toString();
	}
	
	/*
	 * Checks if a table exists in the database, waits 5 seconds then returns false if
	 * one not found. Returns true otherwise
	 * 
	 * @param tableName - name of table to check
	 * 
	 * @return true if table exists, false otherwise
	 */
	public static boolean tableExists(String tableName) {
		AmazonDynamoDBClient dynamoClient = DynamoDBClient.getDynamoClient();
			try {
				TableUtils.waitUntilExists(dynamoClient, tableName, 3000, 500);
			} catch (Exception e) {
				return false;
			}
		return true;
	}
}
