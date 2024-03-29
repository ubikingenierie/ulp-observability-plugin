package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.util.Helper;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

public class SampleLoggerTest {
	private static final String TOTAL_lABEL = "total_info";
	private static final int LOG_FREQUENCY = 10; 
	
	private SampleLogger sampleLogger;
	private MicrometerRegistry micrometerRegistry;
	
	@BeforeEach
	public void setUp() {
		this.sampleLogger = new SampleLogger(TOTAL_lABEL);
		this.micrometerRegistry = new MicrometerRegistry(TOTAL_lABEL, 50, 90, 95, LOG_FREQUENCY, 0, sampleLogger, 3000);
	}
	
	@AfterEach
	public void tearDown() {
		this.sampleLogger.clear();
		this.micrometerRegistry.close();
	}
	
	@Test
	@DisplayName("When a sample log is created and saved through the logger, expect it to be nicely formatted in openMetrics format")
	public void whenASampleLogIsCreatedExpectItsOpenMetricsFormat() throws Exception {
		this.addResponseResult("sample", 1000L);
		SampleLog sampleLog = this.createAndRecordSampleLog("spl_sample", new Date());
		SampleLog totalLabelLog = this.createAndRecordSampleLog(Util.makeMicrometerName(TOTAL_lABEL), new Date());
		
		String openMetrics = this.sampleLogger.openMetrics(null, false);
		assertFalse(openMetrics.isEmpty(), "If the returned openMetrics is empty so no SampleLog was added to the SampleLogger.");
		assertTrue(openMetrics.contains(Helper.getExpectedOpenMetrics(sampleLog)), "The expected openMEtrics format doesn't match the returned openMetrics."); // assert that the metrics of the sample are included in the generated openMetrics 
		assertTrue(openMetrics.contains(Helper.getExpectedOpenMetrics(totalLabelLog)), "The expected openMEtrics format doesn't match the returned openMetrics."); // assert that the metrics of the totalLabel are included in the generated openMetrics 
	}
	
	@Test
	@DisplayName("When the name of the sample is not correct when it's passed to the makeLog, expect null")
	public void whenSampleNameIsNotCorrectExpectNull() {
		this.addResponseResult("sample", 1000L);	
		// we try to make a log of the sample when the given name is not correct. 
		// Remember the label of a sample should be prefixed by "spl_"
		SampleLog sampleLog = micrometerRegistry.makeLog("sample", new Date());
		assertNull(sampleLog, "The given sample's name was 'sample' and wasn't prefixed with 'spl_', so should be null.");
		
		// when the total label is not formatted as Micrometer name
		SampleLog totalLabel = micrometerRegistry.makeLog(TOTAL_lABEL, new Date());
		assertNull(totalLabel, "The given sample's name was 'total_label' and wasn't formatted as micrometer format, so should be null.");
	}
	
	private void addResponseResult(String samplerLabel, long duration) {
	    ResponseResult responseResult = new ResponseResult("groupe1", duration, false, "", 1, 1, samplerLabel, 0L, duration);
	    micrometerRegistry.addResponse(responseResult);
	}

	private SampleLog createAndRecordSampleLog(String samplerLabel, Date timeStamp) {
		SampleLog sampleLog = micrometerRegistry.makeLog(samplerLabel, timeStamp);
	    this.sampleLogger.add(sampleLog);
	    return sampleLog;
	}


}
