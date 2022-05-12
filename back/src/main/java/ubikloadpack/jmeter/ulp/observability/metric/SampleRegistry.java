package ubikloadpack.jmeter.ulp.observability.metric;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ubikloadpack.jmeter.ulp.observability.data.MetricsData;

public class SampleRegistry {
	
	private Map<String, Sample> ulpRegistry;
	private Map<String, MetricsData> ulpData;
	private SampleLogger logger;
	
	private final String data_output;
	private final Integer pct1;
	private final Integer pct2;
	private final Integer pct3;
	private final Integer pct_precision;
	private final Boolean data_output_enabled;
	private final String totalLabel;
	
	
	public SampleRegistry(
			SampleLogger logger, 
			String totalLabel, 
			Integer pct1, 
			Integer pct2,
			Integer pct3, 
			Integer pct_precision, 
			String data_output, 
			Boolean data_output_enabled
			) {
		
		this.logger = logger;
		this.totalLabel = totalLabel;
		this.ulpRegistry = new ConcurrentHashMap<>();
		this.ulpRegistry.put(totalLabel, new Sample(totalLabel, pct1, pct2, pct3, pct_precision));
		this.ulpData = new ConcurrentHashMap<>();
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		this.pct_precision = pct_precision;
		this.data_output = data_output;
		this.data_output_enabled = data_output_enabled;
	}
	
	public SampleRegistry(
			SampleLogger logger, 
			Integer pct1, 
			Integer pct2,
			Integer pct3, 
			Integer pct_precision, 
			String data_output, 
			Boolean data_output_enabled
			) {
		
		this(
				logger,
				"_total",
				pct1,
				pct2,
				pct3,
				pct_precision,
				data_output,
				data_output_enabled
				);
	}

	public Sample getSample(String sampleName) {
		
		this.ulpRegistry.putIfAbsent(sampleName, 
				new Sample(
						sampleName, 
						this.pct1, 
						this.pct2, 
						this.pct3, 
						this.pct_precision
						)
				);
		if(data_output_enabled) {
			this.ulpData.putIfAbsent(sampleName,
					new MetricsData(data_output+"\\"+sampleName)
					);
		}
			
		return this.ulpRegistry.get(sampleName);
	}
	
	public Set<String> getSampleNames() {
		return this.ulpRegistry.keySet();
	}
	
	public boolean containsSample(String sampleName) {
		return this.ulpRegistry.containsKey(sampleName);
	}
	
	public Collection<Sample> getSamples(){
		return this.ulpRegistry.values();
	}

	public void clear() {
		this.ulpRegistry.clear();
		for(MetricsData metricsData : this.ulpData.values()) {
			metricsData.close();
		}
		this.ulpData.clear();
	}
	
	public void processSample(ResponseResult responseResult) {
		Sample sample = this.getSample(responseResult.getSampleLabel());
		sample.addResponse(responseResult.getResponseTime(), responseResult.hasError());
		this.getSample(totalLabel).addResponse(responseResult.getResponseTime(),responseResult.hasError());
		
		if(data_output_enabled) {

			this.ulpData.get(sample.getSampleName()).write(
					responseResult.getEndTime(), 
					responseResult.getResponseTime(),
					sample.getTotalRequestCount(),
					sample.getErrorRequestCount(),
					sample.getMeanResponseTime(),
					sample.getMaxResponseTime(),
					sample.getPct1(),
					sample.getPct2(),
					sample.getPct3());
		}
	}
	
	public SampleLogger getLogger() {
		return this.logger;
	}
	
	public SampleLogger logAndClear() {
		this.ulpRegistry.values().forEach(sample -> {
			this.logger.add(sample.logAndClear());
		});
		return this.logger;
	}
	
	public SampleLogger currentLog() {
		return new SampleLogger(
				this.totalLabel, 
				this.getSamples().stream()
					.map(sample -> {
						return sample.log();
						})
					.collect(Collectors.toList()));
	}

}
