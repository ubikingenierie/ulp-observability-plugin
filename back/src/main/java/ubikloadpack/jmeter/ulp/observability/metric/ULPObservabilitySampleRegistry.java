package ubikloadpack.jmeter.ulp.observability.metric;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jmeter.samplers.SampleResult;

import ubikloadpack.jmeter.ulp.observability.data.MetricsData;
import ubikloadpack.jmeter.ulp.observability.util.Util;

public class ULPObservabilitySampleRegistry {
	
	private Map<String, ULPObservabilitySample> ulpRegistry;
	private Map<String, MetricsData> ulpData;
	private ULPObservabilitySample total;
	
	private final String data_output;
	private final Integer pct1;
	private final Integer pct2;
	private final Integer pct3;
	private final Integer pct_precision;
	private final Boolean data_output_enabled;
	
	
	public ULPObservabilitySampleRegistry(Integer pct1, Integer pct2,
			Integer pct3, Integer pct_precision, String data_output, Boolean data_output_enabled) {
		this.ulpRegistry = new HashMap<>();
		this.ulpData = new HashMap<>();
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		this.pct_precision = pct_precision;
		this.data_output = data_output;
		this.total = new ULPObservabilitySample("_total", pct1, pct2, pct3, pct_precision);
		this.data_output_enabled = data_output_enabled;
	}


	public ULPObservabilitySample getSample(String sampleName) {
		if(!this.ulpRegistry.containsKey(sampleName)) {
			this.ulpRegistry.put(sampleName, 
					new ULPObservabilitySample(
							sampleName, 
							this.pct1, 
							this.pct2, 
							this.pct3, 
							this.pct_precision
							)
					);
			if(data_output_enabled) {
				this.ulpData.put(sampleName,
						new MetricsData(data_output+"\\"+sampleName)
						);
			}
			
		}
		return this.ulpRegistry.get(sampleName);
	}
	
	public Set<String> getSampleNames() {
		return this.ulpRegistry.keySet();
	}
	
	public synchronized boolean containsSample(String sampleName) {
		return this.ulpRegistry.containsKey(sampleName);
	}
	
	public Collection<ULPObservabilitySample> getSamples(List<String> filter){
		if(filter == null || filter.size() == 0) {
			return this.ulpRegistry.values();
		}
		
		return this.ulpRegistry.entrySet()
				.stream().filter(sample -> filter.contains(sample.getKey()))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}
	
	public Collection<ULPObservabilitySample> getSamples(){
		return this.ulpRegistry.values();
	}
	
	public ULPObservabilitySample getTotal() {
		return this.total;
	}
	
	public synchronized void clear() {
		this.ulpRegistry.clear();
		for(MetricsData metricsData : this.ulpData.values()) {
			metricsData.close();
		}
		this.ulpData.clear();
	}
	
	public synchronized ULPObservabilitySample processSample(SampleResult sampleResult) {
		ULPObservabilitySample sample = this.getSample(sampleResult.getSampleLabel());
		double current = Util.getResponseTime(sampleResult.getEndTime(),sampleResult.getStartTime());
		sample.addResponse(current, sampleResult.getErrorCount() < 1);
		this.total.addResponse(current, sampleResult.getErrorCount() < 1);
		
		if(data_output_enabled) {

			this.ulpData.get(sample.getSampleName()).write(
					sampleResult.getEndTime(), 
					current,
					sample.getTotalRequestCount(),
					sample.getErrorRequestCount(),
					sample.getMeanResponseTime(),
					sample.getMaxResponseTime(),
					sample.getPct1(),
					sample.getPct2(),
					sample.getPct3());
		}
		
		return sample;
	}

}
