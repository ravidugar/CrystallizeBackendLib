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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.common.io.Files;

import edu.cornell.softwareengineering.crystallize.util.common.DynamoDBClient;

public class UploadDictionary {

	public static void main(String[] args) throws JSONException, IOException {
		uploadDictionary();
	}
	public static String readFile(String path, Charset encoding) throws IOException 
    {
    	String s = Files.toString(new File(path), StandardCharsets.UTF_8);
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
	public static void uploadDictionary() throws JSONException, IOException {
		String filename = "./data/JMdict_e.xml";
		try {
		    JSONObject obj = XML.toJSONObject(readFile(filename, StandardCharsets.UTF_8));

		    JSONObject JMdict = obj.getJSONObject("JMdict");
		    JSONArray entries = JMdict.getJSONArray("entry");
		    System.out.println(entries.length());
		    
		    for(int i = 0; i < 100; i++) {
		    	System.out.println("Writing: " + i);
		    	JSONObject entry = entries.getJSONObject(i);

		    	//System.out.println(entry);
		    	if(entry.has("ent_seq")) {
		    		Item item = new Item().withPrimaryKey("WordID", String.valueOf(entry.getInt("ent_seq")));
		    		
			    	// Add English translation string
			    	if(entry.has("sense")) {
			    		List<Map<String, ?>> english = getSensesList(JSONObject.wrap(entry.get("sense")));
			    		String englishSummary = "";
			    		
			    		for(Map<String, ?> sense : english) {
			    			if(sense.containsKey("gloss") && sense.get("gloss") instanceof String) {
			    				String senseString = (String) sense.get("gloss");
				    			if(!englishSummary.equals("")) englishSummary += "; ";
				    			englishSummary += senseString;
			    			}
			    		}
			    		
			    		if(!english.isEmpty()) item.withList("English", english);
						if(!englishSummary.equals("")) item.withString("EnglishSummary", englishSummary);
						
//						if(entry.getJSONObject("sense").has("pos")) {
//							item.withString("PartOfSpeech", entry.getJSONObject("sense").getString("pos"));
//						}
			    	}
			    	
			    	// Add Kana Character list
			    	if(entry.has("r_ele")) {
			    		List<String> kanaObjects = getKanaList(JSONObject.wrap(entry.get("r_ele")));
						if(!kanaObjects.isEmpty()) {
							item.withList("Kana", kanaObjects);
							
							String kanaSummary = "";
				    		for(String kana : kanaObjects) {
					    		if(!kanaSummary.equals("")) kanaSummary += ", ";
					    		kanaSummary += kana;
				    		}
				    		item.withString("KanaSummary", kanaSummary);
						}
			    	}
			    	
			    	// Add Kana Character list
			    	if(entry.has("k_ele")) {
			    		List<String> kanjiObjects = getKanjiList(JSONObject.wrap(entry.get("k_ele")));
						if(!kanjiObjects.isEmpty()) {
							item.withList("Kanji", kanjiObjects);
							
							String kanjiSummary = "";
				    		for(String kanji : kanjiObjects) {
					    		if(!kanjiSummary.equals("")) kanjiSummary += ", ";
					    		kanjiSummary += kanji;
				    		}
				    		item.withString("KanjiSummary", kanjiSummary);
						}
			    	}
			    	
			    	System.out.println(item);
			    	//Store item
			    	Table table = DynamoDBClient.getTable("Dictionary");
			    	PutItemOutcome result = table.putItem(item);
			    	System.out.println(result);
		    	}
		    }
		    
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
	public static List<Map<String, ?>> getSensesList(Object sensesObject) throws JSONException {
		List<Map<String, ?>> sensesList = new ArrayList<Map<String, ?>>();
		JSONArray senses;
		
		Map<String, Object> newSense = new HashMap<String, Object>();
		
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
    		JSONObject sense = senses.getJSONObject(senseIndex);
    		
    		if(sense.has("gloss")) {
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
		    	}
		    	if(!senseString.equals("")) newSense.put("gloss", senseString);
    		}
    		
	    	if(sense.has("pos")) {
	    		Object POSObject = JSONObject.wrap(sense.get("pos"));
	    		JSONArray POS;
	
	    		if(POSObject instanceof JSONArray) {
	    			POS = (JSONArray) POSObject;
	    			//System.out.println(senses.toString());
	    		}
	    		else if(POSObject instanceof String) {
	    			POS = new JSONArray().put(POSObject);
	    		}
	    		else {
	    			POS = new JSONArray();
	    		}
	    		
	    		List<String> POSArray = new ArrayList<String>();
	       		for(int posIdx = 0; posIdx < POS.length(); posIdx++) {
	    			POSArray.add(POS.getString(posIdx));
	    		}
	       		
	       		newSense.put("pos", POSArray);
	    	}
    		
    		
    		sensesList.add(newSense);
    	}
    	
    	return sensesList;
	}
	
	public static List<String> getKanaList(Object kanaObject) throws JSONException {
		List<String> kanaChars = new ArrayList<String>();

		JSONArray kanaJSONArray;
		if(kanaObject instanceof JSONArray) {
			kanaJSONArray = (JSONArray) kanaObject;
		}
		else if(kanaObject instanceof JSONObject) {
			kanaJSONArray = new JSONArray().put((JSONObject) kanaObject);
		}
		else {
			kanaJSONArray = new JSONArray();
		}
		
		for(int j = 0; j < kanaJSONArray.length(); j++) {
			JSONObject newJSON = kanaJSONArray.getJSONObject(j);
			if(newJSON.has("reb")) kanaChars.add(newJSON.getString("reb"));
		}
		
		return kanaChars;
    }
	
	public static List<String> getKanjiList(Object kanjiObject) throws JSONException {
		List<String> kanjiChars = new ArrayList<String>();

		JSONArray kanjiJSONArray;
		if(kanjiObject instanceof JSONArray) {
			kanjiJSONArray = (JSONArray) kanjiObject;
		}
		else if(kanjiObject instanceof JSONObject) {
			kanjiJSONArray = new JSONArray().put((JSONObject) kanjiObject);
		}
		else {
			kanjiJSONArray = new JSONArray();
		}
		
		for(int j = 0; j < kanjiJSONArray.length(); j++) {
			JSONObject newJSON = kanjiJSONArray.getJSONObject(j);
			if(newJSON.has("keb")) kanjiChars.add(newJSON.getString("keb"));
		}
		
		return kanjiChars;
    }
}
