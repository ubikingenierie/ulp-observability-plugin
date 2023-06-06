package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilityMetricServletTest extends AbstractConfigTest {
	
	@Test
	public void testDoGet() throws Exception {	
		HttpResponse httpResponse = this.sendHttpRequest(METRICS_ROUTE);
		assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
		assertEquals(httpResponse.getContentType(), "text/plain; version=0.0.4; charset=utf-8");
	}
	
	@Test
	public void testDoGet_and_checkTheOpenMetricsSentFromServer() throws Exception {	
		int groupThreads = 5, allThreads = groupThreads;
		String sampleName = "sampleTest";
		long responseTime = 1000;
		SampleEvent sampleEvent = this.createSampleEvent(sampleName, "groupe1", true, groupThreads, allThreads, responseTime);
		this.listener.sampleOccurred(sampleEvent);
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse = this.sendHttpRequest(METRICS_ROUTE);
		
		assertEquals(httpResponse.getResponseCode(), HttpStatus.OK_200);
		assertEquals(httpResponse.getContentType(), "text/plain; version=0.0.4; charset=utf-8");
		
		String openMetrics = httpResponse.getResponse();
		assertFalse(openMetrics.isBlank());
		
		// assert percentiles for one period
		Map<Integer, Long> pcts = Map.of(this.listener.getPct1(), responseTime, this.listener.getPct2(), responseTime, this.listener.getPct3(), responseTime);
		for(Entry<Integer, Long> entry: pcts.entrySet()) {
			String totalLabelPct = String.format("%s_pct{quantile=\"%s\"} %s", Util.makeOpenMetricsName(TOTAL_LABEL), entry.getKey(), entry.getValue());
			String samplePct = String.format("%s_pct{quantile=\"%s\"} %s", Util.makeOpenMetricsName(sampleName), entry.getKey(), entry.getValue());
			assertTrue(openMetrics.contains(totalLabelPct));
			assertTrue(openMetrics.contains(samplePct));
		}
		
		// assert percentiles for every period
		for(Entry<Integer, Long> entry: pcts.entrySet()) {
			String totalLabelPct = String.format("%s_pct{quantile_every_periods=\"%s\"} %s", Util.makeOpenMetricsName(TOTAL_LABEL), entry.getKey(), entry.getValue());
			String samplePct = String.format("%s_pct{quantile_every_periods=\"%s\"} %s", Util.makeOpenMetricsName(sampleName), entry.getKey(), entry.getValue());
			assertTrue(openMetrics.contains(totalLabelPct));
			assertTrue(openMetrics.contains(samplePct));
		}
		
		// assert average
		assertTrue(openMetrics.contains(Util.makeOpenMetricsName(TOTAL_LABEL) + "_avg " + responseTime));
		assertTrue(openMetrics.contains(Util.makeOpenMetricsName(TOTAL_LABEL) + "_avg_every_periods " + responseTime));
		assertTrue(openMetrics.contains("spl_" + Util.makeOpenMetricsName(sampleName) + "_avg " + responseTime));
		assertTrue(openMetrics.contains("spl_" + Util.makeOpenMetricsName(sampleName) + "_avg_every_periods " + responseTime));
		
		// assert threads count
		assertTrue(openMetrics.contains(Util.makeOpenMetricsName(TOTAL_LABEL) + "_threads " + groupThreads));
		assertTrue(openMetrics.contains(Util.makeOpenMetricsName(TOTAL_LABEL) + "_threads_every_periods " + allThreads));
		assertTrue(openMetrics.contains("spl_" + Util.makeOpenMetricsName(sampleName) + "_threads " + groupThreads));
		assertTrue(openMetrics.contains("spl_" + Util.makeOpenMetricsName(sampleName) + "_threads_every_periods " + allThreads));
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
