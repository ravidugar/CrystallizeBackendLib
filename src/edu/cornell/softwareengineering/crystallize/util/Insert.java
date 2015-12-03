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
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

public class Insert {
	public static String upsert(JSONObject parameters) throws Exception {
		String tableName;
		String ID;
		JSONObject document;
		try {
			tableName = parameters.getString("table");
		} catch (JSONException e) {
			throw new Exception("Attribute 'table' is not a String as anticipated");
		}
		try {
			document = parameters.getJSONObject("document");
		} catch (JSONException e) {
			throw new Exception("Attribute 'document' is not a JSON object as anticipated");
		}
		try {
			ID = parameters.getString("ID");
		} catch (JSONException e) {
			throw new Exception("Attribute 'ID' is not a String as anticipated");
		}
		
		Item item = new Item();
		
		String JSONWithoutEmptyStrings = document.toString().replaceAll(":\"\"", ":null");

		JSONObject refinedDocument = new JSONObject(JSONWithoutEmptyStrings);
		
		JSONArray keys = refinedDocument.names();
		for(int i = 0; i < keys.length(); i++) {
			String key = keys.getString(i);
			if(key.equals("ID")) continue;
			
			Object value = JSONObject.wrap(refinedDocument.get(key));
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