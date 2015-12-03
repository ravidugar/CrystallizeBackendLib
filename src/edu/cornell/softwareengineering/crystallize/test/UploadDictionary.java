package edu.cornell.softwareengineering.crystallize.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.io.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

/**
 * @author ravidugar
 *
 */
public class UploadDictionary {

	public static void main(String[] args) throws JSONException, IOException {
		uploadDictionary(10);
	}
	public static String readFile(String path, Charset encoding) throws IOException 
    {
    	String s = Files.toString(new File(path), StandardCharsets.UTF_8);
//    	System.out.println("Read file = "+s);
    	return s;
    }
	public static void writeFile(String path, byte[] data) throws IOException 
    {
    	FileOutputStream stream = new FileOutputStream(path);
    	try {
    	    stream.write(data);
    	} finally {
    	    stream.close();
    	}
    }
	public static void uploadDictionary(int entriesCount) throws JSONException, IOException {
		String filename = "./data/JMdict_e.xml";
		try {
		    JSONObject obj = XML.toJSONObject(readFile(filename, StandardCharsets.UTF_8));

		    JSONObject JMdict = obj.getJSONObject("JMdict");
		    JSONArray entries = JMdict.getJSONArray("entry");
		    
		    long beginTime = System.currentTimeMillis();
		    
		    List<Item> itemList = new ArrayList<Item>();
//		    for(int i = 0; i < entries.length(); i++) {
		    for(int i = 0; i < entriesCount; i++) {
		    	JSONObject entry = entries.getJSONObject(i);
		    	//System.out.println(entry.toString());
		    	
		    	if(entry.has("ent_seq")) {
		    		Item item = new Item().withPrimaryKey("WordID", String.valueOf(entry.getInt("ent_seq")));
		    		
			    	// Add English translation string
			    	if(entry.has("sense")) {
			    		String sensesString = getSensesString(JSONObject.wrap(entry.get("sense")));
						if(!sensesString.equals("")) item.withString("English", sensesString);

			    	}   	
			    	
			    	// Add Kana Character list
			    	if(entry.has("r_ele")) {
			    		List<Map<String, String>> kanaObjects = getKanaList(JSONObject.wrap(entry.get("r_ele")));
						if(!kanaObjects.isEmpty()) item.withList("Kana", kanaObjects);
			    	}
			    	
			    	//Store item
			    	Table table = DynamoDBClient.getTable("Dictionary");
			    	itemList.add(item);
//			    	System.out.println(item.toString());
			    	table.putItem(item);
		    	}
		    }
//		    DynamoDB dynamoDB = new DynamoDB(DynamoDBClient.getDynamoClient());
//		    
//			TableWriteItems batchWriteRequest = new TableWriteItems("Dictionary")
//				.withItemsToPut(itemList);
//			
//		    BatchWriteItemOutcome result = dynamoDB.batchWriteItem(batchWriteRequest);
//		    System.out.println(result.toString());
		    System.out.println("Duration: " + (System.currentTimeMillis() - beginTime));
		    
		}
		catch (IOException e){
        	e.printStackTrace(); 
        }
	}	
	
	/*
	 *  Create string of English translations, separated into senses and different sense translations. 
	 *  Ignores all fields other than "sense" and "gloss"
	 *  
	 *  @param sensesObject object from JSON
	 *  	EX. "sense":[{"gloss":["personality","individuality"],"pos":"&n;"},{"gloss":["TV personality","celebrity"]}]
	 *	@return String with sense definitions separated by "," and senses
	 *			separated by "|"
	 *		EX. "personality, individuality | TV personality, celebrity"
	 */
	public static String getSensesString(Object sensesObject) throws JSONException {
    	String sensesString = "";
		JSONArray senses;
		
		// sense attribute can be a single object or list of objects
		if(sensesObject instanceof JSONArray) {
			senses = (JSONArray) sensesObject;
		}
		else if(sensesObject instanceof JSONObject) {
			senses = new JSONArray().put((JSONObject) sensesObject);
		}
		else {
			senses = new JSONArray();
		}
    	for(int senseIndex = 0; senseIndex < senses.length(); senseIndex++) {
    		if(!sensesString.equals("")) sensesString += " | ";
    		
    		JSONObject sense = senses.getJSONObject(senseIndex);
    		
    		Object glossObject = JSONObject.wrap(sense.get("gloss"));
    		JSONArray glosses;

    		if(glossObject instanceof JSONArray) {
    			glosses = (JSONArray) glossObject;
    			//System.out.println(senses.toString());
    		}
    		else if(glossObject instanceof String) {
    			glosses = new JSONArray().put(glossObject);
    		}
    		else { 
    			glosses = new JSONArray();
    		}
	    	
    		// Each sense can have multiple English translations
	    	String senseString = "";
	    	for(int glossIndex = 0; glossIndex < glosses.length(); glossIndex++) {
	    		if(!senseString.equals("")) senseString += ", ";
	    		Object value = glosses.get(glossIndex);
	    		
	    		if(value instanceof String) {
	    			if(value != null) senseString += glosses.getString(glossIndex);
	    		}
	    		else {
	    			System.out.println(value.toString());
	    		}
	    	}
	    	sensesString += senseString;
    	}
    	
    	return sensesString;
	}
	
	public static List<Map<String, String>> getKanaList(Object kanaObject) throws JSONException {
		List<Map<String, String>> kanaObjects = new ArrayList<Map<String, String>>();

		JSONArray kana;
		
		if(kanaObject instanceof JSONArray) {
			kana = (JSONArray) kanaObject;
		}
		else if(kanaObject instanceof JSONObject) {
			kana = new JSONArray().put((JSONObject) kanaObject);
		}
		else {
			kana = new JSONArray();
		}
		
		for(int j = 0; j < kana.length(); j++) {
			JSONObject newJSON = kana.getJSONObject(j);
			Map<String, String> newObj = new HashMap<String, String>();
			for(String name : JSONObject.getNames(newJSON)) {
				if(name.equals("reb")) newObj.put(name, newJSON.getString(name));
				//System.out.println(newJSON.getString(name));
				byte[] bytes = newJSON.getString(name).getBytes(StandardCharsets.UTF_8);
				System.out.println(new String(bytes));
			}
			kanaObjects.add(newObj);
		}
		
		return kanaObjects;
    }
}
