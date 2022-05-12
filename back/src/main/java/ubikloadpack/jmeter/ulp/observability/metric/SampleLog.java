package ubikloadpack.jmeter.ulp.observability.metric;

import java.util.Date;


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
	 * The total count of responses during the given period
	 */
	private final Long current;
	
	/**
	 * The total count of errors during the given period
	 */
	private final Long error;
	
	/**
	 * The score of 1st percentile
	 */
	private final Integer pct1;
	
	/**
	 * The score of 2nd percentile
	 */
	private final Integer pct2;
	
	/**
	 * The score of 3rd percentile
	 */
	private final Integer pct3;
	
	/**
	 * 1st percentile value
	 */
	private final Long pct1_val;
	
	/**
	 * 2nd percentile value
	 */
	private final Long pct2_val;
	
	/**
	 * 3rd percentile value
	 */
	private final Long pct3_val;
	
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
	
	
	public SampleLog(
			String sampleName, 
			Date timeStamp, 
			Long total, 
			Long current, 
			Long error, 
			Integer pct1, 
			Integer pct2,
			Integer pct3, 
			Long pct1_val, 
			Long pct2_val, 
			Long pct3_val, 
			Long avg, 
			Long max, 
			Long throughput
			) {
		this.sampleName = sampleName;
		this.timeStamp = timeStamp;
		this.total = total;
		this.current = current;
		this.error = error;

		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		this.pct1_val = pct1_val;
		this.pct2_val = pct2_val;
		this.pct3_val = pct3_val;
		this.avg = avg;
		this.max = max;
		this.throughput = throughput;
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

	public Integer getPct1() {
		return pct1;
	}


	public Integer getPct2() {
		return pct2;
	}


	public Integer getPct3() {
		return pct3;
	}


	public Long getPct1_val() {
		return pct1_val;
	}


	public Long getPct2_val() {
		return pct2_val;
	}


	public Long getPct3_val() {
		return pct3_val;
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
	
	
	/**
	 * Generate sample record metrics in OpenMetrics format
	 * 
	 * @return Sample record metrics in OpenMetrics format
	 */
	public String toOpenMetricsString() {
		StringBuilder str = new StringBuilder()
				.append("# HELP "+this.sampleName+"_request Request metrics\n")
				.append("# TYPE "+this.sampleName+"_request summary\n")
				.append(this.sampleName+"_request{metrics=\"count\",status=\"total\"} "+ this.total +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_request{metrics=\"count\",status=\"current\"} "+ this.current +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_request{metrics=\"count\",status=\"error\"} "+ this.error +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_request{metrics=\"throughput\"} "+ this.throughput +" "+this.timeStamp.getTime()+"\n")
				.append("# HELP "+this.sampleName+"_response Response metrics\n")
				.append("# TYPE "+this.sampleName+"_response summary\n")
				.append(this.sampleName+"_response{metrics=\"avg\"} "+this.avg +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_response{metrics=\"max\"} "+this.max +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_response{metrics=\"pc"+this.pct1+"\"} "+this.pct1_val +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_response{metrics=\"pc"+this.pct2+"\"} "+this.pct2_val +" "+this.timeStamp.getTime()+"\n")
				.append(this.sampleName+"_response{metrics=\"pc"+this.pct3+"\"} "+this.pct3_val +" "+this.timeStamp.getTime()+"\n");
				
		
		return str.toString();	
	}
	
	
	/**
	 * Create line for record debug log (see {@link ubikloadpack.jmeter.ulp.observability.metric.SampleLogger})
	 * 
	 * @param namePadding Fixed padding space for sample name (see {@link ubikloadpack.jmeter.ulp.observability.metric.SampleLogger#guiLog(Integer)})
	 * @param totalLabel The label value assigned to record for total samples
	 * @return Record in debug log format
	 */
	public String toLog(Integer namePadding, String totalLabel) {
		String sampleName = this.sampleName.equals(totalLabel) ? "TOTAL" : this.sampleName;
		return String.format("|%"+namePadding+"s|%10d|%10d|%10d|%7.3f%%|%5d|%5d|%5d|%5d|%5d|%10d|%n", 
				sampleName, 
				this.total, 
				this.current,
				this.error, 
				(float) this.error / (float) this.current * 100,
				this.avg,
				this.pct1_val,
				this.pct2_val,
				this.pct3_val,
				this.max,
				this.throughput
				);
	}
	
	
	@Override
	public String toString() {
		return "SampleLog [sampleName=" + sampleName + ", timeStamp=" + timeStamp + ", total=" + total + ", current="
				+ current + ", error=" + error + ", pct1=" + pct1 + ", pct2=" + pct2 + ", pct3=" + pct3 + ", pct1_val="
				+ pct1_val + ", pct2_val=" + pct2_val + ", pct3_val=" + pct3_val + ", avg=" + avg + ", max=" + max
				+ ", throughput=" + throughput + "]";
	}
	
	
}
