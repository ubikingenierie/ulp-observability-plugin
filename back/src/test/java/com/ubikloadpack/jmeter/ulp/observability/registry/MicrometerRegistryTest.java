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
		Map<Integer, Long> expectedPcts = Map.of(50, responseTime, 90, responseTime, 95, responseTime);
		for (ValueAtPercentile pct: sampleLog.getPctTotal()) {
			assertEquals((long) pct.value(), expectedPcts.get((int)(pct.percentile()*100))); 
		}
		
		double expectedThroughput = (double) 1/LOG_FREQUENCY; // number of requests of a sample per the log period
		
		// Assert the value of metrics for a log period
		assertEquals(sampleLog.getPct()[0].value(), 1000); // 50th percentile
		assertEquals(sampleLog.getPct()[0].value(), 1000); // 90th percentile
		assertEquals(sampleLog.getPct()[0].value(), 1000); // 95th percentile
		
		assertPeriodMetricsForSampleLog(sampleLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertPeriodMetricsForSampleLog(totalLabelLog, creationDate, 1, responseTime, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
		
		// Assert the value of metrics for a every periods
		assertEquals(sampleLog.getPctTotal()[0].value(), 1000); // 50th percentile
		assertEquals(sampleLog.getPctTotal()[0].value(), 1000); // 90th percentile
		assertEquals(sampleLog.getPctTotal()[0].value(), 1000); // 95th percentile
		
		expectedThroughput = groupThreads / millisToSeconds(endTime - startTime);
		assertEveryPeriodsMetricsForSampleLog(sampleLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);
		assertEveryPeriodsMetricsForSampleLog(totalLabelLog, creationDate, 1, responseTime, responseTime, 0, expectedThroughput, groupThreads);	
	}
	
	/**
	 * This test verifies the case when we have several requests of a sample. There's two samples and we should check that the metrics of 
	 * the totalLabel aggregate the results for the two samples. 
	 */
	@Test
	public void whenSeveralSamplesRecordedExpectComputedMetrics() {
		int groupThreads = 10;
		
		long startTime = 0, endTime = 0, responseTime = 0, expectedSumSample1 = 0, expectedSumSample2 = 0; 
		
		// generate 10 requests for "sample1" which there response times are : {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000};
		// and 10 requests for "sample2" which there response times are : {250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500};
		for (int i = 0; i < groupThreads; i++) {		
			startTime = i;
			endTime = startTime + 500 * (i + 1); // endTime is increased by 500 milliseconds
			responseTime = endTime - startTime;
			expectedSumSample1 += responseTime;
			ResponseResult responseResult1 = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample1", startTime, endTime);
			micrometerRegistry.addResponse(responseResult1);
			
			endTime = startTime + 250 * (i + 1); // we compute an other end time for the second sample
			responseTime = endTime - startTime;
			expectedSumSample2 += responseTime;
			ResponseResult responseResult2 = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample2", startTime, endTime);	
			micrometerRegistry.addResponse(responseResult2);
		}
		
		Date creationDate = new Date();
		// ### Testing ###
		// making the sampleLogs for each sample
		SampleLog sampleLog1 = micrometerRegistry.makeLog("spl_sample1", creationDate);
		SampleLog sampleLog2 = micrometerRegistry.makeLog("spl_sample2", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		
		// ### Assertions ###		
		// Assert the value of metrics for a log period
		assertTrue(sampleLog1.getPct()[0].value() >= 2500 && sampleLog1.getPct()[0].value() <= 3000); // 50th percentile
		assertTrue(sampleLog1.getPct()[1].value() >= 4500 && sampleLog1.getPct()[1].value() < 5000); // 90th percentile
		assertTrue(sampleLog1.getPct()[2].value() >= 5000); // 95th percentile
		
		assertTrue(sampleLog2.getPct()[0].value() >= 1250 && sampleLog2.getPct()[0].value() <= 1500); // 50th percentile
		assertTrue(sampleLog2.getPct()[1].value() >= 2250 && sampleLog2.getPct()[1].value() < 2500); // 90th percentile
		assertTrue(sampleLog2.getPct()[2].value() >= 2500); // 95th percentile
		// totalLabel responseTimes are : [250, 500, 500, 750, 1000, 1000, 1250, 1500, 1500, 1750, 2000, 2000, 2250, 2500, 2500, 3000, 3500, 4000, 4500, 5000]
		assertTrue(totalLabelLog.getPct()[0].value() >= 1750 && totalLabelLog.getPct()[0].value() <= 2000); // 50th percentile
		assertTrue(totalLabelLog.getPct()[1].value() >= 4000 && totalLabelLog.getPct()[1].value() < 4500); // 90th percentile
		assertTrue(totalLabelLog.getPct()[2].value() >= 4500); // 95th percentile
		
		double expectedThroughput = (double) groupThreads/LOG_FREQUENCY; // 10 threads per the LOG_FREQUENCY is the expected throughput
		double expectedAvg1 = (double) expectedSumSample1 / groupThreads; // the mean of {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000}
		assertPeriodMetricsForSampleLog(sampleLog1, creationDate, groupThreads, expectedSumSample1, expectedAvg1, 5000, 0, expectedThroughput, groupThreads);
		
		double expectedAvg2 = (double) expectedSumSample2 / groupThreads; // the mean of {250, 500, 750, 1000, 1250, 1500, 1750, 2000, 2250, 2500}
		assertPeriodMetricsForSampleLog(sampleLog2, creationDate, groupThreads, expectedSumSample2, expectedAvg2, 2500, 0, expectedThroughput, groupThreads);
		
		long expectedSumTotal = expectedSumSample1 + expectedSumSample2;
		double expectedAvgTotal = (double) expectedSumTotal / (groupThreads*2);
		expectedThroughput = (double) (groupThreads*2)/LOG_FREQUENCY;
		assertPeriodMetricsForSampleLog(totalLabelLog, creationDate, groupThreads*2, expectedSumTotal, expectedAvgTotal, 5000, 0, expectedThroughput, groupThreads);	
		
		// Assert the value of metrics for every periods
		assertTrue(sampleLog1.getPctTotal()[0].value() >= 2500 && sampleLog1.getPctTotal()[0].value() <= 3000); // 50th percentile
		assertTrue(sampleLog1.getPctTotal()[1].value() >= 4500 && sampleLog1.getPctTotal()[1].value() < 5000); // 90th percentile
		assertTrue(sampleLog1.getPctTotal()[2].value() >= 5000); // 95th percentile
		
		assertTrue(sampleLog2.getPctTotal()[0].value() >= 1250 && sampleLog2.getPctTotal()[0].value() <= 1500); // 50th percentile
		assertTrue(sampleLog2.getPctTotal()[1].value() >= 2250 && sampleLog2.getPctTotal()[1].value() < 2500); // 90th percentile
		assertTrue(sampleLog2.getPctTotal()[2].value() >= 2500); // 95th percentile
		// totalLabel responseTimes are : [250, 500, 500, 750, 1000, 1000, 1250, 1500, 1500, 1750, 2000, 2000, 2250, 2500, 2500, 3000, 3500, 4000, 4500, 5000]
		assertTrue(totalLabelLog.getPctTotal()[0].value() >= 1750 && totalLabelLog.getPctTotal()[0].value() <= 2000); // 50th percentile
		assertTrue(totalLabelLog.getPctTotal()[1].value() >= 4000 && totalLabelLog.getPctTotal()[1].value() < 4500); // 90th percentile
		assertTrue(totalLabelLog.getPctTotal()[2].value() >= 4500); // 95th percentile
		
		expectedThroughput = groupThreads / millisToSeconds(5009); // startTime is equal to zero so endTime - startTime = endTime
		assertEveryPeriodsMetricsForSampleLog(sampleLog1, creationDate, groupThreads, expectedAvg1, 5000, 0, expectedThroughput, groupThreads);
		expectedThroughput = groupThreads / millisToSeconds(2509);
		assertEveryPeriodsMetricsForSampleLog(sampleLog2, creationDate, groupThreads, expectedAvg2, 2500, 0, expectedThroughput, groupThreads);
		expectedThroughput = (groupThreads*2) / millisToSeconds(5009);
		assertEveryPeriodsMetricsForSampleLog(totalLabelLog, creationDate, groupThreads*2, expectedAvgTotal, 5000, 0, expectedThroughput, groupThreads);	
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
		assertTrue(sampleLog1.getPct()[0].value() == 500); // 50th percentile
		assertTrue(sampleLog1.getPct()[1].value() == 500); // 90th percentile
		assertTrue(sampleLog1.getPct()[2].value() == 500); // 95th percentile
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
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate2);
		
		// check the metrics for the log period.
		assertEquals(sampleLog2.getPct()[0].value(), 200); // 50th percentile
		assertEquals(sampleLog2.getPct()[1].value(), 200); // 90th percentile
		assertEquals(sampleLog2.getPct()[2].value(), 200); // 95th percentile
		
		long expectedSum2 = responseTime2, expectedAvg2 = responseTime2, expectedMax2 = responseTime2;
		assertPeriodMetricsForSampleLog(sampleLog2, creationDate2, groupThreads, expectedSum2, expectedAvg2, expectedMax2, 0, expectedThroughput, groupThreads);
		
		// check the metrics for every periods.
		assertEquals(totalLabelLog.getPctTotal()[0].value(), 200); // 50th percentile
		assertTrue(totalLabelLog.getPctTotal()[1].value() >= 500); // 90th percentile
		assertTrue(totalLabelLog.getPctTotal()[2].value() >= 500); // 95th percentile
		
		int groupThreadsTotal = 2; // there was two samples 
		long samplerCountTotal = groupThreadsTotal; // two requests of the same sampler
		long expectedSumTotal = responseTime1 + responseTime2; // the sum of the two response times
		long expectedAvgTotal = expectedSumTotal / groupThreadsTotal; // the average will be the sum divided by the two samples.
		long expectedMaxTotal = responseTime1; // the response time of the first sample is bigger than the second.
		expectedThroughputTotal = groupThreadsTotal / millisToSeconds(endTime2); // startTime is equal to zero so endTime - startTime = endTime
		
		assertEveryPeriodsMetricsForSampleLog(sampleLog2, creationDate2, samplerCountTotal, expectedAvgTotal, expectedMaxTotal, 0, expectedThroughputTotal, groupThreads);
		assertEveryPeriodsMetricsForSampleLog(totalLabelLog, creationDate2, samplerCountTotal, expectedAvgTotal, expectedMaxTotal, 0, expectedThroughputTotal, groupThreadsTotal);
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
