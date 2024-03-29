package com.ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;

/**
 * HttpServlet to expose sample metrics in OpenMetrics format
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityMetricsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3917512890727558222L;
	
	/**
     * Sample record logger.
     */
	private SampleLogger logger;
	
	/**
	 * Create new servlet with unbound empty logger
	 */
	public ULPObservabilityMetricsServlet(SampleLogger logger) {
		this.logger = logger;
	}	
	
	
	 /**
	 * Retrieves sample logs and returns them in form of OpenMetrics format summary plain text. By default retrieves all last period logs.
	 */
	@Override
	  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		  resp.setStatus(200);
		  String contentType = "text/plain; version=0.0.4; charset=utf-8";
		  resp.setContentType(contentType);
		    
		  try(Writer writer = new BufferedWriter(resp.getWriter())) {
			  writer.write(this.logger.openMetrics(getFilter(req), this.all(req)));
			  writer.flush();
		  }
	  }
	
	  @Override
	  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
	    doGet(req, resp);
	  }
	  
	  
	/**
	 * Retrieve sample filter
	 * 
	 * @param req HTTP request data
	 * @return List of sample names to filter, empty list if parameter not found
	 */
	private List<String> getFilter(HttpServletRequest req) {
		String[] includedParam = req.getParameterValues("name[]");
		return includedParam == null ?  new ArrayList<String>() : new ArrayList<String>(Arrays.asList(includedParam));
	}
	
	/**
	 * Check if user wants to retrieve the sample metrics of all periods
	 * 
	 * @param req HTTP request data
	 * @return 
	 */
	private Boolean all(HttpServletRequest req) {
		return req.getParameter("all") != null;
	}

}
