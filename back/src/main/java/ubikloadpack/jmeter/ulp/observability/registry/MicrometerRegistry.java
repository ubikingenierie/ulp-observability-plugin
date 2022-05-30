package ubikloadpack.jmeter.ulp.observability.registry;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import ubikloadpack.jmeter.ulp.observability.log.SampleLog;
import ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.util.Util;

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
		this.registry.config().meterFilter(
			new MeterFilter() {
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
			}
				);
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

		String sampleTag = Util.makeMicrometerName(result.getSampleLabel());
		
		this.registry.summary("summary.response", "sample", sampleTag).record(result.getResponseTime());
		this.registry.summary("summary.response", "sample", this.totalLabel).record(result.getResponseTime());
		
	
		this.registry.counter("count.threads", "sample", sampleTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", sampleTag).count());
		this.registry.counter("count.threads", "sample", this.totalLabel).increment(
				result.getAllThreads() - (int) this.registry.counter("count.threads", "sample", this.totalLabel).count());
		
		if(result.hasError()) {
			this.registry.counter("count.error", "sample", sampleTag).increment();
			this.registry.counter("count.error", "sample", this.totalLabel).increment();
		}
		
		this.totalReg.counter("count.total", "sample", sampleTag).increment();
		this.totalReg.counter("count.total", "sample", this.totalLabel).increment();
		
	}
	
	
	/**
	 * Creates new period log from currently registered records
	 * 
	 * @param name Thread group to log 
	 * @param timestamp Log timestamp
	 * @return New log with recorded period metrics
	 */
	public SampleLog makeLog(String name, Date timestamp) {
		
		
		DistributionSummary summary = this.registry.find("summary.response").tag("sample", name).summary();
		
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
