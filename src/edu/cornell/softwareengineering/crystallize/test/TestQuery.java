package edu.cornell.softwareengineering.crystallize.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class TestQuery {
	final static String queryURL = "http://localhost:8080/CrystallizeBackendLib/Query";
	
	public JSONArray query(String table, JSONArray queryItems, JSONArray filters) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("table", table);
		parameters.put("query", queryItems);
		if(filters != null) parameters.put("filters", filters);
		
		JSONObject output = new JSONObject(HTTPConnection.excutePost(queryURL, parameters.toString()));
		
		if(output.getBoolean("ok")) {
			JSONArray results = output.getJSONArray("results");
			return results;
		}
		else {
			assertTrue(false);
			return null;
		}
	}
	
	@Test
	public void basicTest() throws JSONException, IOException {
		TestInsert.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
				
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A"));

		JSONArray results = query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 1);
		
		TestDelete.deleteObject("Test", "1");
	}
	
	@Test
	public void basicTest2() throws JSONException, IOException {
		TestInsert.insertObject("Test", "1", new JSONObject().put("grade", 99.5));
		
		JSONArray query = new JSONArray();
				
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put(99.5));
		query.put(queryItem);
		
		queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "LE");
		queryItem.put("values", new JSONArray().put(99.5));
		query.put(queryItem);
		
		queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "GE");
		queryItem.put("values", new JSONArray().put(99.5));
		query.put(queryItem);

		JSONArray results = query("Test", query, null);
		assertEquals(results.length(), 1);
		
		TestDelete.deleteObject("Test", "1");
	}

	@Test
	public void multiValueSingleFieldTest() throws JSONException, IOException {
		TestInsert.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		TestInsert.insertObject("Test", "2", new JSONObject().put("grade", "B+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A").put("B"));
		
		JSONArray results = query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 2);
		
		TestDelete.deleteObject("Test", "1");
		TestDelete.deleteObject("Test", "2");
	}
	
	@Test
	public void multiValueMultiFieldTest() throws JSONException, IOException {
		TestInsert.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		TestInsert.insertObject("Test", "2", new JSONObject().put("grade", "B+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A").put("B"));
		
		JSONObject queryItem2 = new JSONObject();
		queryItem2.put("attribute", "ID");
		queryItem2.put("op", "EQ");
		queryItem2.put("values", new JSONArray().put("1"));
		
		JSONArray results = query("Test", new JSONArray().put(queryItem).put(queryItem2), null);
		assertEquals(1, results.length());
		
		TestDelete.deleteObject("Test", "1");
		TestDelete.deleteObject("Test", "2");
	}
	
	@Test
	public void nestDocTest() throws JSONException, IOException {
		JSONArray grades = new JSONArray().put("A+").put("B").put("A-");
		TestInsert.insertObject("Test", "1", new JSONObject().put("grades", grades));	
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grades");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("B"));
		
		JSONArray results = query("Test", new JSONArray().put(queryItem), null);
		
		System.out.println(results.toString());
		
		assertEquals(results.length(), 1);
		
		TestDelete.deleteObject("Test", "1");
	}
	
	@Test
	public void testFilters() throws JSONException, IOException {
		TestInsert.insertObject("Test", "1", new JSONObject().put("grade", "A+"));
		
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("A"));
		
		JSONArray filters = new JSONArray();
		filters.put("ID");
		
		JSONArray results = query("Test", new JSONArray().put(queryItem), filters);
		
		assertEquals(1, results.length());
		JSONObject obj = (JSONObject) results.get(0);
		System.out.println("LOOK HERE: " + obj.toString());
		
		assertTrue(obj.has("ID"));
		assertEquals("1", obj.getString("ID"));
		assertTrue(!obj.has("grade"));
		
		TestDelete.deleteObject("Test", "1");
	}
	
	@Test
	public void testObjectFields() throws JSONException, IOException {
		JSONObject name = new JSONObject();
		name.put("firstname", "peter");
		name.put("lastname", "baker");
		
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
		document.put("fullname", name);
		document.put("mixedList", mixedList);
		System.out.println(document.toString());
		
		TestInsert.insertObject("Test", "1", document);
		
		// Query Item
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "ID");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put("1"));

		JSONArray results = query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 1);
		
		JSONObject obj = results.getJSONObject(0);
		System.out.println(obj);
		
		JSONArray objList = obj.getJSONArray("mixedList");
		
		System.out.println(objList);
		
		TestDelete.deleteObject("Test", "1");
	}
	
	@Test
	public void testResultRefine() throws JSONException, IOException {
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

		TestInsert.insertObject("Test", "1", document);
		
		// Query Item
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "ID");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put("1"));

		JSONArray results = query("Test", new JSONArray().put(queryItem), null);
		assertEquals(results.length(), 1);
		assertEquals((document.put("ID", "1")).toString(), results.getJSONObject(0).toString());
		
		TestDelete.deleteObject("Test", "1");
	}
	
	@Test
	public void testDictionary() throws JSONException, IOException {
		// Query Item
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "English");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("dance"));

		JSONArray results = query("Dictionary", new JSONArray().put(queryItem), null);
		System.out.println(results);
	}
	
	public static void allFeatureTest() throws JSONException, IOException {
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "document.grade");
		queryItem.put("op", "CONTAINS");
		queryItem.put("values", new JSONArray().put("C").put("B"));
		
		JSONObject queryItem2 = new JSONObject();
		queryItem2.put("attribute", "ID");
		queryItem2.put("op", "EQ");
		queryItem2.put("values", new JSONArray().put("123"));
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "Test");
		parameters.append("query", queryItem);
		parameters.append("query", queryItem2);
		parameters.put("filters", new JSONArray().put("document.grade").put("ID"));
		
		HTTPConnection.excutePost(queryURL, parameters.toString());
	}
	
	@Test
	public void testPlayers() throws JSONException, IOException {
		JSONObject queryItem = new JSONObject();
		queryItem.put("attribute", "ID");
		queryItem.put("op", "EQ");
		queryItem.put("values", new JSONArray().put("12345"));
		
		JSONObject parameters = new JSONObject();
		parameters.append("query", queryItem);
		parameters.put("table", "Players");
		System.out.println(parameters.toString());
		
		String results = HTTPConnection.excutePost(queryURL, parameters.toString());
		System.out.println(results);
	}

	public static void testPlayers2() throws JSONException, IOException {
		JSONObject query = new JSONObject();
		query.put("attribute", "document.PersonalData.PlayerName");
		query.put("op", "EQ");
		query.put("values", new JSONArray().put("SimpleSheep"));
		
		System.out.println(query.toString());
		
		JSONArray filters = new JSONArray();
		filters.put("document.PersonalData");
		
		JSONObject parameters = new JSONObject();
		parameters.put("table", "Players");
		parameters.append("query", query);
		parameters.put("filters", filters);
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(queryURL, parameters.toString());
	}
}

