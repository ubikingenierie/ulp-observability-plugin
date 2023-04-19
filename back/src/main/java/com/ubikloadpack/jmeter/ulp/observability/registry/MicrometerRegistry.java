package com.ubikloadpack.jmeter.ulp.observability.registry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Registry class for managing sample records and metrics calculations.
 * @author Valentin ZELIONII
 *
 */
public class MicrometerRegistry {
	
	private static final Logger LOG = LoggerFactory.getLogger(MicrometerRegistry.class);
	
	/**
	 * Current period count
	 */
	private int periodCount = 0;
	
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
	 * Start of the test plan instant
	 */
	private Instant startInstant;
	
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
			SampleLogger logger,
			Instant startInstant
			) {
		this.registry = new SimpleMeterRegistry();
		this.totalReg = new SimpleMeterRegistry();
		this.totalLabel = Util.makeMicrometerName(totalLabel);
		this.logFrequency = logFrequency;
		this.logger = logger;
		this.registry.config().meterFilter(createMeterFilter(pctPrecision, pct1, pct2, pct3));
		this.totalReg.config().meterFilter(createMeterFilter(pctPrecision, pct1, pct2, pct3));
		this.startInstant = startInstant;
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
	
		this.registry.counter("count.threads", "sample", threadTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", threadTag).count());
		this.registry.counter("count.threads", "sample", this.totalLabel).increment(
				result.getAllThreads() - (int) this.registry.counter("count.threads", "sample", this.totalLabel).count());
		this.registry.counter("count.threads", "sample", samplerTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", threadTag).count());
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
		DistributionSummary totalSummary = totalReg.find("summary.response").tag("sample", name).summary();
		
		long maxTotalResponseTime = (long) totalSummary.max();
		long totalErrorCounter = (long) totalReg.counter("count.error","sample",name).count();
		Double averageTotalResponseTime = totalReg.counter("accumulate.response", "sample", name).count() /
				totalReg.counter("count.total", "sample", name).count();
		
		double timeSinceFirstSampleCallInSeconds = (startAndEndDatesOfSamplers.get(name).getRight() - startAndEndDatesOfSamplers.get(name).getLeft()) / 1000d; 
		Double totalThroughput = this.totalReg.counter("count.total","sample",name).count() / (timeSinceFirstSampleCallInSeconds);
		ValueAtPercentile[] totalPercentiles = summary.takeSnapshot().percentileValues();
		
		System.out.println("#######################################");
		for(ValueAtPercentile pc : totalPercentiles) {
			System.out.println(name+" {quantile=\""+(long)(pc.percentile()*100)+"\"} "+ pc.value());
		}	
		System.out.println("total Throughput for '" + name + "' " + totalThroughput + " time since begin : " + timeSinceFirstSampleCallInSeconds);
		System.out.println("max total response time for '" + name + "' " + maxTotalResponseTime);
		System.out.println("mean total response time for '" + name + "' " + averageTotalResponseTime);
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
				summary.mean(),
				(long) summary.max(),
				(long) summary.count() / this.logFrequency,
				(long) registry.counter("count.threads","sample",name).count(),
				maxTotalResponseTime,
				averageTotalResponseTime
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
		this.periodCount++;
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
