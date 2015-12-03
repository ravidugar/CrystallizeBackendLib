package edu.cornell.softwareengineering.crystallize.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestInsert {
	final static String insertURL = "http://localhost:8080/CrystallizeBackendLib/Insert";

	public static void main(String[] args) throws JSONException, IOException {	
		TestJSON();
		//		
//		JSONObject test = new JSONObject()
//			.put("int", 1)
//			.put("long", 2109841204)
//			.put("double", 1.2)
//			.put("bool", false);
//		System.out.println(test);
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
	
	public static void TestJSON() throws JSONException, IOException {
		JSONObject document = new JSONObject("{'ID':'ae1d850d-0326-4458-ab62-ae21d777a61d','Reviews':{'Reviews':[]},'KanaReviews':{'Reviews':[]},'PersonalData':{'Name':'Player','TotalPlayTimeTicks':0,'TotalPlayTime':'00:00:00','StartPlayTime':'0001-01-01T00:00:00','SurveysRequested':0,'Context':{'Elements':[{'Name':'name','Data':{'Translation':'Player','PhraseElements':[{'WordID':101,'FormID':0,'Text':'Player','RomajiText':'Player','Tags':[],'ElementType':101}]}}]}},'WordCollection':null,'PhraseStorage':null,'Tutorial':null,'Proficiency':{'ReviewExperience':0,'Phrases':0,'Words':3,'Confidence':10,'ReserveConfidence':0},'Session':{'BaseMoney':0,'ReducedMoney':-2147483648,'Mistakes':0,'MaxMistakes':0,'RestQuality':0.0,'ChestItem':null,'LegsItem':null,'TodaysCollectedWords':[],'isPromotion':false,'Confidence':10,'Position':[0.0,0.0,0.0],'Area':''},'Money':0,'ScriptType':2}");
		System.out.println(document);
		
    	//Store item
    	JSONObject parameters = new JSONObject();
		parameters.put("table", "Players");
		parameters.put("ID", "12345");
		parameters.put("document", document);

		System.out.println(parameters.toString());
		
	    HTTPConnection.excutePost(insertURL, parameters.toString());
	}

}
