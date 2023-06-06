package com.ubikloadpack.jmeter.ulp.observability.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ubikloadpack.jmeter.ulp.observability.listener.ULPObservabilityListener;

public abstract class AbstractConfigTest {
	protected static final Integer PORT = 9595;
	protected static final String METRICS_ROUTE = "/ulp-o-metrics";
	protected static final String WEB_APP_ROUTE = "/ulp-observability";
	protected static final Integer LOG_FREQUENCY = 1;
	protected static final String TOTAL_LABEL = "total_info";
	
	protected URL endpoint;
	protected ULPObservabilityListener listener;
	
	@BeforeEach
	public void setUp() throws Exception {
		this.endpoint =  new URL("http://localhost:" + PORT);
		
		listener = new ULPObservabilityListener();
		listener.setJettyPort(PORT);
		listener.setMetricsRoute(METRICS_ROUTE);
		listener.setWebAppRoute(WEB_APP_ROUTE);
		listener.setPct1(50);
		listener.setPct2(90);
		listener.setPct3(95);
		listener.setThreadSize(5);
		listener.setBufferCapacity(30000);
		listener.setMicrometerExpiryTimeInSeconds("3600");
		listener.setLogFreq(LOG_FREQUENCY);
		listener.setTotalLabel(TOTAL_LABEL);
		try (MockedStatic<JMeterUtils> utilitites = Mockito.mockStatic(JMeterUtils.class)) {
			utilitites.when(() -> JMeterUtils.getLocale()).thenReturn(new Locale(Locale.ENGLISH.getLanguage()));
			
			listener.testStarted("localhost");
		}
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		listener.stopJettyServer();
        listener.testEnded();
	}
	
	protected HttpResponse sendHttpRequest(String uri) throws MalformedURLException, IOException, UnsupportedEncodingException {
		return this.sendHttpRequest(uri, null);
	}
	
	protected HttpResponse sendHttpRequest(String uri, Map<String, String> paramsMap) throws MalformedURLException, IOException, UnsupportedEncodingException {
		StringBuilder url = new StringBuilder();
		url.append(uri);
		url.append(this.formatURLParams(paramsMap));
		
		HttpURLConnection http = null;	
		try {
			http = (HttpURLConnection) new URL(this.endpoint, url.toString()).openConnection();	
        	http.connect();    
        	return new HttpResponse(http.getResponseCode(), http.getContentType(), getServerResponse(http));
		}
		finally {
            if (http != null) {
            	http.disconnect();
            }
        }
	}
	
	private String getServerResponse(HttpURLConnection http) throws IOException {
		InputStreamReader reader = new InputStreamReader(http.getInputStream());
		BufferedReader br = new BufferedReader(reader);
		String str;
		
		List<String> lines = new ArrayList<>();
	    while ((str = br.readLine()) != null) {
	    	lines.add(str);
	    }  
	    
	    reader.close();
	    br.close();
	    
	    return String.join("\n", lines);
	}
	
	private String formatURLParams(Map<String, String> paramsMap) {
		StringBuilder paramsStr = new StringBuilder();
		if (paramsMap != null && !paramsMap.isEmpty()) {
			paramsStr.append("?");
			String params = paramsMap.entrySet()
									 .stream()
									 .map(e -> {
									    try {
										   return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
										} catch (UnsupportedEncodingException e1) {
											throw new RuntimeException(e1); // the exception is ignored because the UTF-8 is a supported character encoding.
										}
								      })
									 .collect(Collectors.joining("&"));
			paramsStr.append(params);
		}
		return paramsStr.toString();
	}
	
	protected class HttpResponse {
		private int responseCode;
		private String contentType;
		private String response;
		
		public HttpResponse(int responseCode, String contentType, String response) {
			super();
			this.responseCode = responseCode;
			this.contentType = contentType;
			this.response = response;
		}

		protected int getResponseCode() {
			return responseCode;
		}

		protected String getContentType() {
			return contentType;
		}

		protected String getResponse() {
			return response;
		}
		
	}

}
