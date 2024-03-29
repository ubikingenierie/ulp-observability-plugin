package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilityConfigServletTest extends AbstractConfigTest {
	
	@Test
	@DisplayName("When sending GET request, expect the json format and the values of the received configuration")
	public void whenSendingGetRequestToConfigEndpointExpectConfigurationAsJsonFormat() throws Exception {		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
        
        String actualConfig = httpResponse.getResponse();
        String expectedConfig = getExpectedConfigAsJson(METRICS_ROUTE, LOG_FREQUENCY, TOP_ERRORS, TOTAL_LABEL);
        
        assertEquals(expectedConfig, actualConfig);
	}
	
	@Test
	@DisplayName("when totalLabel contains spaces expect formatted totalLabel in openMetrics format")
	public void whenTotalLabelContainsSpacesExpectFormattedTotalLabelInOpenMetricsFormat() throws Exception {		
		this.listener.testEnded(HOST); // should stop the listener started by @BeforeEach before setting the totalLabel property
		this.listener.setTotalLabel("total label");
		this.testStarted(HOST); // restart the listener 
		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
	    
	    String actualConfig = httpResponse.getResponse();
        String expectedConfig = getExpectedConfigAsJson(METRICS_ROUTE, LOG_FREQUENCY, TOP_ERRORS, Util.makeOpenMetricsName("total label"));
        
        assertEquals(expectedConfig, actualConfig);
	}
	
	@Test
	@DisplayName("when logFrequency is zero expect 30 second as default value")
	public void whenLogFrequencyIsZeroExpectDefaultValue() throws Exception {
		this.listener.testEnded(HOST); // should stop the listener started by @BeforeEach before setting the totalLabel property
		this.listener.setLogFreq(0); 
		this.testStarted(HOST); // restart the listener 
		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
	    
	    String actualConfig = httpResponse.getResponse();
        String expectedConfig = getExpectedConfigAsJson(METRICS_ROUTE, 30, TOP_ERRORS, TOTAL_LABEL); 
        
        assertEquals(expectedConfig, actualConfig);
	}
	
	private String getExpectedConfigAsJson(String metricsRoute, int logFrequency, int topErrors, String totalLabel) {
		return String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"topErrors\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
				metricsRoute, logFrequency, topErrors, totalLabel);
	}
}
