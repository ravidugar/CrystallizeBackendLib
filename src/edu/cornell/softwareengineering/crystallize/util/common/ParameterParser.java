package edu.cornell.softwareengineering.crystallize.util.common;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ParameterParser {
	public static JSONObject getParameterObject(HttpServletRequest request) throws IOException, JSONException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
		jb.append(line);

		JSONObject parameters = new JSONObject(jb.toString());
		return parameters;
	}
}
