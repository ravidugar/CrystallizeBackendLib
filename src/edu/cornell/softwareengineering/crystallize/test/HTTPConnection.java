package edu.cornell.softwareengineering.crystallize.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPConnection {
    public static String excutePost(String targetURL, String parameters) throws IOException {
		URL urlObject = new URL(targetURL);
		HttpURLConnection con = (HttpURLConnection) urlObject.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		
		OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
		wr.write(parameters.toString());
		wr.flush();
		
		StringBuilder sb = new StringBuilder();
		int HttpResult = con.getResponseCode();
		if(HttpResult == HttpURLConnection.HTTP_OK){
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		        sb.append(line + "\n");  
		    }  
		    
		    br.close();  
		    System.out.println("Returned JSON: "+sb.toString());
		    return sb.toString();
		}else{
		    System.out.println(con.getResponseMessage());
		    return null;
		}
	}
}
