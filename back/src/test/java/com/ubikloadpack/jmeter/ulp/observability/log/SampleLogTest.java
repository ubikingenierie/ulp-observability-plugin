package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.util.Helper;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class SampleLogTest {
	
	@Test
	public void whenSampleLogIsConvertedToOpenMetricsFormatExpectValidOutput() throws Exception {
        ValueAtPercentile[] pct = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 200), new ValueAtPercentile(0.9, 500), new ValueAtPercentile(0.95, 600) };
        ValueAtPercentile[] pctTotal = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 100), new ValueAtPercentile(0.9, 300), new ValueAtPercentile(0.95, 400) };

        SampleLog sampleLog = new SampleLog("testSample", new Date(), 5L, 1L, pct, 2500L, 500.0, 600L, 5.0, 1L, 5L, 600L, 500.0D, 1L, 5.0D, pctTotal, 1L);

        String actualOpenMetrics = sampleLog.toOpenMetricsString();
        
        assertFalse(actualOpenMetrics.isEmpty());
        assertEquals(Helper.getExpectedOpenMetrics(sampleLog), actualOpenMetrics);
	}

}
