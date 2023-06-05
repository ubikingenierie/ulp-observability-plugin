package com.ubikloadpack.jmeter.ulp.observability.registry;

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
		this.micrometerRegistry.close();
	}
	
	
	/**
	 * Test the metrics values when a single request of a sample is sent by one virtual user. 
	 * Note: this test only considers one logging period.
	 */
	@Test 
	public void testComputedMetrics_when_oneVirtualUser_and_onlyOneSample() {
		// ### SetUp ###
		int groupThreads = 1;
		long startTime = 0; // millisecond
		long endTime = 1000; // millisecond
		long responseTime = endTime - startTime; 
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, groupThreads, 1, "sample", startTime, endTime);
		
		micrometerRegistry.addResponse(responseResult);
		Date creationDate = new Date();
		
		// ### Testing ###
		SampleLog sampleLog = micrometerRegistry.makeLog("spl_sample", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		
		// ### Assertions ###
		// Only one sample was recorded so the value of different percentiles are the same as the value of the response time
		Map<Integer, Long> expectedPcts = Map.of(50, responseTime, 90, responseTime, 95, responseTime);
		for (ValueAtPercentile pct: sampleLog.getPctTotal()) {
			assertEquals((long) pct.value(), expectedPcts.get((int)(pct.percentile()*100))); 
		}
		
		double expectedThroughput = (double) 1/LOG_FREQUENCY; // number of requests of a sample per the log period
		
		// Assert the value of metrics for a log period
		assertPeriodMetricsForSampleLog(sampleLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertPeriodMetricsForSampleLog(totalLabelLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
		
		// Assert the value of metrics for a every periods
		expectedThroughput = groupThreads / millisToSeconds(endTime - startTime);
		assertEveryPeriodsMetricsForSampleLog(sampleLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertEveryPeriodsMetricsForSampleLog(totalLabelLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
	}
	
	@Test
	public void testSeveralRequestsOfASingleSample() {
		int groupThreads = 10;
		
		long startTime = 0, endTime = 0, responseTime = 0, expectedSum = 0; 
		// generate 10 samples with theses response times: {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000};
		for (int i = 0; i < groupThreads; i++) {	// we add the response of 10 sequential queries of a sample.	
			startTime = i;
			endTime = startTime + 500 * (i + 1); // endTime is increased by 1000 milliseconds
			responseTime = endTime - startTime;
			expectedSum += responseTime;
			
			ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample", startTime, endTime);
			micrometerRegistry.addResponse(responseResult);
		}
		
		Date creationDate = new Date();
		// ### Testing ###
		// making two logs, one for the sample and the other one for the totalLabel
		SampleLog sampleLog = micrometerRegistry.makeLog("spl_sample", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		
		// ### Assertions ###
		// Only one sample was recorded so the value of different percentiles are the same as the value of the response time
		double expectedThroughput = (double) groupThreads/LOG_FREQUENCY; // we have 10 threads 
		double expectedAvg = 2750D; // the mean of {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000}
		
		// Assert the value of metrics for a log period
		assertPeriodMetricsForSampleLog(sampleLog, creationDate, groupThreads, expectedSum, expectedAvg, responseTime, 0, expectedThroughput, groupThreads);
		assertPeriodMetricsForSampleLog(totalLabelLog, creationDate, groupThreads, expectedSum, expectedAvg, responseTime, 0, expectedThroughput, groupThreads);	
		
		// Assert the value of metrics for a every periods
		expectedThroughput = groupThreads / millisToSeconds(endTime); // startTime is equal to zero so endTime - startTime = endTime
		assertEveryPeriodsMetricsForSampleLog(sampleLog, creationDate, groupThreads,  expectedAvg, responseTime, 0, expectedThroughput, groupThreads);
		assertEveryPeriodsMetricsForSampleLog(totalLabelLog, creationDate, groupThreads, expectedAvg, responseTime, 0, expectedThroughput, groupThreads);	
	}
	
	@Test
	public void testTwoRequestsOfASingleSample_on_severalLogPeriods() {
		int groupThreads = 1;
		 
		// *** First sample for the first log period ***
		long startTime1 = 0, endTime1 = 500, responseTime1 = endTime1;
		ResponseResult responseResult1 = new ResponseResult("groupe1", responseTime1, false, groupThreads, groupThreads, "sample", startTime1, endTime1);
		
		micrometerRegistry.addResponse(responseResult1);
		Date creationDate1 = new Date(); // the timestamp for the first log
		SampleLog sampleLog1 = micrometerRegistry.makeLog("spl_sample", creationDate1);
		
		double expectedThroughput = (double) groupThreads/LOG_FREQUENCY;
		long expectedSum1 = responseTime1, expectedAvg1 = responseTime1, expectedMax1 = responseTime1;
		
		// check the metrics for the log period. There's only one sample so the sum, average and the max are the same as the responseTime of the sample
		assertPeriodMetricsForSampleLog(sampleLog1, creationDate1, groupThreads, expectedSum1, expectedAvg1, expectedMax1, 0, expectedThroughput, groupThreads);
		
		double expectedThroughputTotal = groupThreads / millisToSeconds(endTime1); // startTime is equal to zero so endTime - startTime = endTime
		// check the metrics for every periods. The value of the metrics are the same as those of the log period.
		assertEveryPeriodsMetricsForSampleLog(sampleLog1, creationDate1, groupThreads, expectedAvg1, expectedMax1, 0, expectedThroughputTotal, groupThreads);
		
		// *** Clear the interval registry before starting an other log period ***
		this.micrometerRegistry.clearIntervalRegistry();
		 
		// *** Second sample for the second log period ***
		long startTime2 = LOG_FREQUENCY * 1000; 	// The second sample starts after the first log period (ex: after 10 seconds = 10000 ms) 
		long endTime2 = startTime2 + 200; 	// The sample ends after 200 ms
		long responseTime2 = endTime2 - startTime2; 	// the response time is changed to 200 ms.
		int totalThreads = groupThreads + 1; // totalThread is increased by 1, because we will add a new sample
		ResponseResult responseResult2 = new ResponseResult("groupe1", responseTime2, false, groupThreads, totalThreads, "sample", startTime2, endTime2);
		
		micrometerRegistry.addResponse(responseResult2);
		Date creationDate2 = new Date(); // the new timestamp for the second log
		SampleLog sampleLog2 = micrometerRegistry.makeLog("spl_sample", creationDate2);
		
		// check the metrics for the log period.
		long expectedSum2 = responseTime2, expectedAvg2 = responseTime2, expectedMax2 = responseTime2;
		assertPeriodMetricsForSampleLog(sampleLog2, creationDate2, groupThreads, expectedSum2, expectedAvg2, expectedMax2, 0, expectedThroughput, groupThreads);
		
		// check the metrics for every periods.
		int groupThreadsTotal = 2; // there was two samples 
		long samplerCountTotal = groupThreadsTotal; // two requests of the same sampler
		long expectedSumTotal = responseTime1 + responseTime2; // the sum of the two response times
		long expectedAvgTotal = expectedSumTotal / groupThreadsTotal; // the average will be the sum divided by the two samples.
		long expectedMaxTotal = responseTime1; // the response time of the first sample is bigger than the second.
		expectedThroughputTotal = groupThreadsTotal / millisToSeconds(endTime2); // startTime is equal to zero so endTime - startTime = endTime
		
		assertEveryPeriodsMetricsForSampleLog(sampleLog2, creationDate2, samplerCountTotal, expectedAvgTotal, expectedMaxTotal, 0, expectedThroughputTotal, groupThreads);
	}
	
	
	/**
	 * Checks if the metric values ​​of the given SampleLog are the same as the expected values.
	 * Note: This method only checks the metrics corresponding to a specific period.
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
	private void assertPeriodMetricsForSampleLog(SampleLog sampleLog, Date expectedCreationDate, long expectedSamplerCount, long expectedSum, double expectedAvg, long expectedMax,
			 					int expectedErrors, double expectedThroughput, int expectedGroupThreads) {
		assertNotNull(sampleLog);
		assertEquals(sampleLog.getTimeStamp(), expectedCreationDate);
		assertEquals(sampleLog.getSamplerCount(), expectedSamplerCount); 
		assertEquals(sampleLog.getError(), expectedErrors); 
		
		assertTrue(sampleLog.getPct().length == 3);
	 	for (int i = 0; i < sampleLog.getPct().length - 1; i++) {
	 		long currentPct = (long) sampleLog.getPct()[i].value();
	 		long nextPct = (long) sampleLog.getPct()[i+1].value();
	 		assertTrue(currentPct <= nextPct); 
		}
		
		assertEquals(sampleLog.getSum(), expectedSum);
		assertEquals(sampleLog.getAvg(), expectedAvg); 
		assertEquals(sampleLog.getMax(), expectedMax);
		assertEquals(sampleLog.getThroughput(), expectedThroughput); 
		assertEquals(sampleLog.getThreads(), expectedGroupThreads); 
	}
	
	/**
	 * Checks if the metric values ​​of the given SampleLog are the same as the expected values.
	 * Note: This method only checks the metrics corresponding to a every periods.
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
	private void assertEveryPeriodsMetricsForSampleLog(SampleLog sampleLog, Date expectedCreationDate, long expectedSamplerCount, double expectedAvg, long expectedMax,
				int expectedErrors, double expectedThroughput, int expectedGroupThreads) {
		assertNotNull(sampleLog);
		assertEquals(sampleLog.getSamplerCountTotal(), expectedSamplerCount); 
		assertEquals(sampleLog.getErrorTotal(), expectedErrors); 
		
		assertTrue(sampleLog.getPctTotal().length == 3);
		for (int i = 0; i < sampleLog.getPct().length - 1; i++) {
	 		long currentPct = (long) sampleLog.getPct()[i].value();
	 		long nextPct = (long) sampleLog.getPct()[i+1].value();
	 		assertTrue(currentPct <= nextPct);
		}
		
		assertEquals(sampleLog.getAvgTotal(), expectedAvg); 
		assertEquals(sampleLog.getMaxTotal(), expectedMax);
		assertEquals(sampleLog.getThroughputTotal(), expectedThroughput); 
		assertEquals(sampleLog.getThreadsTotal(), expectedGroupThreads); 
	}
	
	private double millisToSeconds(long time) {
		return time / 1000d;
	}
	

}
