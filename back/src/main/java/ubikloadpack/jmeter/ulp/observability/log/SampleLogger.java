package ubikloadpack.jmeter.ulp.observability.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;

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
	private final ConcurrentLinkedQueue<SampleLog> logger;
	
	/**
	 * Label used to indicate the total metrics of all samples
	 */
	private final String total_label;
	
	/**
	 * First percentile score
	 */
	private final Integer pct1;
	/**
	 * Second percentile score
	 */
	private final Integer pct2;
	/**
	 * third percentile score
	 */
	private final Integer pct3;
	
	private static final Logger log = LoggerFactory.getLogger(SampleLogger.class);

	public SampleLogger() {
		this(ULPObservabilityDefaultConfig.totalLabel(), ULPObservabilityDefaultConfig.pct1(), ULPObservabilityDefaultConfig.pct2(), ULPObservabilityDefaultConfig.pct3(), new ArrayList<>());
	}
	
	public SampleLogger(String total_label, Integer pct1, Integer pct2, Integer pct3) {
		this(total_label,pct1,pct2,pct3,new ArrayList<>());
	}
	
	public SampleLogger(String total_label, Integer pct1, Integer pct2, Integer pct3, Collection<SampleLog> logger) {
		this.logger = new ConcurrentLinkedQueue<>(logger);
		this.total_label = total_label;
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
	}
	
	
	/**
	 * Add new sample record to log storage
	 * 
	 * @param record Sample record to add
	 */
	public void add(SampleLog record) {
		this.logger.add(record);
	}
	
	/**
	 * Add list of sample records to log storage
	 * 
	 * @param recordList List of sample records to add
	 */
	public void add(Collection<SampleLog> recordList) {
		this.logger.addAll(recordList);
	}
	
	/**
	 * Get last period sample records list filtered by names
	 * 
	 * @param filter Sample name filter
	 * @return List of last period sample records
	 */
	public Collection<SampleLog> getLast(List<String> filter){
		Map<String, SampleLog> lastLog = new HashMap<>();
		for(SampleLog sampleLog : filter == null || filter.size() == 0 ? this.logger : getAll(filter)) {
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
		Set<String> sampleNames = new HashSet<>();
		this.logger.forEach(sampleLog -> {
			sampleNames.add(sampleLog.getSampleName());
		});
		return sampleNames;
	}
	
	/**
	 * Get last period sample records list
	 * 
	 * @return List of last period sample records
	 */
	public Collection<SampleLog> getLast(){
		return this.getLast(null);
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
		StringBuilder s = new StringBuilder();
		
		Collection<SampleLog> logger = all ? this.getAll(filter) : getLast(filter);
		logger.forEach(sampleLog -> {
			s.append(sampleLog.toOpenMetricsString());
			s.append("\n");
		});
		
		return s.toString();
	}
	
	
	/**
	 * Generate log summary for debug/non-gui mode
	 * 
	 * @return Record logs in form of summary table
	 */
	public String guiLog() {
		StringBuilder s = new StringBuilder();
		SampleLog total = null;
		
		Integer namePadding = 17;
		Set<String> names = this.getSampleNames();
		for(String name: names) {
			if(name.length() > namePadding) {
				namePadding = name.length();
			}
		}
		
		final String divider = "+"+"-".repeat(namePadding)
				+ "+----------"
				+ "+----------"
				+ "+----------"
				+ "+--------"
				+ "+-----"
				+ "+-------"
				+ "+-------"
				+ "+-------"
				+ "+-----"
				+ "+----------"
				+ "+-------"
				+ "+\n";
		
		s.append("\n"+ new Date().toString() + "\n" +divider);
		
		s.append(
				String.format("|%"+namePadding+"s|%10s|%10s|%10s|%7s%%|%5s|%7s|%7s|%7s|%5s|%10s|%7s|%n",
						"Thread Group Name",
						"Total",
						"Count",
						"Error",
						"Err ",
						"Avg",
						"Pc "+pct1+"th",
						"Pc "+pct2+"th",
						"Pc "+pct3+"th",
						"Max",
						"Throughput",
						"Threads"
						)
				);
		
		
		s.append(divider);
		for(SampleLog sampleLog : this.getLast()){
			if(!sampleLog.getSampleName().equals(this.total_label)) {
				s.append(sampleLog.toLog(namePadding,total_label));
			}
			else {
				total = sampleLog;
			}
		};
		s.append(divider);
		s.append(total.toLog(namePadding,total_label));
		s.append(divider);
		return s.toString();
	}

	
	/**
	 * Clear log storage
	 */
	public void clear() {
		this.logger.clear();
	}

}
