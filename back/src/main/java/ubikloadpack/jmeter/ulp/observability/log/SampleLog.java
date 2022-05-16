package ubikloadpack.jmeter.ulp.observability.log;

import java.util.Date;

import io.micrometer.core.instrument.distribution.ValueAtPercentile;


/**
 * Represents immutable sample period record
 * 
 * @author Valentin ZELIONII
 *
 */
public class SampleLog {

	/**
	 * Sampler name 
	 */
	private final String sampleName;
	
	/**
	 * Time stamp when record was created 
	 */
	private final Date timeStamp;
	
	/**
	 * The total count of responses starting from the beginning of test
	 */
	private final Long total;
	
	/**
	 * The total count of successful responses during the given period
	 */
	private final Long success;
	
	/**
	 * The total count of errors during the given period
	 */
	private final Long error;
	
	/**
	 * Response time percentiles for given period
	 */
	private final ValueAtPercentile[] pct;
	
	/**
	 * Average response time for given period
	 */
	private final Long avg;
	
	/**
	 * Max response time for given period
	 */
	private final Long max;
	
	/**
	 * Response throughput per second for given period
	 */
	private final Long throughput;
	
	/**
	 * Number of virtual users
	 */
	private final Integer threads;
	
	
	public SampleLog(
			String sampleName, 
			Date timeStamp, 
			Long total, 
			Long success, 
			Long error, 
			ValueAtPercentile[] pct,
			Long avg, 
			Long max, 
			Long throughput,
			Integer threads
			) {
		this.sampleName = sampleName;
		this.timeStamp = timeStamp;
		this.total = total;
		this.success = success;
		this.error = error;
		this.pct = pct;
		this.avg = avg;
		this.max = max;
		this.throughput = throughput;
		this.threads = threads;
	}
	
	
	public String getSampleName() {
		return sampleName;
	}


	public Date getTimeStamp() {
		return timeStamp;
	}


	public Long getTotal() {
		return total;
	}
	
	public Long getSuccess() {
		return success;
	}


	public Long getError() {
		return error;
	}

	public ValueAtPercentile[] getPct() {
		return this.pct;
	}

	public Long getAvg() {
		return avg;
	}


	public Long getMax() {
		return max;
	}
	
	public Long getThroughput() {
		return throughput;
	}
	
	public Integer getThreads() {
		return threads;
	}
	
	/**
	 * Generate sample record metrics in OpenMetrics format
	 * 
	 * @return Sample record metrics in OpenMetrics format
	 */
	public String toOpenMetricsString() {
		
		StringBuilder str = new StringBuilder()
				.append("# HELP "+this.sampleName+" Response metrics\n")
				.append("# TYPE "+this.sampleName+" summary\n");
		
		for(ValueAtPercentile pc : this.pct) {
			str.append(this.sampleName+"{quantile=\""+(long)(pc.percentile()*100)+"\"} "+ (long)pc.value() +"\n");
		}	
		
		str
			.append(this.sampleName+"_count " + this.total +"\n")
			.append(this.sampleName+"_created " + this.timeStamp.getTime() +"\n")
			
			.append("# HELP "+this.sampleName+"_success Success count\n")
			.append("# TYPE "+this.sampleName+"_success gauge\n")
			.append(this.sampleName+"_success "+ this.success +"\n")	
			
			.append("# HELP "+this.sampleName+"_error Error count\n")
			.append("# TYPE "+this.sampleName+"_error gauge\n")
			.append(this.sampleName+"_error "+ this.error +"\n")	
			
			.append("# HELP "+this.sampleName+"_avg Average response time\n")
			.append("# TYPE "+this.sampleName+"_avg gauge\n")
			.append(this.sampleName+"_avg "+ this.avg +"\n")	
			
			.append("# HELP "+this.sampleName+"_max Max response time\n")
			.append("# TYPE "+this.sampleName+"_max gauge\n")
			.append(this.sampleName+"_error "+ this.max +"\n")	
			
			.append("# HELP "+this.sampleName+"_throughput Response throughput\n")
			.append("# TYPE "+this.sampleName+"_throughput gauge\n")
			.append(this.sampleName+"_throughput "+ this.throughput +"\n")	
		
			.append("# HELP "+this.sampleName+"_threads Virtual user number\n")
			.append("# TYPE "+this.sampleName+"_threads gauge\n")
			.append(this.sampleName+"_threads "+ this.threads +"\n");	
		
		
		return str.toString();	
	}
	
	
	/**
	 * Create line for record debug log (see {@link ubikloadpack.jmeter.ulp.observability.log.SampleLogger})
	 * 
	 * @param namePadding Fixed padding space for sample name (see {@link ubikloadpack.jmeter.ulp.observability.log.SampleLogger#guiLog(Integer)})
	 * @param totalLabel The label value assigned to record for total samples
	 * @return Record in debug log format
	 */
	public String toLog(Integer namePadding, String totalLabel) {
		String sampleName = this.sampleName.equals(totalLabel) ? "TOTAL" : this.sampleName;
		StringBuilder s = 
				new StringBuilder().append(
						String.format("|%"+namePadding+"s|%10d|%10d|%10d|%7.3f%%|%5d", 
								sampleName, 
								this.total, 
								this.success,
								this.error, 
								(float) this.error / (float) (this.success + this.error) * 100.0,
								this.avg
								)
						);
		for(ValueAtPercentile pc : pct) {
			s.append(String.format("|%5d",(long)pc.value()));
		}
		
		return s.append(String.format("|%5d|%10d|%n",this.max,this.throughput)).toString();
	}
	
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("SampleLog [sampleName=" + sampleName + ", timeStamp=" + timeStamp + ", total=" + total + ", success="
				+ success + ", error=" + error + ", pct={");
		for(ValueAtPercentile pc : pct) {
			s.append(pc.percentile()+"="+(long)pc.value()+",");
		}
		s.append("}, avg=" + avg + ", max=" + max + ", throughput=" + throughput + "]");
		return s.toString();
	}
	
	
}
