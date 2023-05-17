package com.ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ULPObservabilityConfigServlet.class);
	
	/**
	 * Plugin configuration in JSON format.
	 */
	private final String pluginConfigJson;
	
	
	public ULPObservabilityConfigServlet(String metricsRoute, Integer logFrequeny, Integer topErrors, String totalLabel) {
		String json = "{}";
		try {
			json = new ObjectMapper()
					.writeValueAsString(
							new PluginConfig(
									metricsRoute,
									logFrequeny,
									topErrors,
									totalLabel,
									JMeterUtils.getLocale().getLanguage()
									)
							);
		} catch (JsonProcessingException e) {
			LOG.error(
					"Unable to serialize plugin config into JSON {}", e);
		} finally {
			this.pluginConfigJson = json;
		}
	}
	
	/**
	 * Returns plugin configuration in JSON format
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) 
			throws IOException {
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
	  
	class PluginConfig {
		
		private final String metricsRoute;
		private final Integer logFrequency;
		private final Integer topErrors;
		private final String totalLabel;
		private final String localeLang;

		public PluginConfig(String metricsRoute, Integer logFrequency, Integer topErrors, String totalLabel, String localeLang) {
			super();
			this.metricsRoute = metricsRoute;
			this.logFrequency = logFrequency;
			this.topErrors = topErrors;
			this.totalLabel = totalLabel;
			this.localeLang = localeLang;
		}
		
		public String getMetricsRoute() {
			return this.metricsRoute;
		}


		public Integer getLogFrequency() {
			return this.logFrequency;
		}


		public String getTotalLabel() {
			return this.totalLabel;
		}
		
		public String getLocaleLang() {
			return this.localeLang;
		}

		public Integer getTopErrors() {
			return topErrors;
		}
	}

}
