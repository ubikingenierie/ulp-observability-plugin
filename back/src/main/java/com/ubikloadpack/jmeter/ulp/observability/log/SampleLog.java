package com.ubikloadpack.jmeter.ulp.observability.log;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	private final Double avg;
	
	/**
	 * Max response time for given period
	 */
	private final Long max;

	/**
	 * Response throughput per minute for given period
	 */
	private final Double throughput;
	
	/**
	 * Virtual users count
	 */
	private final Long threads;
	
	/**
	 * Max response time for every period
	 */
	private final Long maxTotal;
	
	/**
	 * Average response time for every period
	 */
	private final Double avgTotal;
	
	/**
	 * The total count of errors during every periods
	 */
	private final Long errorTotal;
	
	/**
	 * Response throughput per seconds for every periods
	 */
	private final Double throughputTotal;
	
	/**
	 * Response time percentiles for every periods
	 */
	private final ValueAtPercentile[] pctTotal;
	
	/**
     * Creates new Sample log
     * 
     * @param sampleName Sampler name
     * @param timeStamp Time stamp when record was created
     * @param current The total count of current period responses during the given period
     * @param error The total count of errors during the given period
     * @param pct Response time percentiles for given period
     * @param sum Response time sum for given period
     * @param avg Average response time for given period
     * @param max Max response time for given period
     * @param throughput Response throughput per seconds for given period
     * @param threads Virtual users count
     * @param total The total count of responses starting from the beginning of test
	 * @param maxTotal Max response time for every periods 
	 * @param avgTotal Average response time for every periods 
	 * @param errorTotal The total count of errors during every periods 
	 * @param throughputTotal Response throughput per seconds for every periods 
	 * @param pctTotal Response time percentiles for every periods
	 */
	public SampleLog(
		String sampleName, 
		Date timeStamp, 
		Long current, 
		Long error, 
		ValueAtPercentile[] pct,
		Long sum,
		Double avg, 
		Long max, 
		Double throughput,
		Long threads,
		Long total, 
		Long maxTotal,
		Double avgTotal,
		Long errorTotal,
		Double throughputTotal,
		ValueAtPercentile[] pctTotal
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
		this.maxTotal = maxTotal;
		this.avgTotal = avgTotal;
		this.errorTotal = errorTotal;
		this.throughputTotal = throughputTotal;
		this.pctTotal = pctTotal;
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

	public Double getAvg() {
		return this.avg;
	}


	public Long getMax() {
		return this.max;
	}
	
	public Double getThroughput() {
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
		// Percentiles + response time sum
		StringBuilder str = new StringBuilder();
		addOpenMetricTypeUnitHelpToStr(str, this.sampleName + "_pct" , "summary", "milliseconds", "Response percentiles");
		for(ValueAtPercentile pc : this.pct) {
			str.append(this.sampleName + "_pct" + "{quantile=\""+(long)(pc.percentile()*100)+"\"} "+ (long)pc.value() +"\n");
		}
		for(ValueAtPercentile pc : this.pctTotal) {
			str.append(this.sampleName + "_pct" + "{quantile_every_periods=\""+(long)(pc.percentile()*100)+"\"} "+ (long)pc.value() +"\n");
		}
		str.append(this.sampleName+"_pct_sum " + this.sum +"\n")
		.append(this.sampleName+"_pct_created " + this.timeStamp.getTime() +"\n");

		// Max
		addOpenMetricTypeUnitHelpToStr(str, this.sampleName + "_max", "gauge", "milliseconds", "Current period max response");
		str.append(this.sampleName+"_max "+ this.max + " " + this.timeStamp.getTime() +"\n");
		addOpenMetricTypeUnitHelpToStr(str, this.sampleName + "_max_every_periods", "gauge", "milliseconds", "Total max response");
		str.append(this.sampleName+"_max_every_periods "+ this.maxTotal + " " + this.timeStamp.getTime() +"\n");
		
		// Averages
		addOpenMetricTypeUnitHelpToStr(str, this.sampleName + "_avg", "gauge", "milliseconds", "Current period average response");
		str.append(this.sampleName+"_avg "+ roundDoubleValueTo2DigitsAfterDecimalPoint(this.avg)
			+ " " + this.timeStamp.getTime() +"\n");
		addOpenMetricTypeUnitHelpToStr(str, this.sampleName + "_avg_every_periods", "gauge", "milliseconds", "Total average response");
		str.append(this.sampleName+"_avg_every_periods "+ roundDoubleValueTo2DigitsAfterDecimalPoint(this.avgTotal)
			+ " " + this.timeStamp.getTime() +"\n");
		
		// Sampler calls count + errors count
		addOpenMetricTypeHelpToStr(str, this.sampleName + "_total", "gauge", "Response count");
		str.append(this.sampleName+"_total{count=\"all\"} "+ this.total + " " + this.timeStamp.getTime() +"\n")	
		.append(this.sampleName+"_total{count=\"period\"} "+ this.current + " " + this.timeStamp.getTime() +"\n")	
		.append(this.sampleName+"_total{count=\"error\"} "+ this.error + " " + this.timeStamp.getTime() +"\n")
		.append(this.sampleName+"_total{count=\"error_every_periods\"} "+ this.errorTotal + " " + this.timeStamp.getTime() +"\n");
		
		// Throughput
		addOpenMetricTypeHelpToStr(str, this.sampleName + "_throughput", "gauge", "Current period responses per second");
		str.append(this.sampleName+"_throughput "+ roundDoubleValueTo2DigitsAfterDecimalPoint(this.throughput)
			+ " " + this.timeStamp.getTime() +"\n");
		addOpenMetricTypeHelpToStr(str, this.sampleName + "_throughput_every_periods", "gauge", "Total responses per second");
		str.append(this.sampleName+"_throughput_every_periods "+ roundDoubleValueTo2DigitsAfterDecimalPoint(this.throughputTotal)
			+ " " + this.timeStamp.getTime() +"\n");
		
		// Threads number
		addOpenMetricTypeHelpToStr(str, this.sampleName + "_threads", "counter", "Virtual user count");
		str.append(this.sampleName+"_threads "+ this.threads + " " + this.timeStamp.getTime() + "\n");	
		
		return str.toString();
	}
	
	private void addOpenMetricTypeUnitHelpToStr(StringBuilder str, String sampleName, String type, String unit, String help) {
		str.append("# TYPE " + sampleName + " " + type + "\n")
		.append("# UNIT " + sampleName + " " + unit + "\n")
		.append("# HELP " + sampleName + " " + help + "\n");
	}
	
	private void addOpenMetricTypeHelpToStr(StringBuilder str, String sampleName, String type, String help) {
		str.append("# TYPE " + sampleName + " " + type + "\n")
		.append("# HELP " + sampleName + " " + help + "\n");
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
		
		// Current period
		.append(":\n     Current period: ")
		.append("\n         Error: ")
		.append(PCT_FORMAT.format((float) this.error / (float) (this.current) * 100.0))
		.append("% (")
		.append(this.error)
		.append("/")
		.append(this.current)
		.append(")")
		.append("\n         Average: ")
		.append(this.avg)
		.append("ms\n         Percentiles:");
		for(ValueAtPercentile pc : this.pct) {
			str.append("\n            Percentile ")
			.append((long)(pc.percentile()*100.))
			.append("th: ")
			.append((long)pc.value())
			.append("ms");
		}
		str.append("\n         Max: ")
		.append(this.max)
		.append("ms\n         Throughput: ")
		.append(this.throughput)
		.append(" req/s\n         Threads: ")
		.append(this.threads)
		
		// Every periods
		.append("\n     Every periods: ")
		.append("\n         Total: ")
		.append(this.total)
		.append("\n         Error: ")
		.append(PCT_FORMAT.format((float) this.errorTotal / (float) (this.total) * 100.0))
		.append("% (")
		.append(this.errorTotal)
		.append("/")
		.append(this.total)
		.append(")")
		.append("\n         Average: ")
		.append(this.avgTotal)
		.append("ms\n         Percentiles:");
		for(ValueAtPercentile pc : this.pctTotal) {
			str.append("\n            Percentile ")
			.append((long)(pc.percentile()*100.))
			.append("th: ")
			.append((long)pc.value())
			.append("ms");
		}
		str.append("\n         Max: ")
		.append(this.maxTotal)
		.append("ms\n         Throughput: ")
		.append(this.throughputTotal)
		.append(" req/s\n");
		
		return str.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("SampleLog [sampleName=" + this.sampleName 
				+ ", timeStamp=" + this.timeStamp + 
				", current="+ this.current + 
				", error=" + this.error + 
				", pct={");
		for(ValueAtPercentile pc : this.pct) {
			s.append(pc.percentile()+"="+(long)pc.value()+",");
		}
		s.append("}, avg=" + this.avg + 
				", max=" + this.max + 
				", throughput=" + this.throughput + ",");
		
		s.append(" total=" + this.total + 
				", errorTotal=" + this.errorTotal +
				", pctTotal={");
		for(ValueAtPercentile pc : this.pctTotal) {
			s.append(pc.percentile()+"="+(long)pc.value()+",");
		}
		s.append("}, avgTotal=" + this.avgTotal + 
				", maxTotal=" + this.maxTotal + 
				", throughputTotal=" + this.throughputTotal + "]");
		return s.toString();
	}
	
	private double roundDoubleValueTo2DigitsAfterDecimalPoint(double value) {
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(2, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	
}
