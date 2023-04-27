package com.ubikloadpack.jmeter.ulp.observability.registry;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

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
	
	private volatile Map<String, Pair<Long,Long>> startAndEndDatesOfSamplers = new ConcurrentHashMap<>();
	
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
			return;
		}

		// Create micrometer tags to feed with data
		String threadTag = Util.makeMicrometerName(result.getThreadGroupLabel());
		String samplerTag = "spl_"+Util.makeMicrometerName(result.getSamplerLabel());
		List<String> micrometerTags = Arrays.asList(new String[]{threadTag, samplerTag, this.totalLabel});
		
		for(String microMeterTag : micrometerTags) {
			// Save current period responses time
			this.registry.summary("summary.response", "sample", microMeterTag).record(result.getResponseTime());
			this.totalReg.summary("summary.response", "sample", microMeterTag).record(result.getResponseTime());

			// Save the first sampler that occured in time, and the last. We have to get them this way or we lack
			// precision when we calculate the Throughput. We can't use micrometer for this because we can't retrieve minimal value from
			// a registry. Because of this we handle it manually with a custom Map.
			Pair<Long, Long> startAndEndDate = startAndEndDatesOfSamplers.get(microMeterTag);
			if(startAndEndDate == null || startAndEndDate.getLeft() == null || startAndEndDate.getLeft() > result.getStartTime()) {
				Pair<Long, Long> newStartAndEndDate = Pair.of(
					result.getStartTime(), 
					(startAndEndDate == null || startAndEndDate.getRight() == null) ? null : startAndEndDate.getRight()
				);
				startAndEndDate = newStartAndEndDate;
				startAndEndDatesOfSamplers.put(microMeterTag, newStartAndEndDate);
			}
			if(startAndEndDate == null || startAndEndDate.getRight() == null || result.getEndTime() > startAndEndDate.getRight()) {
				Pair<Long, Long> newStartAndEndDate = Pair.of(
					(startAndEndDate == null || startAndEndDate.getLeft() == null) ? null : startAndEndDate.getLeft(),
					result.getEndTime() 
				);
				startAndEndDatesOfSamplers.put(microMeterTag, newStartAndEndDate);
			}
			
			// Increment error counters if there is one
			if(result.hasError()) {
				this.registry.counter("count.error", "sample", microMeterTag).increment();
				this.totalReg.counter("count.error", "sample", microMeterTag).increment();
			}
			
			// Accumulate responses time to calculate average responses time with a good precision without being heavy for the memory
			this.totalReg.counter("accumulate.response", "sample", microMeterTag).increment(result.getResponseTime());
			
			// Increment tag count
			this.totalReg.counter("count.total", "sample", microMeterTag).increment();
		}
	
		// Count thread number. The registries keep the max thread number values respectively for the period, and every periods.
		int threadGroupIncrement = result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", threadTag).count();
		this.registry.counter("count.threads", "sample", threadTag).increment(threadGroupIncrement < 0 ? 0 : threadGroupIncrement);
		int totalIncrement = result.getAllThreads() - (int) this.registry.counter("count.threads", "sample", this.totalLabel).count();
		this.registry.counter("count.threads", "sample", this.totalLabel).increment(totalIncrement < 0 ? 0 : totalIncrement);
		int samplerIncrement = result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", samplerTag).count();
		this.registry.counter("count.threads", "sample", samplerTag).increment(samplerIncrement < 0 ? 0 : samplerIncrement);
		
		int threadGroupIncrementTotal = result.getGroupThreads() - (int) this.totalReg.counter("count.threads", "sample", threadTag).count();
		this.totalReg.counter("count.threads", "sample", threadTag).increment(threadGroupIncrementTotal < 0 ? 0 : threadGroupIncrementTotal);
		int totalIncrementTotal = result.getAllThreads() - (int) this.totalReg.counter("count.threads", "sample", this.totalLabel).count();
		this.totalReg.counter("count.threads", "sample", this.totalLabel).increment(totalIncrementTotal < 0 ? 0 : totalIncrementTotal);
		int samplerIncrementTotal = result.getGroupThreads() - (int) this.totalReg.counter("count.threads", "sample", samplerTag).count();
		this.totalReg.counter("count.threads", "sample", samplerTag).increment(samplerIncrementTotal < 0 ? 0 : samplerIncrementTotal);
	}
	
	/**
	 * Creates new period log from currently registered records
	 * 
	 * @param name Thread group to log 
	 * @param timestamp Log timestamp
	 * @return New log with recorded period metrics
	 */
	public SampleLog makeLog(String name, Date timestamp) {
		DistributionSummary currentPeriodSummary = registry.find("summary.response").tag("sample", name).summary();
		DistributionSummary everyPeriodsSummary = totalReg.find("summary.response").tag("sample", name).summary();
		
		Double averageTotalResponseTime = totalReg.counter("accumulate.response", "sample", name).count() /
				totalReg.counter("count.total", "sample", name).count();
		double timeSinceFirstSampleCallInSeconds = (startAndEndDatesOfSamplers.get(name).getRight() - startAndEndDatesOfSamplers.get(name).getLeft()) / 1000d; 
		Double totalThroughput = this.totalReg.counter("count.total","sample",name).count() / (timeSinceFirstSampleCallInSeconds);
		
		return currentPeriodSummary == null ? null : new SampleLog(
				Util.micrometerToOpenMetrics(name),
				timestamp,
				// Current period data
				(long) currentPeriodSummary.count(),
				(long) registry.counter("count.error","sample",name).count(),
				currentPeriodSummary.takeSnapshot().percentileValues(),
				(long) currentPeriodSummary.totalAmount(),
				currentPeriodSummary.mean(),
				(long) currentPeriodSummary.max(),
				(double) currentPeriodSummary.count() / this.logFrequency,
				(long) registry.counter("count.threads","sample",name).count(),
				// Every periods data
				(long) totalReg.counter("count.total","sample",name).count(),
				(long) everyPeriodsSummary.max(),
				averageTotalResponseTime,
				(long) totalReg.counter("count.error","sample",name).count(), // total error count
				totalThroughput,
				everyPeriodsSummary.takeSnapshot().percentileValues(), // total percentiles
				(long) totalReg.counter("count.threads","sample",name).count()
		);
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
