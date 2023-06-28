package com.ubikloadpack.jmeter.ulp.observability.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.util.ErrorTypeInfo;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class MicrometerRegistryTest {
	private static final String TOTAL_lABEL = "total_info";
	private static final int LOG_FREQUENCY = 10; 
	private static final int TOP_ERRORS_NUMBER = 3;
	
	private MicrometerRegistry micrometerRegistry;
	
	@BeforeEach
	public void setUp() {
		this.micrometerRegistry = new MicrometerRegistry(TOTAL_lABEL, 50, 90, 95, LOG_FREQUENCY, TOP_ERRORS_NUMBER, new SampleLogger(TOTAL_lABEL), 3000);
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
	@DisplayName("When only one sample is created expect its computed metrics.")
	public void whenOnlyOneSampleRecordedExpectComputedMetrics() {
		// ### SetUp ###
		int groupThreads = 1;
		long startTime = 0; // millisecond
		long endTime = 1000; // millisecond
		long responseTime = endTime - startTime; 
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, "", groupThreads, 1, "sample", startTime, endTime);
		
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
	@DisplayName("When two samples from different samplers are created expect their metrics and those of totalLabel.")
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
	@DisplayName("When two requests of a single sample are created on two log periods, expect totalLabel's metrics are agregated")
	public void whenTwoRequestsOfSingleSampleOnTwoLogPeriodsExpectMetricsVerification() {
		int groupThreads = 1;
		 
		// *** First sample for the first log period ***
		ResponseResult responseResult1 = new ResponseResult("groupe1", 500L, false, "", groupThreads, groupThreads, "sample", 0L, 500L);
		micrometerRegistry.addResponse(responseResult1);
		SampleLog sampleLog1 = micrometerRegistry.makeLog("spl_sample", getFixedDateIncreasedBySeconds(1));
		
		// check the metrics for the log period. There's only one sample so the sum, average and the max are the same as the responseTime of the sample
		assertPercentilesForSampleLog(sampleLog1.getPct(), 500, 500, 500, 500);
		assertPeriodMetricsForSample(sampleLog1, getFixedDateIncreasedBySeconds(1), groupThreads, 500, 500, 500, 0, (double) groupThreads/LOG_FREQUENCY, groupThreads);
		// check the metrics for every periods. The value of the metrics are the same as those of the log period.
		assertEveryPeriodsMetricsForSample(sampleLog1, getFixedDateIncreasedBySeconds(1), groupThreads, 500, 500, 0, groupThreads / millisToSeconds(500), groupThreads);
		
		// *** Clear the interval registry before starting an other log period ***
		this.micrometerRegistry.clearIntervalRegistry();
		 
		// *** Second sample for the second log period ***
		long startTime2 = LOG_FREQUENCY * 1000; 	// The second sample starts after the first log period (ex: after 10 seconds = 10000 ms) 
		// totalThread is increased by 1, because we will add a new sample
		ResponseResult responseResult2 = new ResponseResult("groupe1", 200L, false, "", groupThreads, groupThreads+1, "sample", startTime2, startTime2+200); 
		micrometerRegistry.addResponse(responseResult2);
		SampleLog sampleLog2 = micrometerRegistry.makeLog("spl_sample", getFixedDateIncreasedBySeconds(2));
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), getFixedDateIncreasedBySeconds(2));
		
		// check the metrics for the log period.
		assertPercentilesForSampleLog(sampleLog2.getPct(), 200, 200, 200, 200);
		assertPeriodMetricsForSample(sampleLog2, getFixedDateIncreasedBySeconds(2), groupThreads, 200, 200, 200, 0, (double) groupThreads/LOG_FREQUENCY, groupThreads);
		// check the metrics for every periods.
		assertPercentilesForSampleLog(totalLabelLog.getPctTotal(), 200, 500, 500, 501);
		// the average will be the sum divided by the number of samples. So (200 + 500) / 2 = 350
		double throughputTotal = 2/millisToSeconds(startTime2+200); // number of samples divided by the endtime of the last sample
		assertEveryPeriodsMetricsForSample(sampleLog2, getFixedDateIncreasedBySeconds(2), 2, 350, 500, 0, throughputTotal, groupThreads);
		assertEveryPeriodsMetricsForSample(totalLabelLog, getFixedDateIncreasedBySeconds(2), 2, 350, 500, 0, throughputTotal, 2);
	}
	
	@Test
	@DisplayName("When a sample fails due to an error, expect it's added to the errors map")
	public void whenAnErrorOccursOnSampleExpectItIsAddedToErrorsMap() {
		ResponseResult responseResult = new ResponseResult("groupe1", 500L, true, Integer.toString(HttpStatus.NOT_FOUND_404), 1, 1, "sample", 0L, 500L);
		micrometerRegistry.addResponse(responseResult);
		
		ConcurrentHashMap<String, ErrorTypeInfo> errorMap = micrometerRegistry.getErrorsMap().getErrorPerType();
		assertTrue(errorMap.containsKey("404"));
		assertEquals(new ErrorTypeInfo("404", 1), errorMap.get("404"));
	}
	
	@Test
	@DisplayName("When a sample fails due to an error, expect it's extracted from the top errors")
	public void whenAnErrorOccursOnSampleExpectItIsExtractedFromTopErrors() {
		ResponseResult responseResult = new ResponseResult("groupe1", 500L, true, Integer.toString(HttpStatus.NOT_FOUND_404), 1, 1, "sample", 0L, 500L);
		micrometerRegistry.addResponse(responseResult);
		
		SampleLog sampleLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), getFixedDateIncreasedBySeconds(1));
		
		assertTrue(sampleLog.getTopErrors().isPresent(), "The list of the top errors is not present. It should be present only if the sampleLog represents the total label");
		assertEquals(1, sampleLog.getTopErrors().get().size());
		assertEquals(new ErrorTypeInfo("404", 1), sampleLog.getTopErrors().get().get(0));	
	}



	// assertEquals(1, actualErrorType.computeErrorRateAmongErrors(sampleLog.getErrorTotal()));
	// assertEquals(1, actualErrorType.computeErrorRateAmongRequests(sampleLog.getSamplerCountTotal()));
	
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
		assertNotNull(sampleLog, "If null so the name of the sample passed to the MicrometerRegistry#makeLog() is incorrect");
		assertEquals(expectedCreationDate, sampleLog.getTimeStamp());
		assertEquals(expectedSamplerCount, sampleLog.getSamplerCount()); 
		assertEquals(expectedErrors, sampleLog.getError()); 
		
		assertValueAtPercentile(sampleLog.getPct());
		
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
		assertNotNull(sampleLog, "If null so the name of the sample passed to the MicrometerRegistry#makeLog() is incorrect");
		assertEquals(expectedSamplerCount, sampleLog.getSamplerCountTotal()); 
		assertEquals(expectedErrors, sampleLog.getErrorTotal()); 
		
		assertValueAtPercentile(sampleLog.getPctTotal());
		
		assertEquals(expectedAvg, sampleLog.getAvgTotal()); 
		assertEquals(expectedMax, sampleLog.getMaxTotal());
		assertEquals(expectedThroughput, sampleLog.getThroughputTotal()); 
		assertEquals(expectedGroupThreads, sampleLog.getThreadsTotal()); 
	}
	

	private void assertValueAtPercentile(ValueAtPercentile[] pcts) {
	    assertTrue(pcts.length == 3, "Actually the number of percentiles should be always three, this is fixed during MicrometerRegistry's instantiation.");
	    for (int i = 0; i < pcts.length - 1; i++) {
	        ValueAtPercentile currentPct = pcts[i];
	        ValueAtPercentile nextPct = pcts[i + 1];
	
	        String failureMsg = String.format("The value of the %sth percentile is not lower than the %sth percentile value.", currentPct.percentile() * 100, nextPct.percentile() * 100);
	        assertTrue((long) currentPct.value() <= (long) nextPct.value(), failureMsg);
	    }
	}
	
	private void assertPercentilesForSampleLog(ValueAtPercentile[] percentiles, double pct50Min, double pct50Max, double pct90Min, double pct95Min) {
	    assertTrue(percentiles[0].value() >= pct50Min && percentiles[0].value() <= pct50Max, "The actual 5Oth percentile isn't in the range ["+pct50Min+", "+pct50Max+"]");
	    assertTrue(percentiles[1].value() >= pct90Min && percentiles[1].value() <= pct95Min, "The actual 9Oth percentile isn't in the range ["+pct90Min+", "+pct95Min+"]");
	    assertTrue(percentiles[2].value() >= pct95Min, "The actual 95th percentile should be at least greater than " + pct95Min);
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
			ResponseResult responseResult1 = new ResponseResult("groupe1", responseTime, false, "", groupThreads, groupThreads, "sample1", startTime, endTime);
			micrometerRegistry.addResponse(responseResult1);
			
			endTime = startTime + 250 * (i + 1); // we compute an other end time for the second sample
			responseTime = endTime - startTime;
			ResponseResult responseResult2 = new ResponseResult("groupe1", responseTime, false, "", groupThreads, groupThreads, "sample2", startTime, endTime);	
			micrometerRegistry.addResponse(responseResult2);
		}
	}
	
	private Date getFixedDateIncreasedBySeconds(int second) {
		return Date.from(Instant.ofEpochSecond(155000000+second));
	}
	
	

}
