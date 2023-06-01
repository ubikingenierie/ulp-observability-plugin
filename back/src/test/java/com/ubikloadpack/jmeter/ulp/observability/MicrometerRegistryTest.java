package com.ubikloadpack.jmeter.ulp.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class MicrometerRegistryTest {
	@Test
	public void testMonoThreadOfMicrometerRegistry() {
		String totalLabel = "total_info";
		SampleLogger sampleLogger = new SampleLogger(totalLabel);

		MicrometerRegistry micrometerRegistry = new MicrometerRegistry(totalLabel, 50, 90, 95, 10, sampleLogger, 3000);
		
		long responseTime = 500L; // Fix response time of that sample at 500 milliseconds
		Long startTime = System.currentTimeMillis();
		Long endTime = startTime + 1000;
		ResponseResult responseResult = new ResponseResult("groupe1", responseTime, false, 1, 1, "sample", startTime, endTime);
		
		micrometerRegistry.addResponse(responseResult);
		Date dateCreatingLog = new Date();
		SampleLog sampleLog = micrometerRegistry.makeLog("spl_sample", dateCreatingLog);
		
		assertNotNull(sampleLog);
		assertEquals(sampleLog.getTimeStamp(), dateCreatingLog);
		assertEquals(sampleLog.getSamplerCount(), 1);
		assertEquals(sampleLog.getError(), 0);
		
		assertTrue(sampleLog.getPct().length == 3);
		for (ValueAtPercentile pct: sampleLog.getPct()) {
			assertEquals((long) pct.value(), responseTime); // Only one sample was recorded so the value of different percentiles are the same
		}
		
		assertEquals(sampleLog.getSum(), responseTime); // the sum is the same as the response time because only one request of that sample is done
		assertEquals(sampleLog.getAvg(), responseTime); 
		assertEquals(sampleLog.getMax(), responseTime);
		assertEquals(sampleLog.getThroughput(), (double) 1/10); // throughput for an interval is the number of samples per log frequency
		assertEquals(sampleLog.getThreads(), 1); // there's only one virtual user
		assertEquals(sampleLog.getSamplerCountTotal(), 1); // there's only one sample	
	}
	

}
