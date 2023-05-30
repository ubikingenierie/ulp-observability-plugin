package com.ubikloadpack.jmeter.ulp.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ULPObservabilityConfigServletTest extends AbstractConfigTest{
	@Test
	public void testDoGet() throws MalformedURLException, IOException {	
		HttpURLConnection http = (HttpURLConnection)new URL("http://localhost:8080/config").openConnection();
        http.connect();
        assertEquals(http.getResponseCode(), HttpStatus.OK_200);
        assertEquals(http.getContentType(), "application/json");
	}
	

}
