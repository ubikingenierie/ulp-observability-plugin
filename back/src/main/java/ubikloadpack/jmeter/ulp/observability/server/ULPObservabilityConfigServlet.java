package ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ubikloadpack.jmeter.ulp.observability.config.PluginConfig;

/**
 * HttpServlet to expose plugin configuration
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityConfigServlet extends HttpServlet{

	private static final long serialVersionUID = 5903356626691130717L;
	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityConfigServlet.class);

	
	private final String pluginConfigJson;
	
	
	public ULPObservabilityConfigServlet(PluginConfig pluginConfig) {
		String json = "{}";
		try {
			json = new ObjectMapper().writeValueAsString(pluginConfig);
		} catch (JsonProcessingException e) {
			log.error("Unable to serialize plugin config into JSON {}",e);
		} finally {
			this.pluginConfigJson = json;
		}
	}
	
	/**
	 * Returns plugin configuration in JSON format
	 */
	@Override
	  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		  resp.setStatus(200);
		  resp.setContentType("application/json");
		    
		  try(Writer writer = new BufferedWriter(resp.getWriter())) {
			  writer.write(pluginConfigJson);
			  writer.flush();
		  }
	  }
	
	  @Override
	  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
	    doGet(req, resp);
	  }

}
