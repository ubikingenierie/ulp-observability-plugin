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
		this.listener.testEnded(HOST);
		this.listener.setTotalLabel("total label");
		this.testStarted(HOST);
		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
	    
	    String actualConfig = httpResponse.getResponse();
        String expectedConfig = getExpectedConfigAsJson(METRICS_ROUTE, LOG_FREQUENCY, TOP_ERRORS, Util.makeOpenMetricsName("total label"));
        
        assertEquals(expectedConfig, actualConfig);
	}
	
	private String getExpectedConfigAsJson(String metricsRoute, int logFrequency, int topErrors, String totalLabel) {
		return String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"topErrors\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
				metricsRoute, logFrequency, topErrors, totalLabel);
	}
}
