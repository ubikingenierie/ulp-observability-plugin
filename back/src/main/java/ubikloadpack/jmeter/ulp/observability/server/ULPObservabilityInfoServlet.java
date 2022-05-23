package ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HttpServlet to expose server info (metrics URL and log frequency)
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityInfoServlet extends HttpServlet{

	private static final long serialVersionUID = 5903356626691130717L;
	
	private final String metricsRoute;
	private final Integer freqLog;
	
	
	public ULPObservabilityInfoServlet(String metricsRoute, Integer freqLog) {
		this.metricsRoute = metricsRoute;
		this.freqLog = freqLog;
	}
	
	
	@Override
	  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		  resp.setStatus(200);
		  resp.setContentType("application/json");
		    
		  try(Writer writer = new BufferedWriter(resp.getWriter())) {
			  writer.write("{\"metricsUrl\":\""+this.metricsRoute+"\",\"logFreq\":"+this.freqLog+"}");
			  writer.flush();
		  }
	  }
	
	  @Override
	  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
	    doGet(req, resp);
	  }

}
