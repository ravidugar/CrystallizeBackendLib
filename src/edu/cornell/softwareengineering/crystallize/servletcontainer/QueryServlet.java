package edu.cornell.softwareengineering.crystallize.servletcontainer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.cornell.softwareengineering.crystallize.util.Query;
import edu.cornell.softwareengineering.crystallize.util.common.ParameterParser;

/**
 * Servlet implementation class QueryServlet
 */
public class QueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		JSONObject parameters;
		try {
			parameters = ParameterParser.getParameterObject(request);
			JSONObject refinedParams = refineParameters(parameters);
			String result = Query.query(refinedParams);
			out.append(result);
			
		} catch (Exception e) {
			out.append(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
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
		
		// check query parameter
		if(parameters.has("query")) {
			JSONArray queryArray = parameters.getJSONArray("query");
			if(queryArray.length() >= 1) {
				refined.put("query", queryArray);
			}
			else { throw new Exception("Parameter 'query' is empty"); }		}
		else { throw new Exception("Parameter 'query' missing"); }
		
		// check filters parameter
		if(parameters.has("filters")) {
			JSONArray filterArray = parameters.getJSONArray("filters");
			if(filterArray.length() >= 1) {
				refined.put("filters", filterArray);
			}
			else { refined.put("filters", new JSONArray()); }
		}
		else { refined.put("filters", new JSONArray()); }
		
		return refined;
	}
}
