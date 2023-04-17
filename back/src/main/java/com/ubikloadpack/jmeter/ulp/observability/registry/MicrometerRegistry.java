package com.ubikloadpack.jmeter.ulp.observability.registry;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Registry class for managing sample records and metrics calculations.
 * @author Valentin ZELIONII
 *
 */
public class MicrometerRegistry {
	/**
	 * Main sample registry containing
	 *  sample records of each thread group + total.
	 */
	private MeterRegistry registry = new SimpleMeterRegistry();

	/**
	 * Record count registry for each thread group + total.
	 */
	private MeterRegistry totalReg =
			new SimpleMeterRegistry();
	
	/**
	 * Total metrics label.
	 */
	private String totalLabel;
	
	/**
	 * Log frequency.
	 */
	private Integer logFrequency;

	/**
	 * Sample record logger.
	 */
	private SampleLogger logger;
	
    /**
     * Creates new Micrometer registery
     * 
     * @param totalLabel Label assigned for total metrics
     * @param pct1 First percentile
     * @param pct2 Second percentile
     * @param pct3 Third percentile
     * @param pctPrecision Percentile precision
     * @param logFrequency Log frequency 
     * @param logger Metrics logger
     */
	public MicrometerRegistry(
			String totalLabel,
			Integer pct1,
			Integer pct2,
			Integer pct3,
			Integer pctPrecision,
			Integer logFrequency,
			SampleLogger logger
			) {
		this.registry = new SimpleMeterRegistry();
		this.totalReg = new SimpleMeterRegistry();
		this.totalLabel = Util.makeMicrometerName(totalLabel);
		this.logFrequency = logFrequency;
		this.logger = logger;
		this.registry.config().meterFilter(createMeterFilter(pctPrecision, pct1, pct2, pct3));
		this.totalReg.config().meterFilter(createMeterFilter(pctPrecision, pct1, pct2, pct3));
	}
	
	private MeterFilter createMeterFilter(Integer pctPrecision, Integer pct1, Integer pct2, Integer pct3) {
		return new MeterFilter() {
			@Override
			public DistributionStatisticConfig configure(
					final Meter.Id id,
					final DistributionStatisticConfig config
					) {
				if(id.getType().equals(
						Type.DISTRIBUTION_SUMMARY
						)) {
					return DistributionStatisticConfig
							.builder()
							.percentilePrecision(
									pctPrecision
									)
							.percentiles(
									(float)pct1/100.0,
									(float)pct2/100.0,
									(float)pct3/100.0
									)
							.percentilesHistogram(true)
							.build()
							.merge(config);
				}
				return config;
			}
		};
	}
	
	/**
	 * Clears and closes registry
	 */
	public void close() {
		this.registry.clear();
		this.registry.close();
		this.totalReg.clear();
		this.totalReg.close();
	}

	/**
	 * Adds new sample record to registry
	 * 
	 * @param result Occurred sample result
	 */
	public synchronized void addResponse(ResponseResult result) {
		if(this.registry.isClosed() || this.totalReg.isClosed()) {
			System.out.println("CLOOOOOOOOOOOOOOOOOOOOOOOOOOOOSED !");
			return;
		}

		String threadTag = Util.makeMicrometerName(result.getThreadGroupLabel());
		String samplerTag = "spl_"+Util.makeMicrometerName(result.getSamplerLabel());
		
		this.registry.summary("summary.response", "sample", threadTag).record(result.getResponseTime());
		this.registry.summary("summary.response", "sample", this.totalLabel).record(result.getResponseTime());
		this.registry.summary("summary.response", "sample", samplerTag).record(result.getResponseTime());
	
		this.registry.counter("count.threads", "sample", threadTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", threadTag).count());
		this.registry.counter("count.threads", "sample", this.totalLabel).increment(
				result.getAllThreads() - (int) this.registry.counter("count.threads", "sample", this.totalLabel).count());
		this.registry.counter("count.threads", "sample", samplerTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", threadTag).count());
	
		if(result.hasError()) {
			this.registry.counter("count.error", "sample", threadTag).increment();
			this.registry.counter("count.error", "sample", this.totalLabel).increment();
			this.registry.counter("count.error", "sample", samplerTag).increment();
		}
		
		this.totalReg.counter("count.total", "sample", threadTag).increment();
		this.totalReg.counter("count.total", "sample", this.totalLabel).increment();
		this.totalReg.counter("count.total", "sample", samplerTag).increment();
	}
	
	
	/**
	 * Creates new period log from currently registered records
	 * 
	 * @param name Thread group to log 
	 * @param timestamp Log timestamp
	 * @return New log with recorded period metrics
	 */
	public SampleLog makeLog(String name, Date timestamp) {
		// Current period summary
		DistributionSummary summary = this.registry.find("summary.response").tag("sample", name).summary();
		// Cumulated periods summaries
		DistributionSummary maxTotalSummary = totalReg.find("summary.response.max").tag("sample", name).summary();
		DistributionSummary meanTotalSummary = totalReg.find("summary.response.mean").tag("sample", name).summary();
		
		long maxTotalResponseTime = (long) maxTotalSummary.max();
		// TODO le mean tombera proche de celui du resultat final, mais ce sera pas le bon parcequ'il est pas
		// pondéré selon le nombre d'appels de samplers, mais sur le nombre d'intervalles (ce qui est incorrecte)
		long meanTotalResponseTime = (long) meanTotalSummary.mean();
		long totalErrorCounter = (long) totalReg.counter("count.error","sample",name).count();
		
		System.out.println("#######################################");
		System.out.println("max total response time for '" + name + "' " + maxTotalResponseTime);
		System.out.println("mean total response time for '" + name + "' " + meanTotalResponseTime);
		System.out.println("error total for '" + name + "' " + totalErrorCounter);
		System.out.println("total samples count : " + totalReg.counter("count.total", "sample", name).count());
		
		return summary == null ? null : new SampleLog(
				Util.micrometerToOpenMetrics(name),
				timestamp,
				(long) this.totalReg.counter("count.total","sample",name).count(),
				(long) summary.count(),
				(long) this.registry.counter("count.error","sample",name).count(),
				summary.takeSnapshot().percentileValues(),
				(long) summary.totalAmount(),
				(long) summary.mean(),
				(long) summary.max(),
				(long) summary.count() / this.logFrequency,
				(long) registry.counter("count.threads","sample",name).count()
				);
	}
	
	private synchronized void refreshTotalMetrics(String samplerName) {
		if(registry.isClosed() || totalReg.isClosed()) {
			return;
		}
		
		DistributionSummary currentIntervalSummary = registry.find("summary.response").tag("sample", samplerName).summary();
		
		if(currentIntervalSummary != null) {
			totalReg.summary("summary.response.max", "sample", samplerName).record(currentIntervalSummary.max());
			totalReg.summary("summary.response.mean", "sample", samplerName).record((long) currentIntervalSummary.mean());
			Long currentPeriodErrors = (long) registry.counter("count.error","sample", samplerName).count();
			totalReg.counter("count.error", "sample", samplerName).increment(currentPeriodErrors);
			// TODO le count perdiod, faut le faire autrement
			// totalReg.counter("count.period", "sample", samplerName).increment();
		}
//		totalReg.summary("summary.throughput", );
//		(long) summary.count() / this.logFrequency,
		
		
	}

	/**
	 * Logs entire registry and then resets it for next period
	 * 
	 * @return Updated logger
	 */
	public void logAndReset() {
		logAndReset(getSampleNames());
	}
	
	/**
	 * Get metrics debug log summary.
	 * @return Record debug logs.
	 */
	public String guiLog() {
		return this.logger.guiLog();
	}
	
	
	/**
	 * Logs selected thread groups without resetting
	 * 
	 * @param names List of thread groups to log
	 * @return Updated logger
	 */
	public void log(List<String> names) {
		Date timestamp = new Date();
		names.forEach(name ->{
			refreshTotalMetrics(name);
			this.logger.add(makeLog(name, timestamp));
		});
	}
	
	/**
	 * Logs selected thread groups and then resets them for next period
	 * 
	 * @return Updated logger
	 */
	public void logAndReset(List<String> names) {
		this.log(names);
		this.registry.clear();
	}

	/**
	 * @return List of recorded thread group names + total
	 */
	public List<String> getSampleNames() {
		return this.registry.find("summary.response").summaries()                  
				.stream().map(summary -> summary.getId().getTag("sample"))
				.collect(Collectors.toList());
	}
	
}
