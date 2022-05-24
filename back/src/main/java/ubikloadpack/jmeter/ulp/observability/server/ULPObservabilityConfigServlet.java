package ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HttpServlet to expose plugin configuration
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityConfigServlet extends HttpServlet{

	private static final long serialVersionUID = 5903356626691130717L;
	
	/**
	 * Metrics resource route
	 */
	private final String metricsRoute;
	/**
	 * Currently assigned total label
	 */
	private final String totalLabel;
	/**
	 * Log frequency
	 */
	private final Integer freqLog;
	
	
	public ULPObservabilityConfigServlet(String metricsRoute, Integer freqLog, String totalLabel) {
		this.metricsRoute = metricsRoute;
		this.freqLog = freqLog;
		this.totalLabel = totalLabel;
	}
	
	/**
	 * Returns plugin configuration in JSON format
	 */
	@Override
	  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		  resp.setStatus(200);
		  resp.setContentType("application/json");
		    
		  try(Writer writer = new BufferedWriter(resp.getWriter())) {
			  writer.write("{\"metricsUrl\":\""+this.metricsRoute
					  +"\",\"logFreq\":"+this.freqLog
					  +",\"totalLabel\":\""+this.totalLabel+"\"}");
			  writer.flush();
		  }
	  }
	
	  @Override
	  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
	    doGet(req, resp);
	  }

}
