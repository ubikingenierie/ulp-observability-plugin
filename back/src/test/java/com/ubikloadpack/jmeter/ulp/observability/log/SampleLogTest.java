package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLoggerTest;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class SampleLogTest {
	
	@Test
	public void testToOpenMetricsString() {
        // Arrange
        String sampleName = "testSample";
        Date timeStamp = new Date();
        Long samplerCount = 5L;
        Long error = 1L;
        ValueAtPercentile[] pct = new ValueAtPercentile[] { new ValueAtPercentile(0.9, 500), new ValueAtPercentile(0.95, 600) };
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
        ValueAtPercentile[] pctTotal = new ValueAtPercentile[] { new ValueAtPercentile(0.9, 300), new ValueAtPercentile(0.95, 400) };
        Long threadsTotal = 1L;

        SampleLog sampleLog = new SampleLog(sampleName, timeStamp, samplerCount, error, pct, sum, avg, max, throughput, threads, 
            samplerCountTotal, maxTotal, avgTotal, errorTotal, throughputTotal, pctTotal, threadsTotal);

        String openMetrics = sampleLog.toOpenMetricsString();
        
        assertTrue(openMetrics != null);
        assertEquals(getExpectedString(sampleLog), openMetrics);
	}
	
	public String getExpectedString(SampleLog sampleLog) {
		long quantile1 = (long) (sampleLog.getPct()[0].percentile() * 100);
		long quantile2 = (long) (sampleLog.getPct()[1].percentile() * 100);
		
		long pct1 = (long) sampleLog.getPct()[0].value();
		long pct2 = (long) sampleLog.getPct()[1].value();
		
		long pctTotal1 = (long) sampleLog.getPctTotal()[0].value();
		long pctTotal2 = (long) sampleLog.getPctTotal()[1].value();
		
		long timestamp = sampleLog.getTimeStamp().getTime();
		
	    return "# TYPE " + sampleLog.getSampleName() + "_pct summary\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_pct milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_pct Response percentiles\n" +        
	        sampleLog.getSampleName() + "_pct{quantile=\"" + quantile1 + "\"} " + pct1 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile=\"" + quantile2 + "\"} " + pct2 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile_every_periods=\"" + quantile1 + "\"} " + pctTotal1 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile_every_periods=\"" + quantile2 + "\"} " + pctTotal2 + "\n" +
	        sampleLog.getSampleName() + "_pct_sum " + sampleLog.getSum() + "\n" +
	        sampleLog.getSampleName() + "_pct_created " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_max gauge\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_max milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_max Max response times\n" +
	        sampleLog.getSampleName() + "_max " + sampleLog.getMax() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_max_every_periods gauge\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_max_every_periods milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_max_every_periods Total max response times\n" +
	        sampleLog.getSampleName() + "_max_every_periods " + sampleLog.getMaxTotal() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_avg gauge\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_avg milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_avg Average response times\n" +
	        sampleLog.getSampleName() + "_avg " + sampleLog.getAvg() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_avg_every_periods gauge\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_avg_every_periods milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_avg_every_periods Total average response times\n" +
	        sampleLog.getSampleName() + "_avg_every_periods " + sampleLog.getAvgTotal() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_total gauge\n" +
	        "# HELP " + sampleLog.getSampleName() + "_total Response count\n" +
	        sampleLog.getSampleName() + "_total{count=\"sampler_count_every_periods\"} " + sampleLog.getSamplerCountTotal() + " " + timestamp + "\n" +
	        sampleLog.getSampleName() + "_total{count=\"sampler_count\"} " + sampleLog.getSamplerCount() + " " + timestamp + "\n" +
	        sampleLog.getSampleName() + "_total{count=\"error\"} " + sampleLog.getError() + " " + timestamp + "\n" +
	        sampleLog.getSampleName() + "_total{count=\"error_every_periods\"} " + sampleLog.getErrorTotal() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_throughput gauge\n" +
	        "# HELP " + sampleLog.getSampleName() + "_throughput Responses per second\n" +
	        sampleLog.getSampleName() + "_throughput " + sampleLog.getThroughput() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_throughput_every_periods gauge\n" +
	        "# HELP " + sampleLog.getSampleName() + "_throughput_every_periods Total responses per second\n" +
	        sampleLog.getSampleName() + "_throughput_every_periods " + sampleLog.getThroughputTotal() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_threads counter\n" +
	        "# HELP " + sampleLog.getSampleName() + "_threads Current period Virtual user count\n" +
	        sampleLog.getSampleName() + "_threads " + sampleLog.getThreads() + " " + timestamp + "\n" +
	        "# TYPE " + sampleLog.getSampleName() + "_threads_every_periods counter\n" +
	        "# HELP " + sampleLog.getSampleName() + "_threads_every_periods Max number of virtual user count\n" +
	        sampleLog.getSampleName() + "_threads_every_periods " + sampleLog.getThreadsTotal() + " " + timestamp + "\n";
	}

}
