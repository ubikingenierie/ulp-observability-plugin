package com.ubikloadpack.jmeter.ulp.observability.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilityMetricServletTest extends AbstractConfigTest {
	
	@Test
	@DisplayName("When GET request expect OK")
	public void whenGetRequestExpectOk() throws Exception {	
		HttpResponse httpResponse = this.sendGetRequest(METRICS_ROUTE);
		
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		assertTrue(httpResponse.getResponse().isEmpty());
	}
	
	@Test
	@DisplayName("When one sample event received expect metrics generated and validated")
	public void whenOneSampleEventReceivedExpectMetricsGeneratedAndValidated() throws Exception {	
		int groupThreads = 5, allThreads = 10;
		SampleEvent sampleEvent = this.createSampleEvent("sampleTest", "groupe1", true, groupThreads, allThreads, 1000L);
		this.listener.sampleOccurred(sampleEvent);
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse = this.sendGetRequest(METRICS_ROUTE);
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
	
		String actualMetrics = httpResponse.getResponse();
		assertFalse(actualMetrics.isEmpty());
		
		String totalLabelOpenMetric = Util.makeOpenMetricsName(TOTAL_LABEL);
		String sampleNameOpenMetric = Util.makeOpenMetricsName("sampleTest");
		
		// assert percentiles for one period : the value of the percentiles are the same as the value of the response time.
		Map<Integer, Long> pcts = Map.of(this.listener.getPct1(), 1000L, this.listener.getPct2(), 1000L, this.listener.getPct3(), 1000L);
		assertPercentilesOfOnePeriod(actualMetrics, totalLabelOpenMetric, pcts, sampleNameOpenMetric, pcts);
		assertPercentilesOfEveryPeriods(actualMetrics, totalLabelOpenMetric, pcts, sampleNameOpenMetric, pcts);
		// assert max response times
		assertMaxResponseTime(actualMetrics, totalLabelOpenMetric, 1000L, sampleNameOpenMetric, 1000L);
		assertMaxEveryPeriods(actualMetrics, totalLabelOpenMetric, 1000L, sampleNameOpenMetric, 1000L);
		// assert average
		assertAverage(actualMetrics, totalLabelOpenMetric, 1000L, sampleNameOpenMetric, 1000L);
		assertAvgEveryPeriods(actualMetrics, totalLabelOpenMetric, 1000L, sampleNameOpenMetric, 1000L);
		// assert sampler count
		assertSamplersCount(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1); // there was only one sample event, so the count = 1.
		assertSamplersCountEveryPeriods(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1);
		// assert error count
		assertErrorsCount(actualMetrics, totalLabelOpenMetric, 0, sampleNameOpenMetric, 0);
		assertErrorsCountEveryPeriods(actualMetrics, totalLabelOpenMetric, 0, sampleNameOpenMetric, 0);
		// assert throughput
		assertThroughput(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1);
		assertThroughputEveryPeriods(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1);
		// assert threads count
		assertThreadsCount(actualMetrics, totalLabelOpenMetric, allThreads, sampleNameOpenMetric, groupThreads);
		assertThreadsCountEveryPeriods(actualMetrics, totalLabelOpenMetric, allThreads, sampleNameOpenMetric, groupThreads);
	}
	
	/**
	 * The following test verifies if the value of the totalLabel contains spaces we should always have the logs.
	 * This test is needed because there was an issue when the totalLabel contains spaces. Indeed, the SampleLogger#guiLog() 
	 * raises a silent NullPöinterException and the current thread making the log is stopped. So no log was made. 
	 * 
	 * So this test guarantee that despite the value of the totalLabel we should always receive the logs.
	 */
	@Test
	@DisplayName("When totalLabel could contains spaces expect metrics generated and logged")
	public void whenTotalLabelCouldContainsSpacesExpectMetricsGeneratedAndLogged() throws Exception { 
		// *** setUp to change the total label set on the Listener ***
		this.listener.testEnded(HOST); // should stop the listener started by @BeforeEach before setting the totalLabel property
		this.listener.setTotalLabel("total label"); // label contains spaces: 
		this.testStarted(HOST); // restart the listener 
		
		// *** create and send a first sampleEvent ***
		SampleEvent sampleEvent1 = this.createSampleEvent("sampleTest", "groupe1", true, 1, 1, 500);
		this.listener.sampleOccurred(sampleEvent1);
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse1 = this.sendGetRequest(METRICS_ROUTE); // send a GET REQUEST
		// *** Checks the response of the server ***
		assertHttpContentTypeAndResponseStatus(httpResponse1, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		
		String actualMetrics = httpResponse1.getResponse();
		assertFalse(actualMetrics.isEmpty()); // assert that we go an answer from the server
		
		// *** Checks if the metrics of the first log is changed ***
		String totalLabelOpenMetric = Util.makeOpenMetricsName("total label");
		String sampleNameOpenMetric = Util.makeOpenMetricsName("sampleTest");
		
		assertSamplersCount(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1); // there was only one sample event, so the count = 1.
		assertSamplersCountEveryPeriods(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1);
		
		// *** create and send a second sampleEvent ***
		SampleEvent sampleEvent2 = this.createSampleEvent("sample", "groupe1", true, 1, 1, 500);
		this.listener.sampleOccurred(sampleEvent2);
		
		Thread.sleep(1000); // should wait the next log
		HttpResponse httpResponse2 = this.sendGetRequest(METRICS_ROUTE); // send a second GET REQUEST
		assertHttpContentTypeAndResponseStatus(httpResponse2, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		
		actualMetrics = httpResponse2.getResponse();
		assertFalse(actualMetrics.isEmpty()); 
		
		// *** Checks if the metrics of the second log is changed ***
		assertSamplersCount(actualMetrics, totalLabelOpenMetric, 1, sampleNameOpenMetric, 1); 
		assertSamplersCountEveryPeriods(actualMetrics, totalLabelOpenMetric, 2, sampleNameOpenMetric, 1); // totalLabel's count becomes 2
	}
	
	@Test
	@DisplayName("When a sample fails due to an error, expect the occurred error is reported as a top error")
	public void whenErrorOccurredOnSampleExpectItOnTopErrors() throws Exception {
		SampleEvent sample = this.createSampleEvent("sampleTest", "groupe1", false, Integer.toString(HttpStatus.NOT_FOUND_404), 1, 1, 500);
		this.listener.sampleOccurred(sample);
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse = this.sendGetRequest(METRICS_ROUTE); // send a GET REQUEST
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		String actualMetrics = httpResponse.getResponse();
		
		String expectedTopError = getExpectedOpenMetricTopError("404", 1, 1D, 1D);
		assertTrue(actualMetrics.contains(expectedTopError),  getTopErrorFailureMessage(expectedTopError, actualMetrics));
	}
	
	@Test
	@DisplayName("When a sample fails due to an error with a success response code, expect `Assertion failed` as reported type error")
	public void whenErrorOccurredOnSampleWithSuccessCodeExpectAssertionFailedAsTypeError() throws Exception {
		SampleEvent sample = this.createSampleEvent("sampleTest", "groupe1", false, Integer.toString(HttpStatus.OK_200), 1, 1, 500);
		this.listener.sampleOccurred(sample);
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse = this.sendGetRequest(METRICS_ROUTE); // send a GET REQUEST
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		String actualMetrics = httpResponse.getResponse();
		
		String expectedTopError = getExpectedOpenMetricTopError(MetricUtils.ASSERTION_FAILED, 1, 1D, 1D);
		assertTrue(actualMetrics.contains(expectedTopError),  getTopErrorFailureMessage(expectedTopError, actualMetrics));
	}
	
	@Test
	@DisplayName("When several samples fail, expect only the most frequented errors are included in the server response")
	public void whenSeveralSamplesFailsExpectOnlyTheFrequentedErrorsAreExtractedAsTopErrors() throws Exception {	
		assertEquals(5, this.listener.getTopErrors());
		
		List<SampleEvent> samples = createSampleEvents();
		samples.forEach(s -> listener.sampleOccurred(s));
		long totalErrors = samples.size();
		
		Thread.sleep(1000); // should wait at least one second before generating the next log.
		HttpResponse httpResponse = this.sendGetRequest(METRICS_ROUTE); // send a GET REQUEST
		assertHttpContentTypeAndResponseStatus(httpResponse, HttpStatus.OK_200, "text/plain; version=0.0.4; charset=utf-8");
		String actualMetrics = httpResponse.getResponse();
		
		String expected404 = getExpectedOpenMetricTopError("404", 5, getErrorRate(5, totalErrors), getErrorRate(5, totalErrors));
		assertTrue(actualMetrics.contains(expected404), getTopErrorFailureMessage(expected404, actualMetrics));
		
		String expected502 = getExpectedOpenMetricTopError("502", 4, getErrorRate(4, totalErrors), getErrorRate(4, totalErrors));
		assertTrue(actualMetrics.contains(expected502), getTopErrorFailureMessage(expected502, actualMetrics));
		
		String expected405 = getExpectedOpenMetricTopError("405", 4, getErrorRate(4, totalErrors), getErrorRate(4, totalErrors));
		assertTrue(actualMetrics.contains(expected405), getTopErrorFailureMessage(expected405, actualMetrics));
		
		String expected409 = getExpectedOpenMetricTopError("409", 3, getErrorRate(3, totalErrors), getErrorRate(3, totalErrors));
		assertTrue(actualMetrics.contains(expected409), getTopErrorFailureMessage(expected409, actualMetrics));
		
		String expected400 = getExpectedOpenMetricTopError("400", 2, getErrorRate(2, totalErrors), getErrorRate(2, totalErrors));
		assertTrue(actualMetrics.contains(expected400), getTopErrorFailureMessage(expected400, actualMetrics));
		
		// The server doesn't contain the 403 error type
		String expected403 = getExpectedOpenMetricTopError("403", 1, getErrorRate(1, totalErrors), getErrorRate(1, totalErrors));
		assertFalse(actualMetrics.contains(expected403), getTopErrorFailureMessage(expected403, actualMetrics));
	}
	
	private double getErrorRate(long occurrence, long total) {
		return total > 0 ? (double) occurrence / (double) total : 0;
	}
	
	/**
	 * Creates sample events with different error types and labels.
	 * @return A list containing all the created samples
	 */
	private List<SampleEvent> createSampleEvents() {
		List<SampleEvent> sampleEvents = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			if (i < 1) { // add up to 1 sample with 400 error
				sampleEvents.add(createSampleEvent("sample1", "groupe1", false, Integer.toString(HttpStatus.FORBIDDEN_403), 1, 1, 500));
			}
			if (i < 2) { // add up to 2 samples with 400 error
				sampleEvents.add(createSampleEvent("sample2", "groupe1", false, Integer.toString(HttpStatus.BAD_REQUEST_400), 1, 1, 500));
			}
			if (i < 3) { // add up to 3 samples with 409 error
				sampleEvents.add(createSampleEvent("sample3", "groupe1", false, Integer.toString(HttpStatus.CONFLICT_409), 1, 1, 500));
			}
			if (i < 4) { // add up to 4 samples for each 502 and 405 error
				sampleEvents.add(createSampleEvent("sample4", "groupe1", false, Integer.toString(HttpStatus.BAD_GATEWAY_502), 1, 1, 500));
				sampleEvents.add(createSampleEvent("sample5", "groupe1", false, Integer.toString(HttpStatus.METHOD_NOT_ALLOWED_405), 1, 1, 500));
			} 
			 // add up to 5 samples with 404 error
			sampleEvents.add(createSampleEvent("sample6", "groupe1", false, Integer.toString(HttpStatus.NOT_FOUND_404), 1, 1, 500));
		}
		return sampleEvents;
	}
	
	/**
	 * Generates the expected top error metric string for a specific error type.
	 * @param errorType The error type.
	 * @param occurrence The occurrence of the error type.
	 * @param errorRate The error rate among requests
	 * @param errorFrequency The error rate among errors
	 * @return the expected top error metric in the openMetric format.
	 */
	private String getExpectedOpenMetricTopError(String errorType, int occurrence, double errorRate, double errorFrequency) {
		String pattern = "{0}_total'{'count=\"error_every_periods\",errorType=\"{1}\",errorRate=\"{2,number,0.0###}\",errorFreq=\"{3,number,0.0###}\"'}' {4}";
		MessageFormat temp = new MessageFormat(pattern, Locale.US);
		Object[] arguments = { TOTAL_LABEL, errorType, errorRate, errorFrequency, occurrence };
		return temp.format(arguments);
	}
	
	private String getTopErrorFailureMessage(String expectedTopError, String actualMetrics) {
		return String.format("Expected top error: %s\nis not contained in the actual metrics:\n%s", expectedTopError, actualMetrics);
	}
	
	
	private SampleEvent createSampleEvent(String sampleLabel, String threadGroupName,  boolean isSuccessful, int groupThreads, int allThreads, long responseTime) {
		return this.createSampleEvent(sampleLabel, threadGroupName, isSuccessful, Integer.toString(HttpURLConnection.HTTP_OK), groupThreads, allThreads, responseTime);
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
	private SampleEvent createSampleEvent(String sampleLabel, String threadGroupName,  boolean isSuccessful, String responseCode, int groupThreads, int allThreads, long responseTime) {
		SampleResult sampleResult = new SampleResult();
		sampleResult.setAllThreads(allThreads);
		sampleResult.setSuccessful(isSuccessful);
		sampleResult.setResponseCode(responseCode);
		sampleResult.setSampleLabel(sampleLabel);
		sampleResult.setGroupThreads(groupThreads);
		sampleResult.sampleStart(); // this set the startTime to currentMillisecond
		sampleResult.setEndTime(sampleResult.getStartTime() + responseTime);
		
		return new SampleEvent(sampleResult, threadGroupName);
	}
	
	private void assertPercentilesOfOnePeriod(String actualOpenMetrics, String totalLabelOpenMetric, Map<Integer, Long> totalPcts,
											  String sampleNameOpenMetric, Map<Integer, Long> samplePcts) 
	{
		totalPcts.forEach((pct, value) -> {
			String totalLabelPct = String.format("%s_pct{quantile=\"%s\"} %s", totalLabelOpenMetric, pct, value);
			assertTrue(actualOpenMetrics.contains(totalLabelPct));
		});
		
		samplePcts.forEach((pct, value) -> {
			String samplePct = String.format("%s_pct{quantile=\"%s\"} %s", sampleNameOpenMetric, pct, value);
			assertTrue(actualOpenMetrics.contains(samplePct));
		});
	}
	
	private void assertPercentilesOfEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, Map<Integer, Long> totalPcts,
												 String sampleNameOpenMetric, Map<Integer, Long> samplePcts) 
	{
		totalPcts.forEach((pct, value) -> {
			String totalLabelPct = String.format("%s_pct{quantile_every_periods=\"%s\"} %s", totalLabelOpenMetric, pct, value);
			assertTrue(actualOpenMetrics.contains(totalLabelPct));
		});
		
		samplePcts.forEach((pct, value) -> {
			String samplePct = String.format("%s_pct{quantile_every_periods=\"%s\"} %s", sampleNameOpenMetric, pct, value);
			assertTrue(actualOpenMetrics.contains(samplePct));
		});
	}
	
	private void assertMaxResponseTime(String actualOpenMetrics, String totalLabelOpenMetric, long totalMax, String sampleNameOpenMetric, long sampleMax) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_max " + totalMax));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_max " + sampleMax));
	}
	
	private void assertMaxEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, long totalMax, String sampleNameOpenMetric, long sampleMax) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_max_every_periods " + totalMax));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_max_every_periods " + sampleMax));
	}
	
	private void assertAverage(String actualOpenMetrics, String totalLabelOpenMetric, long totalAvg, String sampleNameOpenMetric, long sampleAvg) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_avg " + totalAvg)); 
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_avg " + sampleAvg));
	}
	
	private void assertAvgEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, long totalAvg, String sampleNameOpenMetric, long sampleAvg) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_avg_every_periods " + totalAvg));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_avg_every_periods " + sampleAvg));
	}

	private void assertSamplersCount(String actualOpenMetrics, String totalLabelOpenMetric, long totalCount, String sampleNameOpenMetric, long sampleCount) {
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"sampler_count\"} %s", totalLabelOpenMetric, totalCount)));
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"sampler_count\"} %s", sampleNameOpenMetric, sampleCount)));
	}
	
	private void assertSamplersCountEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, long totalCount, String sampleNameOpenMetric, long sampleCount) {
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"sampler_count_every_periods\"} %s", totalLabelOpenMetric, totalCount)));
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"sampler_count_every_periods\"} %s", sampleNameOpenMetric, sampleCount)));
	}
	
	private void assertErrorsCount(String actualOpenMetrics, String totalLabelOpenMetric,  long totalCount, String sampleNameOpenMetric, long sampleCount) {
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"error\"} %s", totalLabelOpenMetric, totalCount)));
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"error\"} %s", sampleNameOpenMetric, sampleCount)));
	}
	
	private void assertErrorsCountEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric,  long totalCount, String sampleNameOpenMetric, long sampleCount) {
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"error_every_periods\"} %s", totalLabelOpenMetric, totalCount)));
		assertTrue(actualOpenMetrics.contains(String.format("%s_total{count=\"error_every_periods\"} %s", sampleNameOpenMetric, sampleCount)));
	}
	
	private void assertThroughput(String actualOpenMetrics, String totalLabelOpenMetric, long totalThrouput, String sampleNameOpenMetric, long sampleThroughput) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_throughput " + totalThrouput));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_throughput " + sampleThroughput));
	}
	
	private void assertThroughputEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, long totalThrouput, String sampleNameOpenMetric, long sampleThroughput) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_throughput_every_periods " + totalThrouput));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_throughput_every_periods " + sampleThroughput));
	}
	
	private void assertThreadsCount(String actualOpenMetrics, String totalLabelOpenMetric, long totalThreads, String sampleNameOpenMetric, long sampleThreads) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_threads " + totalThreads));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_threads " + sampleThreads));
	}
	
	private void assertThreadsCountEveryPeriods(String actualOpenMetrics, String totalLabelOpenMetric, long totalThreads, String sampleNameOpenMetric, long sampleThreads) {
		assertTrue(actualOpenMetrics.contains(totalLabelOpenMetric + "_threads_every_periods " + totalThreads));
		assertTrue(actualOpenMetrics.contains("spl_" + sampleNameOpenMetric + "_threads_every_periods " + sampleThreads));
	}
}
