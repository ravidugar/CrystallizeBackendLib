package edu.cornell.softwareengineering.crystallize.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

public class Query {
	public static String query(JSONObject parameters) throws Exception {
		String tableName;
		JSONArray query;
		JSONArray filters;
		
		try {
			tableName = parameters.getString("table");
		} catch (JSONException e) {
			throw new Exception("Attribute 'table' is not a String as anticipated");
		}
		try {
			query = parameters.getJSONArray("query");
		} catch (JSONException e) {
			throw new Exception("Attribute 'query' is not a Map as anticipated");
		}
		try {
			filters = parameters.getJSONArray("filters");
		} catch (JSONException e) {
			throw new Exception("Attribute 'filters' is not a List as anticipated");
		}
		
		AmazonDynamoDBClient dynamoDB = DynamoDBClient.getDynamoClient();
		
		// Create list of attributes to retrieve
		String filterString = "";
		for(int i = 0; i < filters.length(); i++) {
			if(!filterString.equals("")) filterString += ", ";
			filterString += filters.getString(i);
		}
		
		ScanRequest request = getScanRequest(tableName, query);
		
		if(!filterString.equals("")) request.setProjectionExpression(filterString);
		
		ScanResult result = dynamoDB.scan(request);
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		JSONArray DynamoResults = new JSONArray(items);
		JSONArray refinedResults = refineResults(DynamoResults);
		
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("ok", true);
		resultJSON.put("results", refinedResults);
		
    	return resultJSON.toString();
	}
	
	private static ScanRequest getScanRequest(String tableName, JSONArray query) throws JSONException {
		ScanRequest request = new ScanRequest(tableName);
		
		HashMap<String, AttributeValue> valueMap = new HashMap<String, AttributeValue>();
		String expression = "";
		int valueKeyID = 0;
		
		for(int i = 0; i < query.length(); i++) {
			if(expression != "") expression += " AND ";
			
			JSONObject queryItem = query.getJSONObject(i);
			String attribute = queryItem.getString("attribute");
			String operator = queryItem.getString("op");
			JSONArray values = queryItem.getJSONArray("values");
			
			expression += "(";
			for(int valueIndex = 0; valueIndex < values.length(); valueIndex++) {
				if(valueIndex > 0) expression += " OR ";
				Object value = JSONObject.wrap(values.get(valueIndex));
				String valueKey = ":value" + (valueKeyID++);
				
				if(value instanceof Double) {
					String valueString = ((Double) value).toString();
					valueMap.put(valueKey, new AttributeValue().withN(valueString));
				}
				else if(value instanceof Integer) {
					String valueString = ((Integer) value).toString();
					valueMap.put(valueKey, new AttributeValue().withN(valueString));
				}
				else if(value instanceof Long) {
					String valueString = ((Long) value).toString();
					valueMap.put(valueKey, new AttributeValue().withN(valueString));
				}
				else {
					String valueString = values.getString(valueIndex);
					valueMap.put(valueKey, new AttributeValue().withS(valueString));
				}
				
				expression += getExpression(attribute, valueKey, operator);
			}
			expression += ")";
		}
		
		request
			.withFilterExpression(expression)
			.withExpressionAttributeValues(valueMap);
		
		return request;
	}
	
	private static String getExpression(String attribute, String value, String operator) {
		//General Operators
		if (operator.equals(ComparisonOperator.EQ.toString())) 
			return attribute + " = " + value;
		else if (operator.equals(ComparisonOperator.NE.toString()))
			return attribute + " <> " + value;
		
		//Number Operators
		else if (operator.equals(ComparisonOperator.LT.toString())) 
			return attribute + " < " + value;
		else if (operator.equals(ComparisonOperator.LE.toString()))
			return attribute + " <= " + value;
		else if (operator.equals(ComparisonOperator.GT.toString()))
			return attribute + " > " + value;
		else if (operator.equals(ComparisonOperator.GE.toString()))
			return attribute + " >= " + value;
		
		//String Operators
		else if (operator.equals(ComparisonOperator.BEGINS_WITH.toString()))
			return "begins_with(" + attribute + ", " + value + ")";
		else if (operator.equals(ComparisonOperator.NOT_NULL.toString()))
			return "attribute_exists(" + attribute + ")";
		else if (operator.equals(ComparisonOperator.NULL.toString()))
			return "attribute_not_exists(" + attribute + ")";
		
		//String & Set Operators
		else if (operator.equals(ComparisonOperator.CONTAINS.toString()))
			return "contains(" + attribute + ", " + value + ")";

		//List Operators
		else if (operator.equals(ComparisonOperator.IN.toString()))
			return value + " IN " + attribute;
		
		else return "";
	}
	
	/*
	 * Iterates through all JSON object results in the items from a ScanResult
	 * 
	 * @param results - JSONArray of ScanResult items
	 * 
	 * @return JSONArray of refined items using the refineObjects function
	 */
	public static JSONArray refineResults(JSONArray results) throws JSONException {
		JSONArray refinedResults = new JSONArray();
		for (int i = 0; i < results.length(); i++) { 
			JSONObject resultItem = results.getJSONObject(i);
			JSONObject refinedItem = new JSONObject();
			for(String key : JSONObject.getNames(resultItem)) {
				refinedItem.put(key, refineObject(resultItem.getJSONObject(key)));
			}
			refinedResults.put(refinedItem);
		}
		return refinedResults;
	}
	
	/*
	 * Takes in a JSON with DynamoJSON format, which wraps values with a type String,
	 * and converts to standard JSON
	 * 
	 * @param resultItem - a single JSON object in DynamoJSON format
	 * 	ex. {"mixedList":{
	 * 			"l":[
	 * 				{"s":"A-"},
	 * 				{"n":"1234"},
	 * 				{"BOOL":true},
	 * 				{"n":"10.1"},
	 * 				{"m":{
	 * 					"NestedTest":{"n":"5"}
	 * 					}
	 * 				},
	 * 				{"l":[
	 * 					{"BOOL":false}
	 * 				]},
	 * 				{"NULL":true},
	 * 				{"s":"B-"}
	 * 			]
	 * 		}
	 * 
	 * @return Object result of extracting object from JSON, such as a JSONArray, JSONObject,
	 * 	String, Integer, Double, Boolean, or null. Also returns null if resultItem is empty
	 *  
	 *  ex. {"mixedList":[
	 *  		"A-",
	 *  		"1234",
	 *  		true,
	 *  		"10.1",
	 *  		{"NestedTest":"5"},
	 *  		[false],
	 *  		null,
	 *  		"B-"
	 *  		]
	 *  	}
	 */
	public static Object refineObject(JSONObject resultItem) throws JSONException {
		List<String> dataTypes = Arrays.asList(new String[] {"n", "s", "BOOL", "l", "m"});
		
		if(resultItem == null) return null;
		for(String key : JSONObject.getNames(resultItem)) {
			if(key.equals("NULL")) return null;
			else {
				// Check for String-AttributeValue objects					
				for(String type : dataTypes) {
					// JSONValue is a Type-Value mapping
					if(resultItem.has(type)) {
						if(type.equals("l")) {
							JSONArray list = resultItem.getJSONArray(key);
							JSONArray refinedList = new JSONArray();
							
							// Unwrap each value in list
							for(int i = 0; i < list.length(); i++) {
								refinedList.put(refineObject(list.getJSONObject(i)));
							}
							return refinedList;								
						}
						else if(type.equals("m")) {
							JSONObject obj = resultItem.getJSONObject(key);
							JSONObject refinedObj = new JSONObject();
							
							// Unwrap each value in map
							for(String objKey : JSONObject.getNames(obj)) {
								refinedObj.put(objKey, refineObject(obj.getJSONObject(objKey)));
							}
							
							return refinedObj;
							
						}
						else if(type.equals("n")) {
							String number = resultItem.getString(key);
							
							return Double.parseDouble(number);
						}
						else {
							return resultItem.get(type);
						}
					}
				}
			}
		}
		return null;
	}
}