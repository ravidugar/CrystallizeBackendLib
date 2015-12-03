package edu.cornell.softwareengineering.crystallize.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ravidugar
 *
 */
public class TestInsert {
	final static String insertURL = "http://localhost:8080/CrystallizeDynamoBackend/Insert";

	public static void main(String[] args) throws JSONException, IOException {	
		
		JSONObject test = new JSONObject()
			.put("int", 1)
			.put("long", 2109841204)
			.put("double", 1.2)
			.put("bool", false);
		System.out.println(test);
		//complexTest();
		//uploadPlayer("54321", "./data/ConcernedSheep.json");
	}
	
	public static void insertObject(String table, String ID, JSONObject document) throws JSONException, IOException {
		JSONObject parameters = new JSONObject();
		parameters.put("document", document);
		parameters.put("table", table);
		parameters.put("ID", ID);
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(insertURL, parameters.toString());
	}
	
	public static void basicTest() throws JSONException, IOException {
		JSONObject document = new JSONObject();
		document.put("grade", "A-");
		System.out.println(document.toString());
		
		insertObject("Test", "123", document);
	}
	
	public static void complexTest() throws JSONException, IOException {
		JSONObject name = new JSONObject();
		name.put("firstname", "peter");
		name.put("lastname", "baker");
		
		JSONArray grades = new JSONArray();
		grades.put("A-");
		grades.put(1234);
		grades.put(true);
		grades.put(10.1);
		grades.put((new JSONObject()).put("NestedTest", 5));
		grades.put((new JSONArray()).put(false));
		grades.put("B-");
		System.out.println(grades.toString());
		
		JSONObject document = new JSONObject();
		document.put("fullname", name);
		document.put("grades", grades);
		System.out.println(document.toString());
		
		JSONObject parameters = new JSONObject();
		parameters.put("document", document);
		parameters.put("table", "Test");
		parameters.put("ID", "001");
		System.out.println(parameters.toString());
		
		HTTPConnection.excutePost(insertURL, parameters.toString());
	}
	
	public static void uploadPlayer(String ID, String fileName) throws JSONException, IOException {
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
			parameters.put("table", "Players");
			parameters.put("ID", ID);
			parameters.put("document", document);

			System.out.println(parameters.toString());
			
		    HTTPConnection.excutePost(insertURL, parameters.toString());
		} finally {
		    br.close();
		}
	}

}
