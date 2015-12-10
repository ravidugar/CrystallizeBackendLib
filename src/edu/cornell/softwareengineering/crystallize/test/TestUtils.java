package edu.cornell.softwareengineering.crystallize.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestUtils {
	public static void insertObject(String table, String ID, JSONObject document) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("document", document);
		parameters.put("table", table);
		parameters.put("ID", ID);
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(TestConstants.getInsertURL(), parameters.toString());
	}
	
	public static void uploadJSON(String table, String ID, String fileName) throws JSONException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        System.out.println(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    JSONObject document = new JSONObject(sb.toString());
		    
	    	//Store item
	    	JSONObject parameters = new JSONObject();
			parameters.put("table", table);
			parameters.put("ID", ID);
			parameters.put("document", document);

			System.out.println(parameters.toString());
			
		    HTTPConnection.excutePost(TestConstants.getInsertURL(), parameters.toString());
		} finally {
		    br.close();
		}
	}
	
	public static JSONArray query(String table, JSONArray queryItems, JSONArray filters) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("table", table);
		parameters.put("query", queryItems);
		if(filters != null) parameters.put("filters", filters);
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(TestConstants.getQueryURL(), parameters.toString()));
		
		if(output.getBoolean("ok")) {
			JSONArray results = output.getJSONArray("results");
			return results;
		}
		else {
			assertTrue("Server error: " + output.getString("message"), false);
			return null;
		}
	}
	
	public static void deleteObject(String table, String ID) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("ID", ID);
		parameters.put("table", table);
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(TestConstants.getDeleteURL(), parameters.toString());
	}
}
