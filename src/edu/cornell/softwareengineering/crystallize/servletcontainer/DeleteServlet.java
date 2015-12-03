package edu.cornell.softwareengineering.crystallize.servletcontainer;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import edu.cornell.softwareengineering.crystallize.util.Delete;
import edu.cornell.softwareengineering.crystallize.util.common.ParameterParser;

/**
 * Servlet implementation class DeleteServlet
 */
public class DeleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JSONObject parameters;
		try {
			parameters = ParameterParser.getParameterObject(request);
			JSONObject refinedParams = refineParameters(parameters);
			String result = Delete.delete(refinedParams);	
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
		
		// check ID parameter
		if(parameters.has("ID")) {
			String ID = parameters.getString("ID");
			if (!ID.equals("")) refined.put("ID", ID);
			else { throw new Exception("Parameter 'ID' is empty"); }
		}
		else { throw new Exception("Parameter 'ID' missing"); }
		
		return refined;
	}

}
