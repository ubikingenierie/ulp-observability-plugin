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
        String sampleName = "testSample";
        Date timeStamp = new Date();
        Long samplerCount = 5L;
        Long error = 1L;
        ValueAtPercentile[] pct = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 200), new ValueAtPercentile(0.9, 500), new ValueAtPercentile(0.95, 600) };
        Long sum = 2500L;
        Double avg = 500.0;
        Long max = 600L;
        Double throughput = 5.0;
        Long threads = 1L;
        Long samplerCountTotal = 5L;
        Long maxTotal = 600L;
        Double avgTotal = 500.0;
        Long errorTotal = 1L;
        Double throughputTotal = 5.0;
        ValueAtPercentile[] pctTotal = new ValueAtPercentile[] { new ValueAtPercentile(0.5, 100), new ValueAtPercentile(0.9, 300), new ValueAtPercentile(0.95, 400) };
        Long threadsTotal = 1L;

        SampleLog sampleLog = new SampleLog(sampleName, timeStamp, samplerCount, error, pct, sum, avg, max, throughput, threads, 
            samplerCountTotal, maxTotal, avgTotal, errorTotal, throughputTotal, pctTotal, threadsTotal);

        String actualOpenMetrics = sampleLog.toOpenMetricsString();
        
        assertFalse(actualOpenMetrics.isEmpty());
        assertEquals(Helper.getExpectedOpenMetrics(sampleLog), actualOpenMetrics);
	}

}
