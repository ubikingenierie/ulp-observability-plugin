package ubikloadpack.jmeter.ulp.observability.registry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

public class MicrometerRegistry {
		
	private MeterRegistry registry;
	private MeterRegistry extraReg;
	private final SampleLogger logger;
	private final Integer frequency;
	private final String totalLabel;
	private final AtomicInteger groupThreads;
	private final AtomicInteger allThreads;
	
	private static final Logger log = LoggerFactory.getLogger(MicrometerRegistry.class);
	
	
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
		this.extraReg = new SimpleMeterRegistry();
		this.registry.config().meterFilter(filter);
		this.frequency = frequency;
		this.groupThreads = new AtomicInteger(0);
		this.allThreads = new AtomicInteger(0);
		
	}
	
	public SampleLogger getLogger() {
		return this.logger;
	}
	
	public void close() {
		this.logger.clear();
		this.registry.clear();
		this.registry.close();
		this.extraReg.clear();
		this.extraReg.close();
	}

	public synchronized void addResponse(ResponseResult result) {
		if(this.registry.isClosed()) {
			return;
		}

		
		String sampleTag = Util.makeMicrometerName(result.getSampleLabel());
		
		this.registry.summary("summary.response", "sample", sampleTag).record(result.getResponseTime());
		this.registry.summary("summary.response", "sample", this.totalLabel).record(result.getResponseTime());
		
		this.extraReg.counter("count.total", "sample", sampleTag).increment();
		this.extraReg.counter("count.total", "sample", this.totalLabel).increment();
		
		this.groupThreads.set(result.getGroupThreads());
		this.allThreads.set(result.getAllThreads());
		
		if(result.hasError()) {
			this.registry.counter("count.error", "sample", sampleTag).increment();
			this.registry.counter("count.error", "sample", this.totalLabel).increment();
		}
		
	}
	
	
	public SampleLog getLog(String name, Date timestamp) {
		
		
		DistributionSummary summary = this.registry.find("summary.response").tag("sample", name).summary();
		
		return summary == null ? null : new SampleLog(
				Util.micrometerToOpenMetrics(name),
				timestamp,
				(long)this.extraReg.counter("count.total","sample",name).count(),
				(long) summary.count(),
				(long)this.registry.counter("count.error","sample",name).count(),
				summary.takeSnapshot().percentileValues(),
				(long) summary.totalAmount(),
				(long) summary.mean(),
				(long) summary.max(),
				(long) summary.count() / frequency,
				name == totalLabel ? allThreads.get() : groupThreads.get()
				);
	}

	
	public SampleLogger logAndReset() {
		return this.logAndReset(getSampleNames());
	}
	
	public SampleLogger log(List<String> names) {
		Date timestamp = new Date();
		names.forEach(name ->{
			this.logger.add(this.getLog(name, timestamp));
		});
		return this.logger;
	}
	
	public SampleLogger logAndReset(List<String> names) {
		this.log(names);
		this.registry.clear();
		return this.logger;
	}

	public List<String> getSampleNames() {
		ArrayList<String> names = new ArrayList<String>();
		this.registry.find("summary.response").summaries().forEach(summary ->{
			names.add(summary.getId().getTag("sample"));
		});
		return names;
	}
	
	
	

}
