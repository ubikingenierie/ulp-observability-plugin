package com.ubikloadpack.jmeter.ulp.observability.log;

import java.text.DecimalFormat;
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
	 * Percentage value log format.
	 */
	private final static DecimalFormat PCT_FORMAT = new DecimalFormat("###.##");

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
		return this.sampleName;
	}


	public Date getTimeStamp() {
		return this.timeStamp;
	}


	public Long getTotal() {
		return this.total;
	}
	
	public Long getCurrent() {
		return this.current;
	}


	public Long getError() {
		return this.error;
	}

	public ValueAtPercentile[] getPct() {
		return this.pct;
	}
	
	public Long getSum() {
		return this.sum;
	}

	public Long getAvg() {
		return this.avg;
	}


	public Long getMax() {
		return this.max;
	}
	
	public Long getThroughput() {
		return this.throughput;
	}
	
	public Long getThreads() {
		return this.threads;
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
	 * @param totalLabel Total metrics label
	 * @return Record in debug log format
	 */
	public String toLog() {
		StringBuilder str = new StringBuilder();
		
		str.append("\n")
		.append(this.sampleName)
		.append(":\n     Total: ")
		.append(this.total)
		.append("\n     Error: ")
		.append(PCT_FORMAT.format((float) this.error / (float) (this.current) * 100.0))
		.append("% (")
		.append(this.error)
		.append("/")
		.append(this.current)
		.append(")")
		.append("\n     Avearage: ")
		.append(this.avg)
		.append("ms\n     Percentiles:");
		
		for(ValueAtPercentile pc : this.pct) {
			str.append("\n        Percentile ")
			.append((long)(pc.percentile()*100.))
			.append("th: ")
			.append((long)pc.value())
			.append("ms");
		}

		str.append("\n     Max: ")
		.append(this.max)
		.append("ms\n     Throughput: ")
		.append(this.throughput)
		.append("req/s\n     Threads: ")
		.append(this.threads)
		.append("\n");
		
		return str.toString();
	}
	
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("SampleLog [sampleName=" + this.sampleName 
				+ ", timeStamp=" + this.timeStamp + 
				", total=" + this.total + 
				", current="+ this.current + 
				", error=" + this.error + 
				", pct={");
		for(ValueAtPercentile pc : this.pct) {
			s.append(pc.percentile()+"="+(long)pc.value()+",");
		}
		s.append("}, avg=" + this.avg + 
				", max=" + this.max + 
				", throughput=" + this.throughput + "]");
		return s.toString();
	}
	
	
}
