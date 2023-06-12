package com.ubikloadpack.jmeter.ulp.observability.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
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
	 * Test the value of the metrics when a single request of a sample is created. 
	 * Note: this test only considers one logging period.
	 */
	@Test 
	public void whenOnlyOneSampleRecordedExpectComputedMetrics() {
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
		Arrays.asList(sampleLog.getPct()).forEach(pct -> assertEquals(responseTime, (long) pct.value())); // percentiles for a log period
		Arrays.asList(sampleLog.getPctTotal()).forEach(pct -> assertEquals(responseTime, (long) pct.value())); // percentiles for every periods

		double expectedThroughput = (double) 1/LOG_FREQUENCY; // number of requests of a sample per the log period
		
		assertPeriodMetricsForSample(sampleLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertPeriodMetricsForSample(totalLabelLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
		
		expectedThroughput = groupThreads / millisToSeconds(endTime - startTime);
		assertEveryPeriodsMetricsForSample(sampleLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertEveryPeriodsMetricsForSample(totalLabelLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
	}
	
	/**
	 * This test verifies the case when we have several requests of a sample. There's two samples and we should check that the metrics of 
	 * the totalLabel aggregate the results for the two samples. 
	 */
	@Test
	public void whenTwoSamplesFromDifferentSamplersAreRecordedExpectComputedMetrics() {
		int groupThreads = 10;
		
		createSampleResultsAndAddToMicrometerRegistry(groupThreads);
		
		Date creationDate = new Date();
		// ### Testing ###
		// making the sampleLogs for each sample
		SampleLog sampleLog1 = micrometerRegistry.makeLog("spl_sample1", creationDate);
		SampleLog sampleLog2 = micrometerRegistry.makeLog("spl_sample2", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		
		// ### Assertions ###		
		// Assert the value of metrics for a log period
		assertPercentilesForSampleLog(sampleLog1.getPct(), 2500, 3000, 4500, 5000);
	    assertPercentilesForSampleLog(sampleLog2.getPct(), 1250, 1500, 2250, 2500);
	    assertPercentilesForSampleLog(totalLabelLog.getPct(), 1750, 2000, 4000, 4500);
		
	    long expectedSumSample1 = 27500, expectedSumSample2 = 13750;
		double expectedThroughput = (double) groupThreads/LOG_FREQUENCY; // 10 threads per the LOG_FREQUENCY is the expected throughput
		double expectedAvg1 = (double) expectedSumSample1 / groupThreads; // the mean of {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000}
		assertPeriodMetricsForSample(sampleLog1, creationDate, groupThreads, expectedSumSample1, expectedAvg1, 5000, 0, expectedThroughput, groupThreads);
		
		double expectedAvg2 = (double) expectedSumSample2 / groupThreads; // the mean of {250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500}
		assertPeriodMetricsForSample(sampleLog2, creationDate, groupThreads, expectedSumSample2, expectedAvg2, 2500, 0, expectedThroughput, groupThreads);
		
		long expectedSumTotal = expectedSumSample1 + expectedSumSample2;
		double expectedAvgTotal = (double) expectedSumTotal / (groupThreads*2);
		expectedThroughput = (double) (groupThreads*2)/LOG_FREQUENCY;
		assertPeriodMetricsForSample(totalLabelLog, creationDate, groupThreads*2, expectedSumTotal, expectedAvgTotal, 5000, 0, expectedThroughput, groupThreads);	
		
		// Assert the value of metrics for every periods
		assertPercentilesForSampleLog(sampleLog1.getPctTotal(), 2500, 3000, 4500, 5000);
	    assertPercentilesForSampleLog(sampleLog2.getPctTotal(), 1250, 1500, 2250, 2500);
	    assertPercentilesForSampleLog(totalLabelLog.getPctTotal(), 1750, 2000, 4000, 4500);
		
		expectedThroughput = groupThreads / millisToSeconds(5009); // startTime is equal to zero so endTime - startTime = endTime
		assertEveryPeriodsMetricsForSample(sampleLog1, creationDate, groupThreads, expectedAvg1, 5000, 0, expectedThroughput, groupThreads);
		expectedThroughput = groupThreads / millisToSeconds(2509);
		assertEveryPeriodsMetricsForSample(sampleLog2, creationDate, groupThreads, expectedAvg2, 2500, 0, expectedThroughput, groupThreads);
		expectedThroughput = (groupThreads*2) / millisToSeconds(5009);
		assertEveryPeriodsMetricsForSample(totalLabelLog, creationDate, groupThreads*2, expectedAvgTotal, 5000, 0, expectedThroughput, groupThreads);	
	}

	
	
	/**
	 * This test verifies the value of the metrics registered on the micrometer registries. 
	 * Especially, the test consider two periods of the log, for each one we make a sampleLog and 
	 * then check the metrics for the current period and for every periods. For simplicity, 
	 * we make only one sampleLog for each period.
	 */
	@Test
	public void whenTwoRequestsOfSingleSampleOnTwoLogPeriodsExpectMetricsVerification() {
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
		assertPercentilesForSampleLog(sampleLog1.getPct(), 500, 500, 500, 500);
		assertPeriodMetricsForSample(sampleLog1, creationDate1, groupThreads, expectedSum1, expectedAvg1, expectedMax1, 0, expectedThroughput, groupThreads);
		
		double expectedThroughputTotal = groupThreads / millisToSeconds(endTime1); // startTime is equal to zero so endTime - startTime = endTime
		// check the metrics for every periods. The value of the metrics are the same as those of the log period.
		assertEveryPeriodsMetricsForSample(sampleLog1, creationDate1, groupThreads, expectedAvg1, expectedMax1, 0, expectedThroughputTotal, groupThreads);
		
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
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate2);
		
		// check the metrics for the log period.
		assertPercentilesForSampleLog(sampleLog1.getPct(), 200, 200, 200, 200);
		
		long expectedSum2 = responseTime2, expectedAvg2 = responseTime2, expectedMax2 = responseTime2;
		assertPeriodMetricsForSample(sampleLog2, creationDate2, groupThreads, expectedSum2, expectedAvg2, expectedMax2, 0, expectedThroughput, groupThreads);
		
		// check the metrics for every periods.
		assertPercentilesForSampleLog(totalLabelLog.getPctTotal(), 200, 500, 500, 500);
		
		int groupThreadsTotal = 2; // there was two samples 
		long expectedAvgTotal = (responseTime1 + responseTime2) / groupThreadsTotal; // the average will be the sum divided by the two samples.
		long expectedMaxTotal = responseTime1; // the response time of the first sample is bigger than the second.
		expectedThroughputTotal = groupThreadsTotal / millisToSeconds(endTime2); // startTime is equal to zero so endTime - startTime = endTime
		
		assertEveryPeriodsMetricsForSample(sampleLog2, creationDate2, groupThreadsTotal, expectedAvgTotal, expectedMaxTotal, 0, expectedThroughputTotal, groupThreads);
		assertEveryPeriodsMetricsForSample(totalLabelLog, creationDate2, groupThreadsTotal, expectedAvgTotal, expectedMaxTotal, 0, expectedThroughputTotal, groupThreadsTotal);
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
	private void assertPeriodMetricsForSample(SampleLog sampleLog, Date expectedCreationDate, long expectedSamplerCount, long expectedSum, double expectedAvg, long expectedMax,
			 					int expectedErrors, double expectedThroughput, int expectedGroupThreads) {
		assertNotNull(sampleLog);
		assertEquals(expectedCreationDate, sampleLog.getTimeStamp());
		assertEquals(expectedSamplerCount, sampleLog.getSamplerCount()); 
		assertEquals(expectedErrors, sampleLog.getError()); 
		
		assertTrue(sampleLog.getPct().length == 3);
	 	for (int i = 0; i < sampleLog.getPct().length - 1; i++) {
	 		long currentPct = (long) sampleLog.getPct()[i].value();
	 		long nextPct = (long) sampleLog.getPct()[i+1].value();
	 		assertTrue(currentPct <= nextPct); 
		}
		
		assertEquals(expectedSum, sampleLog.getSum());
		assertEquals(expectedAvg, sampleLog.getAvg()); 
		assertEquals(expectedMax, sampleLog.getMax());
		assertEquals(expectedThroughput, sampleLog.getThroughput()); 
		assertEquals(expectedGroupThreads, sampleLog.getThreads()); 
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
	private void assertEveryPeriodsMetricsForSample(SampleLog sampleLog, Date expectedCreationDate, long expectedSamplerCount, double expectedAvg, long expectedMax,
				int expectedErrors, double expectedThroughput, int expectedGroupThreads) {
		assertNotNull(sampleLog);
		assertEquals(expectedSamplerCount, sampleLog.getSamplerCountTotal()); 
		assertEquals(expectedErrors, sampleLog.getErrorTotal()); 
		
		assertTrue(sampleLog.getPctTotal().length == 3);
		for (int i = 0; i < sampleLog.getPct().length - 1; i++) {
	 		long currentPct = (long) sampleLog.getPct()[i].value();
	 		long nextPct = (long) sampleLog.getPct()[i+1].value();
	 		assertTrue(currentPct <= nextPct);
		}
		
		assertEquals(expectedAvg, sampleLog.getAvgTotal()); 
		assertEquals(expectedMax, sampleLog.getMaxTotal());
		assertEquals(expectedThroughput, sampleLog.getThroughputTotal()); 
		assertEquals(expectedGroupThreads, sampleLog.getThreadsTotal()); 
	}
	
	private void assertPercentilesForSampleLog(ValueAtPercentile[] percentiles, int pct50Min, int pct50Max, int pct90Min, int pct95Min) {
	    assertTrue(percentiles[0].value() >= pct50Min && percentiles[0].value() <= pct50Max);
	    assertTrue(percentiles[1].value() >= pct90Min && percentiles[1].value() < pct95Min);
	    assertTrue(percentiles[2].value() >= pct95Min);
	}
	
	private double millisToSeconds(long time) {
		return time / 1000d;
	}
	
	private void createSampleResultsAndAddToMicrometerRegistry(int groupThreads) {
		long startTime = 0;
		long endTime = 0;
		long responseTime = 0;
		// generate 10 requests for "sample1" which there response times are : {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000};
		// and 10 requests for "sample2" which there response times are : {250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500};
		for (int i = 0; i < groupThreads; i++) {		
			startTime = i;
			endTime = startTime + 500 * (i + 1); // endTime is increased by 500 milliseconds
			responseTime = endTime - startTime;
			ResponseResult responseResult1 = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample1", startTime, endTime);
			micrometerRegistry.addResponse(responseResult1);
			
			endTime = startTime + 250 * (i + 1); // we compute an other end time for the second sample
			responseTime = endTime - startTime;
			ResponseResult responseResult2 = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample2", startTime, endTime);	
			micrometerRegistry.addResponse(responseResult2);
		}
	}
	

}
