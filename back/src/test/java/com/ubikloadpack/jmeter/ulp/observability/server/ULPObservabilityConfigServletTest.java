package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

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
        String expectedConfig = "{\"metricsRoute\":\"/ulp-o-metrics\",\"logFrequency\":1,\"totalLabel\":\"total_info\",\"localeLang\":\"en\"}";
        
        assertEquals(receivedConfig, expectedConfig);
        
        listener.stopJettyServer();
        listener.testEnded();
	}
}
