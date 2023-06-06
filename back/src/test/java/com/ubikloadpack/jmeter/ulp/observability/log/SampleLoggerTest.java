package com.ubikloadpack.jmeter.ulp.observability.log;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
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
	public void testOpenMetrics_when_oneSampleLog() {
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
		
		String samplelogMetrics = generateExpectedString(sampleLog);
		String totalLabellogMetrics = generateExpectedString(totalLabelLog);
		
		assertTrue(openMetrics.contains(samplelogMetrics)); // assert that the metrics of the sample are included in the generated openMetrics 
		assertTrue(openMetrics.contains(totalLabellogMetrics)); // assert that the metrics of the totalLabel are included in the generated openMetrics 
	}
	
	@Test
	public void testOpenMetrics_when_sampleNameIsNotCorrect() {
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

	private String generateExpectedString(SampleLog sampleLog) {
		long quantile1 = (long) (sampleLog.getPct()[0].percentile() * 100);
		long quantile2 = (long) (sampleLog.getPct()[1].percentile() * 100);
		long quantile3 = (long) (sampleLog.getPct()[2].percentile() * 100);
		
		long pct1 = (long) sampleLog.getPct()[0].value();
		long pct2 = (long) sampleLog.getPct()[1].value();
		long pct3 = (long) sampleLog.getPct()[2].value();
		
		long pctTotal1 = (long) sampleLog.getPctTotal()[0].value();
		long pctTotal2 = (long) sampleLog.getPctTotal()[1].value();
		long pctTotal3 = (long) sampleLog.getPctTotal()[2].value();
		
		long timestamp = sampleLog.getTimeStamp().getTime();
		
	    return "# TYPE " + sampleLog.getSampleName() + "_pct summary\n" +
	        "# UNIT " + sampleLog.getSampleName() + "_pct milliseconds\n" +
	        "# HELP " + sampleLog.getSampleName() + "_pct Response percentiles\n" +        
	        sampleLog.getSampleName() + "_pct{quantile=\"" + quantile1 + "\"} " + pct1 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile=\"" + quantile2 + "\"} " + pct2 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile=\"" + quantile3 + "\"} " + pct3 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile_every_periods=\"" + quantile1 + "\"} " + pctTotal1 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile_every_periods=\"" + quantile2 + "\"} " + pctTotal2 + "\n" +
	        sampleLog.getSampleName() + "_pct{quantile_every_periods=\"" + quantile3 + "\"} " + pctTotal3 + "\n" +
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
