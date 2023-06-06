package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class ULPObservabilityMetricServletTest extends AbstractConfigTest {
	
	@Test
	public void testDoGet() throws Exception {	
		HttpResponse httpResponse = this.sendHttpRequest(METRICS_ROUTE);
		assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
		assertEquals(httpResponse.getContentType(), "text/plain; version=0.0.4; charset=utf-8");
	}
	
	@Test
	public void testDoGet_and_checksTheContentOfTheResponse() throws Exception {	
		SampleEvent sampleEvent = this.createSampleEvent("sample", "groupe1", true, 5, 5, 1000);
		this.listener.sampleOccurred(sampleEvent);
		
		HttpResponse httpResponse = this.sendHttpRequest(METRICS_ROUTE);
		assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
		assertEquals(httpResponse.getContentType(), "text/plain; version=0.0.4; charset=utf-8");
		
		String openMetrics = httpResponse.getResponse();
	}
	
	/**
	 * Create a SampleEvent
	 * @param sampleLabel the name of the sample.
	 * @param threadGroupName the name of the thread group which the the sample event is triggered from.
	 * @param isSuccessful indicates whether the sample successes or not
	 * @param groupThreads the number of the group threads
	 * @param allThreads the number of all the threads
	 * @param responseTime the time taken by the sample to finish in milliseconds.
	 * @return an instance of the SampleEvent depending on the given parameters. 
	 */
	private SampleEvent createSampleEvent(String sampleLabel, String threadGroupName,  boolean isSuccessful, int groupThreads, int allThreads, long responseTime) {
		SampleResult sampleResult = new SampleResult();
		sampleResult.setAllThreads(allThreads);
		sampleResult.setSuccessful(isSuccessful);
		sampleResult.setSampleLabel(sampleLabel);
		sampleResult.setGroupThreads(groupThreads);
		sampleResult.sampleStart(); // this set the startTime to currentMillisecond
		sampleResult.setEndTime(sampleResult.getStartTime() + responseTime);
		
		return new SampleEvent(sampleResult, threadGroupName);
	}
}
