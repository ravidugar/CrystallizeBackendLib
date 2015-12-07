package edu.cornell.softwareengineering.crystallize.servletcontainer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.softwareengineering.crystallize.util.Insert;
import edu.cornell.softwareengineering.crystallize.util.Tables;
import edu.cornell.softwareengineering.crystallize.util.common.ParameterParser;

public class TableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public TableServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject parameters;
		try {
			parameters = ParameterParser.getParameterObject(request);
			JSONObject refinedParams = refineParameters(parameters);
			String result = Tables.createTable(refinedParams);
			out.append(result);
		} catch (Exception e) {
			JSONObject failureJSON = new JSONObject();
			try {
				failureJSON.put("ok", false);
				failureJSON.put("message", e.getMessage());
			} catch (JSONException e1) {
				out.append(e.getMessage());
			}
			out.append(failureJSON.toString());
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private JSONObject refineParameters(JSONObject parameters) throws Exception {
		JSONObject refined = new JSONObject();
		if(parameters.length() == 0) throw new Exception("No parameters found");
		
		// check table parameter
		if(parameters.has("table")) {
			String table = parameters.getString("table");
			if (!table.equals("")) refined.put("table", table);
			else { throw new Exception("Parameter 'table' is empty"); }
		}
		else { throw new Exception("Parameter 'table' missing"); }
		
		// check key parameter
		if(parameters.has("key")) {
			JSONObject key = parameters.getJSONObject("key");
			String[] keyOps = {"String", "Number"};
			List<String> keyOpsList = Arrays.asList(keyOps);
			
			if(!key.has("name") || key.getString("name") == "") {
				throw new Exception("Parameter 'key' missing 'name' attribute");
			}
			if(!key.has("type") || !keyOpsList.contains(key.getString("type"))) {
				throw new Exception("Parameter 'type' in 'key' must be either 'String' or 'Number'");
			}
			
			refined.put("key", key);
		}
		else { throw new Exception("Parameter 'key' missing"); }
		
		
		// check key parameter
		if(parameters.has("throughput")) {
			JSONObject throughput = parameters.getJSONObject("throughput");
			if(throughput.has("read")) {
				if(throughput.getInt("read") <= 0) {
					throw new Exception("Parameter 'read' of 'throughput' must be a positive integer");
				}
			}
			else {
				throw new Exception("Parameter 'throughput' has no 'read' attribute");
			}
			if(throughput.has("write")) {
				if(throughput.getInt("write") <= 0) {
					throw new Exception("Parameter 'write' of 'throughput' must be a positive integer");
				}
			}
			else {
				throw new Exception("Parameter 'throughput' has no 'write' attribute");
			}
			refined.put("throughput", throughput);
		}
		else { throw new Exception("Parameter 'throughput' missing"); }
		return refined;
	}

}