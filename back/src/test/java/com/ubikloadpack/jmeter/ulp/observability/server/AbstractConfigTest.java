package com.ubikloadpack.jmeter.ulp.observability;

import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;

public abstract class AbstractConfigTest {
	private static final Integer PORT = 8080;
	private static final String METRICS_ROUTE = "/";
	private static final String WEB_APP_ROUTE = "/ulp-observability";
	private static final Integer LOG_FREQUENCY = 1;
	private static final String TOTAL_LABEL = "total_info";
	
	protected ULPObservabilityServer server;
	protected SampleLogger sampleLogger;
	
	@BeforeEach
	public void startJetty() throws Exception {
		this.sampleLogger = new SampleLogger(TOTAL_LABEL);
		try (MockedStatic<JMeterUtils> utilitites = Mockito.mockStatic(JMeterUtils.class)) {
			utilitites.when(() -> JMeterUtils.getLocale()).thenReturn(new Locale(Locale.ENGLISH.getLanguage()));
			this.server = new ULPObservabilityServer(PORT, METRICS_ROUTE, WEB_APP_ROUTE, 
												 LOG_FREQUENCY, TOTAL_LABEL, sampleLogger);
		}
		this.server.start();
	}
	
	@AfterEach
	public void stopJetty() throws Exception {
		if (this.server != null) {
			this.server.stop();
		}
	}

}
