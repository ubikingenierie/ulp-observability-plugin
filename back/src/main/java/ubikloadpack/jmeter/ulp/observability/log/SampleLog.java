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
	 * The total count of current period responses during the given period
	 */
	private final Long current;
	
	/**
	 * The total count of errors during the given period
	 */
	private final Long error;
	
	/**
	 * Response time percentiles for given period
	 */
	private final ValueAtPercentile[] pct;
	
	/**
	 * Response time sum for given period
	 */
	private final Long sum;
	
	/**
	 * Average response time for given period
	 */
	private final Long avg;
	
	/**
	 * Max response time for given period
	 */
	private final Long max;
	
	/**
	 * Response throughput per minute for given period
	 */
	private final Long throughput;
	
	/**
	 * Virtual users count
	 */
	private final Long threads;
	
	
	public SampleLog(
			String sampleName, 
			Date timeStamp, 
			Long total, 
			Long current, 
			Long error, 
			ValueAtPercentile[] pct,
			Long sum,
			Long avg, 
			Long max, 
			Long throughput,
			Long threads
			) {
		this.sampleName = sampleName;
		this.timeStamp = timeStamp;
		this.total = total;
		this.current = current;
		this.error = error;
		this.pct = pct;
		this.sum = sum;
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
	
	public Long getCurrent() {
		return current;
	}


	public Long getError() {
		return error;
	}

	public ValueAtPercentile[] getPct() {
		return this.pct;
	}
	
	public Long getSum() {
		return sum;
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
	
	public Long getThreads() {
		return threads;
	}
	
	/**
	 * Generate sample record metrics in OpenMetrics format
	 * 
	 * @return Sample record metrics in OpenMetrics format
	 */
	public String toOpenMetricsString() {
		
		StringBuilder str = new StringBuilder()
				.append("# TYPE "+this.sampleName+" summary\n")
				.append("# UNIT "+this.sampleName+" milliseconds\n")
				.append("# HELP "+this.sampleName+" Response percentiles\n");
		
		for(ValueAtPercentile pc : this.pct) {
			str.append(this.sampleName+"{quantile=\""+(long)(pc.percentile()*100)+"\"} "+ (long)pc.value() +"\n");
		}	
		
		str.append(this.sampleName+"_sum " + this.sum +"\n")
		.append(this.sampleName+"_created " + this.timeStamp.getTime() +"\n")
			
		.append("# TYPE "+this.sampleName+"_max gauge\n")
		.append("# UNIT "+this.sampleName+" milliseconds\n")
		.append("# HELP "+this.sampleName+"_max Max response\n")
		.append(this.sampleName+"_max "+ this.max + " " + this.timeStamp.getTime() +"\n")	
		
		.append("# TYPE "+this.sampleName+"_avg gauge\n")
		.append("# UNIT "+this.sampleName+" milliseconds\n")
		.append("# HELP "+this.sampleName+"_avg Average response\n")
		.append(this.sampleName+"_avg "+ this.avg + " " + this.timeStamp.getTime() +"\n")	

		.append("# TYPE "+this.sampleName+"_total gauge\n")
		.append("# HELP "+this.sampleName+"_total Response count\n")
		.append(this.sampleName+"_total{count=\"all\"} "+ this.total + " " + this.timeStamp.getTime() +"\n")	
		.append(this.sampleName+"_total{count=\"period\"} "+ this.current + " " + this.timeStamp.getTime() +"\n")	
		.append(this.sampleName+"_total{count=\"error\"} "+ this.error + " " + this.timeStamp.getTime() +"\n")	
		
		.append("# HELP "+this.sampleName+"_throughput Responses per second\n")
		.append("# TYPE "+this.sampleName+"_throughput gauge\n")
		.append(this.sampleName+"_throughput "+ this.throughput + " " + this.timeStamp.getTime() +"\n");
		
		str.append("# TYPE "+this.sampleName+"_threads counter\n")
		.append("# HELP "+this.sampleName+"_threads Virtual user count\n")
		.append(this.sampleName+"_threads "+ this.threads + " " + this.timeStamp.getTime() + "\n");	
		
		
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
								this.current,
								this.error, 
								(float) this.error / (float) (this.current) * 100.0,
								this.avg
								)
						);
		for(ValueAtPercentile pc : pct) {
			s.append(String.format("|%5d",(long)pc.value()));
		}
		
		return s.append(String.format("|%5d|%10d|%10d|%n",this.max,this.throughput,this.threads)).toString();
	}
	
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("SampleLog [sampleName=" + sampleName + ", timeStamp=" + timeStamp + ", total=" + total + ", current="
				+ current + ", error=" + error + ", pct={");
		for(ValueAtPercentile pc : pct) {
			s.append(pc.percentile()+"="+(long)pc.value()+",");
		}
		s.append("}, avg=" + avg + ", max=" + max + ", throughput=" + throughput + "]");
		return s.toString();
	}
	
	
}
