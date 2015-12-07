package edu.cornell.softwareengineering.crystallize.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TestTable {
	String deleteTableURL;
	String addTableURL;
	
	@Before 
	public void init() {
		addTableURL = TestConstants.getAddTableURL();
		System.out.println(addTableURL);
		deleteTableURL = TestConstants.getDeleteTableURL();
		System.out.println(deleteTableURL);
	}
	
	@Test
	public void testAddDeleteTable() throws JSONException, IOException {
		//Add table
		JSONObject throughput = new JSONObject();
		throughput.put("read", 5);
		throughput.put("write", 5);
		
		JSONObject key = new JSONObject();
		key.put("name", "ID");
		key.put("type", "String");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "TableTest");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		System.out.println("testAddTable: " + addTableURL);
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(addTableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == true);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Delete Table
		parameters = new JSONObject();
		parameters.put("table", "TableTest");
		System.out.println(parameters.toString());
		
		output = new JSONObject(HTTPConnection.excutePost(deleteTableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == true);
	}
	
	@Test
	public void testNoTableDeleteTable() throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("table", "xyz");
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(deleteTableURL, parameters.toString()));
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
		parameters.put("table", "TableTest");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(addTableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == false);
	}

	@Test
	public void testNonExistingType() throws JSONException, IOException {
		JSONObject throughput = new JSONObject();
		throughput.put("read", 5);
		throughput.put("write", 5);
		
		JSONObject key = new JSONObject();
		key.put("name", "ID");
		key.put("type", "Boolean");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "TableTest");
		parameters.put("key", key);
		parameters.put("throughput", throughput);
		System.out.println(parameters.toString());
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(addTableURL, parameters.toString()));
		System.out.println(output.toString());
		assertTrue(output.getBoolean("ok") == false);
	}
	
}
