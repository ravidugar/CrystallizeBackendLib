package edu.cornell.softwareengineering.crystallize.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TestMethods {
	public static String queryURL;
	public static String insertURL;
	public static String deleteURL;
	
	@Before
	public void init() {
		queryURL = TestConstants.getQueryURL();
		insertURL = TestConstants.getInsertURL();
		deleteURL = TestConstants.getDeleteURL();
	}
	
	@Test
	public void testInsertDelete() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
				
		//Check item properly added
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A"));
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 1);
		
		//Check item properly deleted
		TestUtils.deleteObject("Test", "1");
		results = TestUtils.query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 0);
	}
	
	@Test
	public void testNumberOperators() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("grade", 99.5));

		//Test that query matches when appropriate
		String[] operators = {"LT", "LE", "EQ", "NE", "GE", "GT"};
		double[] values = {100, 99.5, 99.5, 14, 99.5, 99};
		JSONArray query;
		for(int i = 0; i < operators.length; i++) {
			query = new JSONArray();
			
			JSONObject queryItem = new JSONObject();
			queryItem.put("attribute", "grade");
			queryItem.put("op", operators[i]);
			queryItem.put("values", new JSONArray().put(values[i]));
			query.put(queryItem);

			JSONArray results = TestUtils.query("Test", query, null);
			
			assertEquals("Returns 0 results with operator '" + operators[i] + "' on value " + values[i], 1, results.length());
		}
		
		//Test that the query does not match when not appropriate
		double[] failValues = {99, 99.4, 0, 99.5, 99.6, 100};
		for(int i = 0; i < operators.length; i++) {
			query = new JSONArray();
			
			JSONObject queryItem = new JSONObject();
			queryItem.put("attribute", "grade");
			queryItem.put("op", operators[i]);
			queryItem.put("values", new JSONArray().put(failValues[i]));
			query.put(queryItem);

			JSONArray results = TestUtils.query("Test", query, null);
			
			assertEquals("Returns >0 results with operator '" + operators[i] + "' on value " + failValues, results.length(), 0);
		}
		
		TestUtils.deleteObject("Test", "1");
	}
	
	@Test
	public void testStringOperators() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("objName", "abcdef"));

		//Test that query matches when appropriate
		String[] operators = {"CONTAINS", "BEGINS_WITH", "EQ", "NE"};
		String[] values = {"cd", "abc", "abcdef", "abcdeg"};
		
		JSONArray query;
		for(int i = 0; i < operators.length; i++) {
			query = new JSONArray();
			
			JSONObject queryItem = new JSONObject();
			queryItem.put("attribute", "objName");
			queryItem.put("op", operators[i]);
			queryItem.put("values", new JSONArray().put(values[i]));
			query.put(queryItem);

			JSONArray results = TestUtils.query("Test", query, null);
			
			assertEquals("Returns 0 results with operator '" + operators[i] + "' on value " + values, results.length(), 1);
		}
		
		//Test that query does not match when not appropriate
		String[] failValues = {"dc", "def", "abcdeg", "abcdef"};
		for(int i = 0; i < operators.length; i++) {
			query = new JSONArray();
			
			JSONObject queryItem = new JSONObject();
			queryItem.put("attribute", "objName");
			queryItem.put("op", operators[i]);
			queryItem.put("values", new JSONArray().put(failValues[i]));
			query.put(queryItem);

			JSONArray results = TestUtils.query("Test", query, null);
			
			assertEquals("Returns >0 results with operator '" + operators[i] + "' on value " + values, results.length(), 0);
		}
	}
	
	@Test
	public void testNullOperators() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("nullObject", JSONObject.NULL).put("obj", "somestring"));
		
		JSONArray query = new JSONArray();
		
		// Check successful null matching
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "nullObject");
		queryItem.put("op", "NULL");
		queryItem.put("values", new JSONArray());
		query.put(queryItem);

		JSONArray results = TestUtils.query("Test", query, null);
		
		assertEquals("Does not match null object properly", results.length(), 1);
		
		// Test successful non-null matching
		queryItem = new JSONObject();
		queryItem.put("attribute", "obj");
		queryItem.put("op", "NOT_NULL");
		queryItem.put("values", new JSONArray());
		query.put(queryItem);

		results = TestUtils.query("Test", query, null);
		
		assertEquals("Does not match non-null object properly", results.length(), 1);
	}
	
	@Test
	/*
	 * Test List operators (CONTAINS) function properly
	 */
	public void testListOperators() throws JSONException, IOException {
		JSONArray grades = new JSONArray().put("A+").put("B").put("A-");
		TestUtils.insertObject("Test", "1", new JSONObject().put("grades", grades));	
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grades");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("B"));
		
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem), null);
		
		System.out.println(results.toString());
		
		assertEquals(results.length(), 1);
		
		TestUtils.deleteObject("Test", "1");
	}
	
	@Test
	/*
	 * Test querying with a list of possible values for an attribute
	 */
	public void multiValueSingleFieldTest() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		TestUtils.insertObject("Test", "2", new JSONObject().put("grade", "B+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A").put("B"));
		
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 2);
		
		TestUtils.deleteObject("Test", "1");
		TestUtils.deleteObject("Test", "2");
	}
	
	@Test
	/*
	 * Tests multiple queries, one with a list of possible values, one with a single item
	 */
	public void multiValueMultiFieldTest() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		TestUtils.insertObject("Test", "2", new JSONObject().put("grade", "B+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A").put("B"));
		
		JSONObject queryItem2 = new JSONObject();
		queryItem2.put("attribute", "ID");
		queryItem2.put("op", "EQ");
		queryItem2.put("values", new JSONArray().put("1"));
		
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem).put(queryItem2), null);
		assertEquals(1, results.length());
		
		TestUtils.deleteObject("Test", "1");
		TestUtils.deleteObject("Test", "2");
	}
	
	@Test
	/*
	 * Tests querying with filters, such that only specified attributes are returned
	 */
	public void testFilters() throws JSONException, IOException {
		TestUtils.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A"));
		
		JSONArray filters = new JSONArray();
		filters.put("ID");
		
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem), filters);
		
		assertEquals(1, results.length());
		JSONObject obj = (JSONObject) results.get(0);
		System.out.println("LOOK HERE: " + obj.toString());
		
		assertTrue(obj.has("ID"));
		assertEquals("1", obj.getString("ID"));
		assertTrue(!obj.has("grade"));
		
		TestUtils.deleteObject("Test", "1");
	}
	
	@Test
	/*
	 * Tests that all attribute types can stored and retrieved properly
	 */
	public void testObjectFields() throws JSONException, IOException {
		JSONObject name = new JSONObject();
		name.put("firstname", "peter");
		name.put("lastname", "baker");
		
		JSONArray mixedList = new JSONArray();
		mixedList.put("A");
		mixedList.put(1234);
		mixedList.put(true);
		mixedList.put(10.1);
		mixedList.put((new JSONObject()).put("NestedTest", 5));
		mixedList.put((new JSONArray()).put(false));
		mixedList.put((Object) null);
		System.out.println(mixedList.toString());
		
		JSONObject document = new JSONObject();
		document.put("fullname", "");
		document.put("mixedList", mixedList);
		System.out.println(document.toString());
		
		TestUtils.insertObject("Test", "1", document);
		
		// Query Item
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "ID");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put("1"));

		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem), null);
		assertEquals(1, results.length());
		
		JSONObject obj = results.getJSONObject(0);
		System.out.println(obj);
		
		JSONArray objList = obj.getJSONArray("mixedList");
		
		System.out.println(objList);
		
		TestUtils.deleteObject("Test", "1");
	}

	@Test
	/*
	 * Tests a series of operations on fields of all different attributes
	 */
	public void allFeatureTest() throws JSONException, IOException {
		JSONArray mixedList = new JSONArray();
		mixedList.put("A-");
		mixedList.put(1234);
		mixedList.put(true);
		mixedList.put(10.1);
		mixedList.put((new JSONObject()).put("NestedTest", 5));
		mixedList.put((new JSONArray()).put(false));
		mixedList.put((Object) null);
		mixedList.put("B-");
		System.out.println(mixedList.toString());
		
		JSONObject document = new JSONObject();
		document.put("Example", mixedList);

		TestUtils.insertObject("Test", "123", document);
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "Example");
		queryItem.put("op", "NOT_NULL");
		queryItem.put("values", new JSONArray());
		
		JSONObject queryItem2 = new JSONObject();
		queryItem2.put("attribute", "Example");
		queryItem2.put("op", "CONTAINS");
		queryItem2.put("values", new JSONArray().put("A-"));
		
		JSONObject queryItem3 = new JSONObject();
		queryItem3.put("attribute", "ID");
		queryItem3.put("op", "EQ");
		queryItem3.put("values", new JSONArray().put("123"));
		
		JSONArray query = new JSONArray();
		query.put(queryItem);
		query.put(queryItem2);
		query.put(queryItem3);
		
		JSONArray results = TestUtils.query("Test", query, null);
		assertEquals(1, results.length());
		
		TestUtils.deleteObject("Test", "123");
	}
	
	@Test
	/*
	 * Tests inserting a player object, and querying using nested attributes
	 */
	public void testPlayers() throws JSONException, IOException {
		TestUtils.uploadJSON("Test", "12345", "./data/SimpleSheep.json");
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "ID");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put("12345"));
		
		JSONObject queryItem2 = new JSONObject();
		queryItem2.put("attribute", "PersonalData.PlayerName");
		queryItem2.put("op", "EQ");
		queryItem2.put("values", new JSONArray().put("SimpleSheep"));
		
		JSONArray results = TestUtils.query("Test", new JSONArray().put(queryItem).put(queryItem2), null);
		assertEquals(1, results.length());
		
		TestUtils.deleteObject("Test", "12345");
	}
}

