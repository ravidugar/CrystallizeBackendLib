package edu.cornell.softwareengineering.crystallize.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

public class Delete {
	public static String delete(JSONObject parameters) throws Exception {
		String tableName;
		String ID;
		try {
			tableName = parameters.getString("table");
		} catch (JSONException e) {
			throw new Exception("Attribute 'table' is not a String as anticipated");
		}
		try {
			ID = parameters.getString("ID");
		} catch (JSONException e) {
			throw new Exception("Attribute 'ID' is not a String as anticipated");
		}
		
		Table table = DynamoDBClient.getTable(tableName);
		
		DeleteItemOutcome result = table.deleteItem("ID", ID);
		
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("ok", true);
		resultJSON.put("results", result);
		
    	return resultJSON.toString();
	}
}
