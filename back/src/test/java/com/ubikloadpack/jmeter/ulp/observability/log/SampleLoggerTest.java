package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
		this.micrometerRegistry = new MicrometerRegistry(TOTAL_lABEL, 50, 90, 95, LOG_FREQUENCY, sampleLogger, 3000);
	}
	
	@AfterEach
	public void tearDown() {
		this.sampleLogger.clear();
		this.micrometerRegistry.close();
	}
	
	@Test
	public void whenASampleLogIsCreatedExpectItsOpenMetricsFormat() throws Exception {
		int groupThreads = 1;
		long startTime = 0; // millisecond
		long endTime = 1000; // millisecond
		long responseTime = endTime - startTime; 
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample", startTime, endTime);
		
		micrometerRegistry.addResponse(responseResult);
		Date creationDate = new Date();
		
		SampleLog sampleLog = micrometerRegistry.makeLog("spl_sample", creationDate);
		SampleLog totalLabelLog = micrometerRegistry.makeLog(Util.makeMicrometerName(TOTAL_lABEL), creationDate);
		this.sampleLogger.add(sampleLog);
		this.sampleLogger.add(totalLabelLog);
		
		String openMetrics = this.sampleLogger.openMetrics(null, false);
		assertTrue(openMetrics != null);
		
		String samplelogMetrics = Helper.getExpectedOpenMetrics(sampleLog);
		String totalLabellogMetrics = Helper.getExpectedOpenMetrics(totalLabelLog);
		
		assertTrue(openMetrics.contains(samplelogMetrics)); // assert that the metrics of the sample are included in the generated openMetrics 
		assertTrue(openMetrics.contains(totalLabellogMetrics)); // assert that the metrics of the totalLabel are included in the generated openMetrics 
	}
	
	@Test
	public void whenSampleNameIsNotCorrectExpectNull() {
		int groupThreads = 1;
		long startTime = 0; // millisecond
		long endTime = 1000; // millisecond
		long responseTime = endTime - startTime; 
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, groupThreads, groupThreads, "sample", startTime, endTime);
		
		micrometerRegistry.addResponse(responseResult);
		Date creationDate = new Date();
		
		// we try to make a log of the sample when the given name is not correct. 
		// Remember the label of a sample should be prefixed by "spl_"
		SampleLog sampleLog = micrometerRegistry.makeLog(responseResult.getSamplerLabel(), creationDate);
		assertNull(sampleLog);
		
		// when the total label is not formatted as Micrometer name
		SampleLog totalLabel = micrometerRegistry.makeLog(TOTAL_lABEL, creationDate);
		assertNull(totalLabel);
	}


}
