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
	@DisplayName("when a sample log is created expect its open metrics format")
	public void whenASampleLogIsCreatedExpectItsOpenMetricsFormat() throws Exception {
		this.addResponseResult("sample", 1000L);
		SampleLog sampleLog = this.createAndRecordSampleLog("spl_sample", new Date());
		SampleLog totalLabelLog = this.createAndRecordSampleLog(Util.makeMicrometerName(TOTAL_lABEL), new Date());
		
		String openMetrics = this.sampleLogger.openMetrics(null, false);
		assertFalse(openMetrics.isEmpty());
		assertTrue(openMetrics.contains(Helper.getExpectedOpenMetrics(sampleLog))); // assert that the metrics of the sample are included in the generated openMetrics 
		assertTrue(openMetrics.contains(Helper.getExpectedOpenMetrics(totalLabelLog))); // assert that the metrics of the totalLabel are included in the generated openMetrics 
	}
	
	@Test
	@DisplayName("When sample name is not correct expect null")
	public void whenSampleNameIsNotCorrectExpectNull() {
		this.addResponseResult("sample", 1000L);	
		// we try to make a log of the sample when the given name is not correct. 
		// Remember the label of a sample should be prefixed by "spl_"
		SampleLog sampleLog = micrometerRegistry.makeLog("sample", new Date());
		assertNull(sampleLog);
		
		// when the total label is not formatted as Micrometer name
		SampleLog totalLabel = micrometerRegistry.makeLog(TOTAL_lABEL, new Date());
		assertNull(totalLabel);
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
