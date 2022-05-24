package ubikloadpack.jmeter.ulp.observability.registry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Registry class for managing sample records and metrics calculations
 * 
 * @author Valentin ZELIONII
 *
 */
public class MicrometerRegistry {
		
	/**
	 * Main sample registry containing sample records of each thread group + total
	 */
	private MeterRegistry registry;
	
	/**
	 * Record count registry for each thread group + total
	 */
	private MeterRegistry totalReg;
	/**
	 * Sample logger
	 */
	private final SampleLogger logger;
	/**
	 * Log frequency (period length) 
	 */
	private final Integer frequency;
	/**
	 * Label used to denote total metrics
	 */
	private final String totalLabel;
	
	private static final Logger log = LoggerFactory.getLogger(MicrometerRegistry.class);
	
	
	/**
	 * New sample registry with configured metrics summary
	 * 
	 * @param totalLabel Label to denote total metrics
	 * @param pct1 First percentile
	 * @param pct2 Second percentile
	 * @param pct3 Third percentile
	 * @param pctPrecision Percentile precision
	 * @param frequency Period frequency
	 */
	public MicrometerRegistry(
			String totalLabel,
			Integer pct1, 
			Integer pct2, 
			Integer pct3, 
			Integer pctPrecision, 
			Integer frequency) {
		
		MeterFilter filter = new MeterFilter() {
			
			@Override
			public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
				if(id.getType().equals(Type.DISTRIBUTION_SUMMARY)) {
					return DistributionStatisticConfig.builder()
							.percentilePrecision(pctPrecision)
							.percentiles(pct1/100.0, pct2/100.0, pct3/100.0)
							.percentilesHistogram(true)
							.build()
							.merge(config);
				}
				return config;
			}
		};
		
		this.totalLabel = Util.makeMicrometerName(totalLabel);
		
		this.logger = new SampleLogger(Util.makeOpenMetricsName(totalLabel));
		this.registry = new SimpleMeterRegistry();
		this.totalReg = new SimpleMeterRegistry();
		this.registry.config().meterFilter(filter);
		this.frequency = frequency;

		
	}
	
	/**
	 * @return Logger with recorded period metrics
	 */
	public SampleLogger getLogger() {
		return this.logger;
	}
	
	/**
	 * Clears and closes registry
	 */
	public void close() {
		this.logger.clear();
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
		if(this.registry.isClosed()) {
			return;
		}

		String sampleTag = Util.makeMicrometerName(result.getSampleLabel());
		
		this.registry.summary("summary.response", "sample", sampleTag).record(result.getResponseTime());
		this.registry.summary("summary.response", "sample", this.totalLabel).record(result.getResponseTime());
		
	
		this.registry.counter("count.threads", "sample", sampleTag).increment(
				result.getGroupThreads() - (int) this.registry.counter("count.threads", "sample", sampleTag).count());
		this.registry.counter("count.threads", "sample", this.totalLabel).increment(
				result.getAllThreads() - (int) this.registry.counter("count.threads", "sample", this.totalLabel).count());
		
		
		this.totalReg.counter("count.total", "sample", sampleTag).increment();
		this.totalReg.counter("count.total", "sample", this.totalLabel).increment();
		
		
		if(result.hasError()) {
			this.registry.counter("count.error", "sample", sampleTag).increment();
			this.registry.counter("count.error", "sample", this.totalLabel).increment();
		}
		
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
				(long)this.totalReg.counter("count.total","sample",name).count(),
				(long) summary.count(),
				(long)this.registry.counter("count.error","sample",name).count(),
				summary.takeSnapshot().percentileValues(),
				(long) summary.totalAmount(),
				(long) summary.mean(),
				(long) summary.max(),
				(long) summary.count() / frequency,
				(long)this.registry.counter("count.threads","sample",name).count()
				);
	}

	
	/**
	 * Logs entire registry and then resets it for next period
	 * 
	 * @return Updated logger
	 */
	public SampleLogger logAndReset() {
		return this.logAndReset(getSampleNames());
	}
	
	
	/**
	 * Logs selected thread groups without resetting
	 * 
	 * @param names List of thread groups to log
	 * @return Updated logger
	 */
	public SampleLogger log(List<String> names) {
		Date timestamp = new Date();
		names.forEach(name ->{
			this.logger.add(this.makeLog(name, timestamp));
		});
		return this.logger;
	}
	
	/**
	 * Logs selected thread groups and then resets them for next period
	 * 
	 * @return Updated logger
	 */
	public SampleLogger logAndReset(List<String> names) {
		this.log(names);
		this.registry.clear();
		return this.logger;
	}

	/**
	 * @return List of recorded thread group names + total
	 */
	public List<String> getSampleNames() {
		ArrayList<String> names = new ArrayList<String>();
		this.registry.find("summary.response").summaries().forEach(summary ->{
			names.add(summary.getId().getTag("sample"));
		});
		return names;
	}
	
	
	

}
