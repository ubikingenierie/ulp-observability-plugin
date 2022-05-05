package ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ubikloadpack.jmeter.ulp.observability.metric.ULPObservabilitySample;
import ubikloadpack.jmeter.ulp.observability.metric.ULPObservabilitySampleRegistry;

public class ULPObservabilityServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3917512890727558222L;
	private ULPObservabilitySampleRegistry sampleRegistry;
	
	public ULPObservabilityServlet(ULPObservabilitySampleRegistry sampleRegistry) {
		this.sampleRegistry = sampleRegistry;
	}
	
	
	  @Override
	  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		  resp.setStatus(200);
		  String contentType = "text/plain; version=0.0.4; charset=utf-8";
		  resp.setContentType(contentType);
		    
		  try(Writer writer = new BufferedWriter(resp.getWriter())) {
			  for(ULPObservabilitySample sample : sampleRegistry.getSamples(getFilter(req))) {
				  writer.write(sample.toString()+"\n");
			  }
			  writer.write(sampleRegistry.getTotal().toString());
			  writer.flush();
		  }
	  }
	
	  @Override
	  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
	    doGet(req, resp);
	  }
	  
	  
	  private List<String> getFilter(HttpServletRequest req) {
		    String[] includedParam = req.getParameterValues("name[]");
		    if (includedParam == null) {
		      return null;
		    } else {
		      return new ArrayList<String>(Arrays.asList(includedParam));
		    }
		  }

}
