package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilityConfigServletTest extends AbstractConfigTest {
	@Test
	public void whenGetRequestExpectOkAndJsonContentType() throws Exception {	
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
	}
	
	@Test
	public void whenSendingGetRequestToConfigEndpointExpectConfigurationAsJsonFormat() throws Exception {		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
        
        String actualConfig = httpResponse.getResponse();
        String expectedConfig = String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
        									  METRICS_ROUTE, LOG_FREQUENCY, TOTAL_LABEL);
        
        assertEquals(expectedConfig, actualConfig);
	}
	
	@Test
	public void whenTotalLabelContainsSpacesExpectFormattedTotalLabelInOpenMetricsFormat() throws Exception {		
		this.listener.testEnded(HOST);
		String totalLabel = "total label";
		this.listener.setTotalLabel(totalLabel);
		this.testStarted(HOST);
		
		HttpResponse httpResponse = this.sendGetRequest("/config");
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
	    
	    String actualConfig = httpResponse.getResponse();
        String expectedConfig = String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
        									  METRICS_ROUTE, LOG_FREQUENCY, Util.makeOpenMetricsName(totalLabel));
        
        assertEquals(expectedConfig, actualConfig);
	}

}
