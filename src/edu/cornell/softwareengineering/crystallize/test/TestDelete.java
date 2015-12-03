package edu.cornell.softwareengineering.crystallize.test;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class TestDelete {
	final static String deleteURL = "http://localhost:8080/CrystallizeBackendLib/Delete";
	
	public static void main(String[] args) throws JSONException, IOException {
		basicTest();
	}
	
	public static void deleteObject(String table, String ID) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("ID", ID);
		parameters.put("table", table);
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(deleteURL, parameters.toString());
	}
	
	public static void basicTest() throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("ID", "123");
		parameters.put("table", "Test");
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(deleteURL, parameters.toString());
	}
}
