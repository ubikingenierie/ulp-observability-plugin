package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.ErrorTypeInfo;
import com.ubikloadpack.jmeter.ulp.observability.util.Helper;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class SampleLogTest {
	
	@Test
	@DisplayName("When converting the sample log to OpenMetrics format, expect the output to match the OpenMetrics format")
	public void whenSampleLogIsConvertedToOpenMetricsFormatExpectValidOutput() throws Exception {
        ValueAtPercentile[] pct = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 200), new ValueAtPercentile(0.9, 500), new ValueAtPercentile(0.95, 600) };
        ValueAtPercentile[] pctTotal = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 100), new ValueAtPercentile(0.9, 300), new ValueAtPercentile(0.95, 400) };

        SampleLog sampleLog = new SampleLog("testSample", new Date(), 5L, 1L, pct, 2500L, 500.0, 600L, 5.0, 1L, 5L, 600L, 500.0D, 1L, Optional.empty(), 5.0D, pctTotal, 1L);

        String actualOpenMetrics = sampleLog.toOpenMetricsString();
        
        assertFalse(actualOpenMetrics.isEmpty(), "The returned openMetrics shouldn't be empty in any way.");
        assertEquals(Helper.getExpectedOpenMetrics(sampleLog), actualOpenMetrics, "The returned OpenMetrics does not match the expected format");
	}
	
	@Test
	@DisplayName("When there are failed sample, expect the OpenMetrics format contains the top errors metrics")
	public void whenThereAreFailedSamplesExpectOutputContainsTopErrors() throws Exception {
        ValueAtPercentile[] pct = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 200), new ValueAtPercentile(0.9, 500), new ValueAtPercentile(0.95, 600) };
        ValueAtPercentile[] pctTotal = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 100), new ValueAtPercentile(0.9, 300), new ValueAtPercentile(0.95, 400) };
        
        List<ErrorTypeInfo> topErrors =  Arrays.asList(new ErrorTypeInfo("404", 1));
        SampleLog sampleLog = new SampleLog("testSample", new Date(), 5L, 1L, pct, 2500L, 500.0, 600L, 5.0, 1L, 5L, 600L, 500.0D, 1L, Optional.of(topErrors), 5.0D, pctTotal, 1L);

        String actualOpenMetrics = sampleLog.toOpenMetricsString();
        
        assertFalse(actualOpenMetrics.isEmpty(), "The returned openMetrics shouldn't be empty in any way.");
        assertEquals(Helper.getExpectedOpenMetricsWithTopErrors(sampleLog), actualOpenMetrics, "The returned OpenMetrics does not match the expected format");
	}

}
