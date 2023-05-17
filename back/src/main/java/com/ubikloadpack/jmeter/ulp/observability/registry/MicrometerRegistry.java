package com.ubikloadpack.jmeter.ulp.observability.registry;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.micrometer.core.instrument.Counter;
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
    private static final Logger LOG = LoggerFactory.getLogger(MicrometerRegistry.class);
	/**
	 * Contains data of current interval
	 */
	private MeterRegistry intervalRegistry = new SimpleMeterRegistry();

	/**
	 * Contains data of every intervals
	 */
	private MeterRegistry summaryRegistry = new SimpleMeterRegistry();
	
	/**
	 * Total metrics label.
	 */
	private String totalLabel;
	
	/**
	 * Log frequency.
	 */
	private Integer logFrequency;
	
	/**
	 * Number of the top errors.
	 */
	private Integer topErrors;

	/**
	 * Sample record logger.
	 */
	private SampleLogger logger;
	
	private Map<String, Pair<Long,Long>> startAndEndDatesOfSamplers = new ConcurrentHashMap<>();
	
    /**
     * Creates new Micrometer registery
     * 
     * @param totalLabel Label assigned for total metrics
     * @param pct1 First percentile
     * @param pct2 Second percentile
     * @param pct3 Third percentile
     * @param logFrequency Log frequency 
     * @param integer 
     * @param logger Metrics logger
     * @param micrometerExpiryTimeInSeconds Expiry value for micrometer
     */
	public MicrometerRegistry(
			String totalLabel,
			Integer pct1,
			Integer pct2,
			Integer pct3,
			Integer logFrequency,
			Integer topErrors, 
			SampleLogger logger,
			Integer micrometerExpiryTimeInSeconds
			) {
		this.intervalRegistry = new SimpleMeterRegistry();
		this.summaryRegistry = new SimpleMeterRegistry();
		this.totalLabel = Util.makeMicrometerName(totalLabel);
		this.logFrequency = logFrequency;
		this.topErrors = topErrors;
		this.logger = logger;
		this.intervalRegistry.config().meterFilter(createMeterFilter(pct1, pct2, pct3, 60));
		LOG.info("Configuring summary registry with pct1:{}, pct2:{}, pct3:{}, expiry:{}", 
		        pct1, pct2, pct3, micrometerExpiryTimeInSeconds);
		this.summaryRegistry.config().meterFilter(createMeterFilter(pct1, pct2, pct3, micrometerExpiryTimeInSeconds));
	}
	
	private MeterFilter createMeterFilter(Integer pct1, Integer pct2, Integer pct3, Integer expiry) {
	    
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
							.percentilePrecision(2)
                            .minimumExpectedValue(1d)
							.maximumExpectedValue(360000d)
							.percentiles(
									(float)pct1/100.0,
									(float)pct2/100.0,
									(float)pct3/100.0
							)
							.percentilesHistogram(true)
							.expiry(Duration.ofSeconds(expiry))
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
		this.intervalRegistry.clear();
		this.intervalRegistry.close();
		this.summaryRegistry.clear();
		this.summaryRegistry.close();
	}
	
	/**
	 * Adds new sample record to registry
	 * 
	 * @param result Occurred sample result
	 */
	public synchronized void addResponse(ResponseResult result) {
		if(this.intervalRegistry.isClosed() || this.summaryRegistry.isClosed()) {
			return;
		}

		// Create micrometer tags to feed with data
		String threadTag = Util.makeMicrometerName(result.getThreadGroupLabel());
		String samplerTag = "spl_"+Util.makeMicrometerName(result.getSamplerLabel());
		List<String> micrometerTags = Arrays.asList(new String[]{samplerTag, this.totalLabel});
		
		for(String microMeterTag : micrometerTags) {
			// Save current period responses time
			this.intervalRegistry.summary("summary.response", "sample", microMeterTag).record(result.getResponseTime());
			this.summaryRegistry.summary("summary.response", "sample", microMeterTag).record(result.getResponseTime());

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
				this.intervalRegistry.counter("count.error", "sample", microMeterTag).increment();
				this.summaryRegistry.counter("count.error", "sample", microMeterTag, "type", result.getErrorCode()).increment();
			}
			// Accumulate responses time to calculate average responses time with a good precision without being heavy for the memory
			this.summaryRegistry.counter("accumulate.response", "sample", microMeterTag).increment(result.getResponseTime());
			
			// Increment tag count
			this.summaryRegistry.counter("count.total", "sample", microMeterTag).increment();
		}
	
		// Count thread number. The registries keep the max thread number values respectively for the period, and every periods.
		int threadGroupIncrement = result.getGroupThreads() - (int) this.intervalRegistry.counter("count.threads", "sample", threadTag).count();
		this.intervalRegistry.counter("count.threads", "sample", threadTag).increment(threadGroupIncrement < 0 ? 0 : threadGroupIncrement);
		int totalIncrement = result.getAllThreads() - (int) this.intervalRegistry.counter("count.threads", "sample", this.totalLabel).count();
		this.intervalRegistry.counter("count.threads", "sample", this.totalLabel).increment(totalIncrement < 0 ? 0 : totalIncrement);
		int samplerIncrement = result.getGroupThreads() - (int) this.intervalRegistry.counter("count.threads", "sample", samplerTag).count();
		this.intervalRegistry.counter("count.threads", "sample", samplerTag).increment(samplerIncrement < 0 ? 0 : samplerIncrement);
		
		int threadGroupIncrementTotal = result.getGroupThreads() - (int) this.summaryRegistry.counter("count.threads", "sample", threadTag).count();
		this.summaryRegistry.counter("count.threads", "sample", threadTag).increment(threadGroupIncrementTotal < 0 ? 0 : threadGroupIncrementTotal);
		int totalIncrementTotal = result.getAllThreads() - (int) this.summaryRegistry.counter("count.threads", "sample", this.totalLabel).count();
		this.summaryRegistry.counter("count.threads", "sample", this.totalLabel).increment(totalIncrementTotal < 0 ? 0 : totalIncrementTotal);
		int samplerIncrementTotal = result.getGroupThreads() - (int) this.summaryRegistry.counter("count.threads", "sample", samplerTag).count();
		this.summaryRegistry.counter("count.threads", "sample", samplerTag).increment(samplerIncrementTotal < 0 ? 0 : samplerIncrementTotal);
	}
	
	/**
	 * Creates new period log from currently registered records
	 * 
	 * @param name Thread group to log 
	 * @param timestamp Log timestamp
	 * @return New log with recorded period metrics
	 */
	public SampleLog makeLog(String name, Date timestamp) {
		DistributionSummary currentPeriodSummary = intervalRegistry.find("summary.response").tag("sample", name).summary();
		DistributionSummary everyPeriodsSummary = summaryRegistry.find("summary.response").tag("sample", name).summary();
		
		Double averageTotalResponseTime = summaryRegistry.counter("accumulate.response", "sample", name).count() /
				summaryRegistry.counter("count.total", "sample", name).count();
		double timeSinceFirstSampleCallInSeconds = (startAndEndDatesOfSamplers.get(name).getRight() - startAndEndDatesOfSamplers.get(name).getLeft()) / 1000d; 
		Double totalThroughput = this.summaryRegistry.counter("count.total","sample",name).count() / (timeSinceFirstSampleCallInSeconds);
		
		return currentPeriodSummary == null ? null : new SampleLog(
				Util.micrometerToOpenMetrics(name),
				timestamp,
				// Current period data
				(long) currentPeriodSummary.count(),
				(long) intervalRegistry.counter("count.error","sample",name).count(),
				currentPeriodSummary.takeSnapshot().percentileValues(),
				(long) currentPeriodSummary.totalAmount(),
				currentPeriodSummary.mean(),
				(long) currentPeriodSummary.max(),
				(double) currentPeriodSummary.count() / this.logFrequency,
				(long) intervalRegistry.counter("count.threads","sample",name).count(),
				// Every periods data
				(long) summaryRegistry.counter("count.total","sample",name).count(),
				(long) everyPeriodsSummary.max(),
				averageTotalResponseTime,
				(long) summaryRegistry.counter("count.error","sample",name).count(), // total error count
				collectTopErrors(name),
				totalThroughput,
				everyPeriodsSummary.takeSnapshot().percentileValues(), // total percentiles
				(long) summaryRegistry.counter("count.threads","sample",name).count()
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
		this.intervalRegistry.clear();
	}

	/**
	 * @return List of recorded thread group names + total
	 */
	public List<String> getSampleNames() {
		return this.intervalRegistry.find("summary.response").summaries()                  
				.stream().map(summary -> summary.getId().getTag("sample"))
				.collect(Collectors.toList());
	}
	
	// TODO: correct the algorithm because getTotalErrorsForType() returns null
	private List<Pair<String, Long>> collectTopErrors(String sampleName) {
		Collection<Counter> counters = summaryRegistry.find("count.error").tag("sample", sampleName).tagKeys("type").counters();
		// Retrieve all error types and their counts
		List<String> errorTypes = counters
			    								.stream()
											    .map(counter -> counter.getId().getTag("type"))
											    .collect(Collectors.toList());
		Map<String, Long> errorCounts = new HashMap<>();
		for (String errorType : errorTypes) {
		    long totalErrors = getTotalErrorsForType(errorType);
		    errorCounts.put(errorType, totalErrors);
		}
		
		List<Pair<String, Long>> countPerError = errorCounts.entrySet().stream()
											        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
											        .limit(this.topErrors)
											        .map(e -> Pair.of(e.getKey(), e.getValue()))
											        .collect(Collectors.toList());
		return countPerError;
	}
	
	private long getTotalErrorsForType(String errorType) {
	    return this.summaryRegistry.find("count.error").tag("type", errorType)
	    		   .counters()
				   .stream()
				   .mapToLong(counter -> (long) counter.count())
				   .sum();
	}

	
}
