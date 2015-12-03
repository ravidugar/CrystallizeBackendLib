/**
 * 
 */
package edu.cornell.softwareengineering.crystallize.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

/**
 * @author ravidugar
 *
 */
public class Insert {
	public static String insert(JSONObject parameters) throws Exception {
		String tableName;
		String ID;
		JSONObject document;
		try {
			tableName = parameters.getString("table");
			document = parameters.getJSONObject("document");
			ID = parameters.getString("ID");
		} catch (JSONException e) {
			throw new Exception("Parameter error inside Insert class");
		}
		
		Item item = new Item().withPrimaryKey("ID", ID);
		
		JSONArray keys = document.names();
		for(int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
			Object value = JSONObject.wrap(document.get(key));
			if(value instanceof JSONArray)
				item.withJSON(key, ((JSONArray) value).toString());
			else if(value instanceof JSONObject) 
				item.withJSON(key, ((JSONObject) value).toString());
			else if(value instanceof String)
				item.withString(key, (String) value);
			else if(value instanceof Double)
				item.withDouble(key, (Double) value);
			else if(value instanceof Integer)
				item.withInt(key, (Integer) value);
			else if(value instanceof Boolean)
				item.withBoolean(key, (Boolean) value);
			else
				item.withNull(key);
		}
		
		Table table = DynamoDBClient.getTable(tableName);
		
		PutItemOutcome result = table.putItem(item);
		
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("ok", true);
		resultJSON.put("results", result);
		
    	return resultJSON.toString();
	}
	

	public static String upsert(JSONObject parameters) throws Exception {
		String tableName;
		String ID;
		JSONObject document;
		try {
			tableName = parameters.getString("table");
			document = parameters.getJSONObject("document");
			ID = parameters.getString("ID");
		} catch (JSONException e) {
			throw new Exception("Parameter error inside Insert class");
		}
		
		Item item = new Item();
		
		JSONArray keys = document.names();
		for(int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
			
			Object value = JSONObject.wrap(document.get(key));
			setItemValue(item, key, value);
		}
		
		Table table = DynamoDBClient.getTable(tableName);
		
		String updateExp = "set ";
		Map<String, String> expressionAttributeNames = new HashMap<String, String>();
		Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
		
		Iterator<Entry<String, Object>> entries = item.attributes().iterator();
		int counter = 0;
		while(entries.hasNext()) {
			if(!updateExp.equals("set ")) updateExp += ", ";
			
			Entry<String, Object> nextEntry = entries.next();
			String keyName = "#attr" + counter;
			String valName = ":val" + counter;
			
			expressionAttributeNames.put(keyName, nextEntry.getKey());
			expressionAttributeValues.put(valName, nextEntry.getValue());
			updateExp += keyName + " = " + valName;
			
			counter++;
		}

		UpdateItemOutcome result =  table.updateItem(
		    "ID",          // key attribute name
		    ID,           // key attribute value
		    updateExp, // UpdateExpression
		    expressionAttributeNames,
		    expressionAttributeValues);
		
		
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("ok", true);
		resultJSON.put("results", result);
		
    	return resultJSON.toString();
	}
	
	public static void setItemValue(Item item, String key, Object value) throws JSONException {
		if(value instanceof JSONArray) {
			JSONArray temp = (JSONArray) value;
			List<Object> valueList = new ArrayList<Object>();
			for(int j = 0; j < temp.length(); j++) {
				Item arrayItem = new Item();
				setItemValue(arrayItem, "0", temp.get(j));
				valueList.add(arrayItem.get("0"));
			}
			item.withList(key, valueList);
		}
		else if(value instanceof JSONObject) 
			item.withJSON(key, ((JSONObject) value).toString());
		else if(value instanceof String)
			item.withString(key, (String) value);
		else if(value instanceof Double)
			item.withDouble(key, (Double) value);
		else if(value instanceof Integer)
			item.withInt(key, (Integer) value);
		else if(value instanceof Boolean)
			item.withBoolean(key, (Boolean) value);
		else
			item.withNull(key);
	}
}
