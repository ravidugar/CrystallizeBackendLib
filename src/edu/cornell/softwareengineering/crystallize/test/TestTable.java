package edu.cornell.softwareengineering.crystallize.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class TestTable {
	public static String tableURL = "http://localhost:8080/CrystallizeBackendLib/Table";
	
	@Test
	public void testAddTable() throws JSONException, IOException {
		JSONObject throughput = new JSONObject();
		throughput.put("read", 5);
		throughput.put("write", 5);
		
		JSONObject key = new JSONObject();
		key.put("name", "ID");
		key.put("type", "String");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "Test1");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(tableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == false);
	}
	
	@Test
	public void testNegativeThroughput() throws JSONException, IOException {
		JSONObject throughput = new JSONObject();
		throughput.put("read", -5);
		throughput.put("write", 5);
		
		JSONObject key = new JSONObject();
		key.put("name", "ID");
		key.put("type", "String");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "Test1");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(tableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == false);
	}

	@Test
	public void testNonExistingType() throws JSONException, IOException {
		JSONObject throughput = new JSONObject();
		throughput.put("read", -5);
		throughput.put("write", 5);
		
		JSONObject key = new JSONObject();
		key.put("name", "ID");
		key.put("type", "Boolean");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "Test1");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(tableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == false);
	}
	
}
