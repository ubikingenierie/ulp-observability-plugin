package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilityConfigServletTest extends AbstractConfigTest {
	@Test
	public void testDoGet() throws Exception {					
		HttpResponse httpResponse = this.sendGetRequest("/config");
        
        assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
        assertEquals(httpResponse.getContentType(), "application/json");
	}
	
	@Test
	public void testConfiguationContent() throws Exception {		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		
        assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
        assertEquals(httpResponse.getContentType(), "application/json");
        
        String receivedConfig = httpResponse.getResponse();
        String expectedConfig = String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
        									  METRICS_ROUTE, LOG_FREQUENCY, TOTAL_LABEL);
        
        assertEquals(receivedConfig, expectedConfig);
	}
	
	@Test
	public void testConfiguationContent_when_totalLabelContainsSpaces_so_shouldBeFormattedToOpenMetrics() throws Exception {		
		this.listener.testEnded();
		String totalLabel = "total label";
		this.listener.setTotalLabel(totalLabel);
		this.testStarted(HOST);
		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		
		assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
	    assertEquals(httpResponse.getContentType(), "application/json");
	    
	    String receivedConfig = httpResponse.getResponse();
        String expectedConfig = String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
        									  METRICS_ROUTE, LOG_FREQUENCY, Util.makeOpenMetricsName(totalLabel));
        
        assertEquals(receivedConfig, expectedConfig);
	}
}
