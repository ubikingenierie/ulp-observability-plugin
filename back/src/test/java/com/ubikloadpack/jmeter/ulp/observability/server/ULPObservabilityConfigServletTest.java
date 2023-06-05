package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class ULPObservabilityConfigServletTest extends AbstractConfigTest{
	@Test
	public void testDoGet() throws IOException, URISyntaxException, InterruptedException {					
		HttpURLConnection http = (HttpURLConnection) new URL(this.endpoint, "/config").openConnection();
        http.connect();
        assertEquals(http.getResponseCode(), HttpStatus.OK_200);
        assertEquals(http.getContentType(), "application/json");
	}
	
	@Test
	public void testConfiguationContent() throws MalformedURLException, IOException {		
		HttpURLConnection http = (HttpURLConnection) new URL(this.endpoint, "/config").openConnection();
        http.connect();
        assertEquals(http.getResponseCode(), HttpStatus.OK_200);
        assertEquals(http.getContentType(), "application/json");
        
        String receivedConfig = getServerResponse(http);
        String expectedConfig = "{\"metricsRoute\":\"/ulp-o-metrics\",\"logFrequency\":1,\"totalLabel\":\"total_info\",\"localeLang\":\"en\"}";
        
        assertEquals(receivedConfig, expectedConfig);
        
        listener.stopJettyServer();
        listener.testEnded();
	}
	
	private String getServerResponse(HttpURLConnection http) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));
		String str;
		List<String> lines = new ArrayList<>();
	    while ((str = br.readLine()) != null) {
	    	lines.add(str);
	    }  
	    return String.join("\n", lines);
	}
	

}
