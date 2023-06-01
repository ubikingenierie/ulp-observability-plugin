package com.ubikloadpack.jmeter.ulp.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class MicrometerRegistryTest {
	private static final String TOTAL_lABEL = "total_info";
	private static final int LOG_FREQUENCY = 10; 
	private MicrometerRegistry micrometerRegistry;
	
	@BeforeEach
	public void setUp() {
		this.micrometerRegistry = new MicrometerRegistry(TOTAL_lABEL, 50, 90, 95, LOG_FREQUENCY, new SampleLogger(TOTAL_lABEL), 3000);
	}
	
	@AfterEach
	public void tearDown() {
		this.micrometerRegistry = null;
	}
	
	// Test the value of the metrics when one request of a sample is sent with one virtual user.
	@Test 
	public void testComputedMetrics_when_OneVirtualUser_and_onlyOneSampleIsSent() {
		long responseTime = 500L; // Fix response time of that sample at 500 milliseconds
		int groupThreads = 1;
		Long startTime = System.currentTimeMillis();
		Long endTime = startTime + 1000;		
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, groupThreads, 1, "sample", startTime, endTime);
		
		micrometerRegistry.addResponse(responseResult);
		Date creationDate = new Date();
		
		// Testing 
		SampleLog sampleLog = micrometerRegistry.makeLog("spl_sample", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		
		// Assertions
		// Only one sample was recorded so the value of different percentiles are the same as the value of the response time
		Map<Integer, Long> expectedPct = Map.of(50, responseTime, 90, responseTime, 95, responseTime); 
		assertSampleLogValuesWhenIntervalPeriods(sampleLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedPct, (double) 1/LOG_FREQUENCY, groupThreads);
		assertSampleLogValuesWhenIntervalPeriods(totalLabelLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedPct, (double) 1/LOG_FREQUENCY, groupThreads);	
	}
	
	@Test
	public void testSeveralRequestOfSample() {
		
	}
	
	/**
	 * Checks if the value of the metrics of the given sampleLog are the same as the expected values.
	 * @param sampleLog the sample Log for which the values ​​of the metrics will be compared to those expected.
	 * @param expectedCreationDate the expected creation date of the sample log.
	 * @param expectedSamplerCount the expected count of the sample.
	 * @param expectedSum the expected sum of response time.
	 * @param expectedAvg the expected average of response time.
	 * @param expectedMax the expected maximum response time.
	 * @param expectedErrors the expected number of the errors raised by the sample.
	 * @param expectedPcts a Map that contains for each expected percentiles it's expected value.
	 * @param expectedThroughput the expected throughput (number of requests of that sample per log frequency)
	 * @param expectedGroupThreads the expected number of virtual users of the group related to this sample.
	 */
	private void assertSampleLogValuesWhenIntervalPeriods(SampleLog sampleLog, Date expectedCreationDate, long expectedSamplerCount, long expectedSum, long expectedAvg, long expectedMax,
			 					int expectedErrors, Map<Integer, Long> expectedPcts, double expectedThroughput, int expectedGroupThreads) {
		assertNotNull(sampleLog);
		assertEquals(sampleLog.getTimeStamp(), expectedCreationDate);
		assertEquals(sampleLog.getSamplerCount(), expectedSamplerCount); // One request of that sample is created
		assertEquals(sampleLog.getError(), expectedErrors); // no errors for that sample
		
		assertTrue(sampleLog.getPct().length == 3);
		for (ValueAtPercentile pct: sampleLog.getPct()) {
			assertEquals((long) pct.value(), expectedPcts.get((int)(pct.percentile()*100))); 
		}
		
		assertEquals(sampleLog.getSum(), expectedSum);
		assertEquals(sampleLog.getAvg(), expectedAvg); 
		assertEquals(sampleLog.getMax(), expectedMax);
		assertEquals(sampleLog.getThroughput(), expectedThroughput); // throughput for an interval is the number of samples per log frequency
		assertEquals(sampleLog.getThreads(), expectedGroupThreads); 
	}
	

}
