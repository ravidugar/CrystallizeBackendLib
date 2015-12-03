package edu.cornell.softwareengineering.crystallize.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import com.google.common.io.Files;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class XMLtoJSONParser {
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    public static String TEST_XML_STRING =
        "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";
    public static String[] toReplace = new String[]{"&adj",
    		"&adv",
    		"&on",
    		"&aux",
    		"&n",
    		"&v5r",
    		"&vs",
    		"&m",
    		"&v5k",
    		"&v2m",
    		"&v1",
    		"&v2r",
    		"&v5u",
    		"&v2t",
    		"&v2h",
    		"&cop",
    		"&v2a",
    		"&v2y",
    		"&v2d",
    		"&v2k",
    		"&v2g",
    		"&v2b",
    		"&v2s",
    		"&v2z",
    		"&v2n",
    		"&v2w",
};
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		try {
			String in = "C:\\Eileen\\Programming\\CrystallizeBackend\\data\\JMdict_sample.xml";
        	String out = "C:\\Eileen\\Programming\\CrystallizeBackend\\data\\JMdict_sample.json";
        	String r = readFile(in, StandardCharsets.UTF_8);
        	System.out.println(r);
        	JSONObject xmlJSONObj = XML.toJSONObject(r);
        	//JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
        	byte[] jsonAsBytes = xmlJSONObj.toString().getBytes(StandardCharsets.UTF_8);//xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            //System.out.println("jsonAsString = "+jsonAsString);
        	writeFile(out, jsonAsBytes);
            //System.out.println(jsonAsString);
        } catch (JSONException je) {
 //           System.out.println(je.toString());
            je.printStackTrace();
        } catch (IOException e){
        	e.printStackTrace(); 
        }
		
		try {
			String path = "C:\\Eileen\\Programming\\CrystallizeBackend\\data\\JMdict_sample.json";
        	String s = readFile(path, StandardCharsets.UTF_8);
    		for(int i = 0; i<toReplace.length; i++){
                if (s.contains(toReplace[i]))
                    s = s.replace(toReplace[i], toReplace[i]+"-");
                if (s.contains(toReplace[i]+"-;"))
                    s = s.replace(toReplace[i]+"-;", toReplace[i]+";");
        	}
        	byte[] fileAsBytes = s.getBytes(StandardCharsets.UTF_8);//xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
        	//System.out.println("jsonAsString = "+jsonAsString);
        	writeFile(path, fileAsBytes);
            //System.out.println(jsonAsString);
        } catch (IOException e){
        	e.printStackTrace(); 
        }
		
	}

		    public static String readFile(String path, Charset encoding) throws IOException 
	    {
	    	String s = Files.toString(new File(path), StandardCharsets.UTF_8);
	//    	System.out.println("Read file = "+s);
	    	return s;
	    }
	    public static void writeFile(String path, String data) throws IOException 
	    {
	    	PrintStream ps = new PrintStream(path, "UTF-8");
	    	ps.println(data); 
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
	}

