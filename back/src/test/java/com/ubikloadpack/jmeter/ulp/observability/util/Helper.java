package com.ubikloadpack.jmeter.ulp.observability.util;

import java.text.MessageFormat;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class Helper {
	public static String getExpectedOpenMetrics(SampleLog sampleLog)  {
	    // Construct metrics strings with a descriptive template
	    String template = buildOpenMetricsTemplate();

	    // Format template with percentile values and sample log values
	    return MessageFormat.format(template, extractMetricsValues(sampleLog));
	}

	private static String buildOpenMetricsTemplate() {
	    // Construct a template for metrics using placeholders for values to be inserted later
	    return (
		    "# TYPE {0}_pct summary\n" +
	        "# UNIT {0}_pct milliseconds\n" +
	        "# HELP {0}_pct Response percentiles\n" +        
	        "{0}_pct'{'quantile=\"{2}\"'}' {3}\n" +
	        "{0}_pct'{'quantile=\"{4}\"'}' {5}\n" +
	        "{0}_pct'{'quantile=\"{6}\"'}' {7}\n" +
	        "{0}_pct'{'quantile_every_periods=\"{2}\"'}' {8}\n" +
	        "{0}_pct'{'quantile_every_periods=\"{4}\"'}' {9}\n" +
	        "{0}_pct'{'quantile_every_periods=\"{6}\"'}' {10}\n" +
	        "{0}_pct_sum {11}\n" +
	        "{0}_pct_created {1}\n" +
	        "# TYPE {0}_max gauge\n" +
	        "# UNIT {0}_max milliseconds\n" +
	        "# HELP {0}_max Max response times\n" +
	        "{0}_max {12} {1}\n" +
	        "# TYPE {0}_max_every_periods gauge\n" +
	        "# UNIT {0}_max_every_periods milliseconds\n" +
	        "# HELP {0}_max_every_periods Total max response times\n" +
	        "{0}_max_every_periods {13} {1}\n" +
	        "# TYPE {0}_avg gauge\n" +
	        "# UNIT {0}_avg milliseconds\n" +
	        "# HELP {0}_avg Average response times\n" +
	        "{0}_avg {14} {1}\n" +
	        "# TYPE {0}_avg_every_periods gauge\n" +
	        "# UNIT {0}_avg_every_periods milliseconds\n" +
	        "# HELP {0}_avg_every_periods Total average response times\n" +
	        "{0}_avg_every_periods {15} {1}\n" +
	        "# TYPE {0}_total gauge\n" +
	        "# HELP {0}_total Response count\n" +
	        "{0}_total'{'count=\"sampler_count_every_periods\"'}' {16} {1}\n" +
	        "{0}_total'{'count=\"sampler_count\"'}' {17} {1}\n" +
	        "{0}_total'{'count=\"error\"'}' {18} {1}\n" +
	        "{0}_total'{'count=\"error_every_periods\"'}' {19} {1}\n" +
	        "# TYPE {0}_throughput gauge\n" +
	        "# HELP {0}_throughput Responses per second\n" +
	        "{0}_throughput {20} {1}\n" +
	        "# TYPE {0}_throughput_every_periods gauge\n" +
	        "# HELP {0}_throughput_every_periods Total responses per second\n" +
	        "{0}_throughput_every_periods {21} {1}\n" +
	        "# TYPE {0}_threads counter\n" +
	        "# HELP {0}_threads Current period Virtual user count\n" +
	        "{0}_threads {22} {1}\n" +
	        "# TYPE {0}_threads_every_periods counter\n" +
	        "# HELP {0}_threads_every_periods Max number of virtual user count\n" +
	        "{0}_threads_every_periods {23} {1}\n");
	}

	private static Object[] extractMetricsValues(SampleLog sampleLog) {
		ValueAtPercentile[] percentiles = sampleLog.getPct();
		ValueAtPercentile[] totalPercentiles = sampleLog.getPctTotal();
		// Extract percentile and total percentile values
	    long[] quantiles = new long[percentiles.length];
	    long[] pcts = new long[percentiles.length];
	    long[] totalPcts = new long[totalPercentiles.length];

	    for (int i = 0; i < percentiles.length; i++) {
	        quantiles[i] = (long) (percentiles[i].percentile() * 100);
	        pcts[i] = (long) percentiles[i].value();
	    }

	    for (int i = 0; i < totalPercentiles.length; i++) {
	        totalPcts[i] = (long) totalPercentiles[i].value();
	    }

	    // Return array of values to be inserted into the template
	    return new Object[] {
	        sampleLog.getSampleName(),
	        sampleLog.getTimeStamp().getTime(),
	        quantiles[0], pcts[0],
	        quantiles[1], pcts[1],
	        quantiles[2], pcts[2],
	        totalPcts[0], totalPcts[1], totalPcts[2],
	        sampleLog.getSum(),
	        sampleLog.getMax(),
	        sampleLog.getMaxTotal(),
	        sampleLog.getAvg(),
	        sampleLog.getAvgTotal(),
	        sampleLog.getSamplerCountTotal(),
	        sampleLog.getSamplerCount(),
	        sampleLog.getError(),
	        sampleLog.getErrorTotal(),
	        sampleLog.getThroughput(),
	        sampleLog.getThroughputTotal(),
	        sampleLog.getThreads(),
	        sampleLog.getThreadsTotal()
	    };
	}

}
