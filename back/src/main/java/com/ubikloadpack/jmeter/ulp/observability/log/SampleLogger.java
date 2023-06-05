package com.ubikloadpack.jmeter.ulp.observability.log;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Represents the storage of periodic sample logs (see {@link ubikloadpack.jmeter.ulp.observability.log.SampleLog} ) 
 *
 * @author Valentin ZELIONII 
 *
 */
public class SampleLogger {
	
	/**
	 * Concurrent list of sample logs
	 */
	private Collection<SampleLog> logger;
	
	/**
	 * Concurrent list of sample names
	 */
	private Set<String> sampleNames;
	
	/**
	 * Total metrics label.
	 */
	private String totalLabel;
	
	
	
	/**
	 * Creates new sample logger.
	 * @param totalLabel Total metrics label in use.
	 */
	public SampleLogger(String totalLabel) {
		this.logger =
				new ConcurrentLinkedQueue<>();
		this.sampleNames =
				ConcurrentHashMap.newKeySet();
		this.totalLabel = totalLabel;
	}
	
	/**
	 * Add new sample record to log storage
	 * 
	 * @param record Sample record to add
	 */
	public void add(SampleLog record) {
		this.logger.add(record);
		this.sampleNames.add(record.getSampleName());
	}
	
	/**
	 * Add list of sample records to log storage
	 * 
	 * @param recordList List of sample records to add
	 */
	public void add(Collection<SampleLog> recordList) {
		recordList.forEach(record -> add(record));
	}
	
	/**
	 * Get last period sample records list filtered by names
	 * 
	 * @param filter Sample name filter
	 * @return List of last period sample records
	 */
	public Collection<SampleLog> getLast(List<String> filter){
		Map<String, SampleLog> lastLog = new HashMap<>();
		for(SampleLog sampleLog : getAll(filter)) {
			if(!lastLog.containsKey(sampleLog.getSampleName())
					|| sampleLog.getTimeStamp().after(lastLog.get(sampleLog.getSampleName()).getTimeStamp())) {
				
				lastLog.put(sampleLog.getSampleName(), sampleLog);
			}
		}
		return lastLog.values();
		
	}
	
	/**
	 * Get recorded sample names list
	 * 
	 * @return List of recorded sample names
	 */
	public Set<String> getSampleNames(){
		return this.sampleNames;
	}
	
	/**
	 * Get last period sample records list
	 * 
	 * @return List of last period sample records
	 */
	public Collection<SampleLog> getLast(){
		return getLast(null);
	}
	
	/**
	 * Get list of all sample records filtered by names
	 * 
	 * @param filter Sample name filter
	 * @return List of all sample records
	 */
	public Collection<SampleLog> getAll(List<String> filter){
		if(filter == null || filter.size() == 0) {
			return this.logger;
		}
		return this.logger.stream()
				.filter(sample -> filter.contains(sample.getSampleName()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Get list of all sample records
	 * 
	 * @return List of all sample records
	 */
	public Collection<SampleLog> getAll(){
		return this.logger;
	}
	
	
	/**
	 * Generate sample metrics summary in OpenMetrics format
	 * 
	 * @param filter Sample name filter
	 * @param all Retrieve sample records of all periods
	 * @return List of sample records in OpenMetrics format
	 */
	public String openMetrics(List<String> filter, Boolean all) {
		Collection<SampleLog> logger = all ? getAll(filter) : getLast(filter);
		return logger.stream()
				.map(sampleLog -> sampleLog.toOpenMetricsString())
				.collect(Collectors.joining("\n"));

	}
	

	
	/**
	 * Generate log summary for debug/non-gui mode
	 * 
	 * @return Record logs in form of summary table
	 */
	public String guiLog() {
		StringBuilder str = new StringBuilder().append(new Date().toString());
		SampleLog total = null;
		
		for(SampleLog sampleLog : getLast()){
			if(!sampleLog.getSampleName().equals(totalLabel)) {
				str.append(sampleLog.toLog());
			}
			else {
				total = sampleLog;
			}
		};
		if (total != null) {
			str.append(total.toLog());
		}
		
		return str.toString();
	}

	
	/**
	 * Clear log storage
	 */
	public void clear() {
		logger.clear();
	}

}
