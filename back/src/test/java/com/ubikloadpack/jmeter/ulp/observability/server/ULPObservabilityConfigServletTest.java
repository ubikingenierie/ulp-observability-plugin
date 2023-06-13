package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
	
//	@Test
//	public void whenLogFrequencyIsZeroExpectItsDefaultValue() throws Exception {		
//		this.listener.testEnded(HOST);
//		this.listener.setLogFreq(0);
//		
//		try (MockedStatic<JMeterUtils> utilitites = Mockito.mockStatic(JMeterUtils.class)) {
//			utilitites.when(() -> JMeterUtils.getLocale()).thenReturn(new Locale(Locale.ENGLISH.getLanguage()));
//			listener.testStarted(HOST);
//		
//			HttpResponse httpResponse = this.sendGetRequest("/config");
//			assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "application/json");
//		    
//		    String actualConfig = httpResponse.getResponse();
//		    int defaultLogFreq = 30;
//	        String expectedConfig = String.format("{\"metricsRoute\":\"%s\",\"logFrequency\":%s,\"totalLabel\":\"%s\",\"localeLang\":\"en\"}",
//	        									  METRICS_ROUTE, defaultLogFreq, TOTAL_LABEL);
//	        
//	        assertEquals(expectedConfig, actualConfig);
//		}
//	}

}
