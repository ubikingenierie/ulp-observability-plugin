package ubikloadpack.jmeter.ulp.observability.metric;

import java.nio.ByteBuffer;
import java.util.Date;

import org.HdrHistogram.Histogram;

public class ULPObservabilitySample {
	
	private final Integer pct1;
	private final Integer pct2;
	private final Integer pct3;
	
	private String sampleName;
	private Long nbrErrorsCounter;
	private Date timeStamp;
	private Histogram responseTimeMetrics;
	double currentResponse;

	
	
	public ULPObservabilitySample(String sampleName, Integer pct1, Integer pct2, Integer pct3, Integer pct_precision) {
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		
		this.sampleName = sampleName;
		this.nbrErrorsCounter = 0L;
		this.responseTimeMetrics = new Histogram(pct_precision);
		timeStamp = new Date();

	}


	public String getSampleName() {
		return sampleName;
	}
	
	public Date getTimestamp() {
		return this.timeStamp;
	}
	
	public synchronized long getThroughput() {
		return (long) ((double) this.getTotalRequestCount() / ((double) (new Date().getTime() - this.timeStamp.getTime()) / 1000.0));
	}

	public synchronized long getTotalRequestCount() {
		return this.responseTimeMetrics.getTotalCount();
	}
	
	public synchronized long getMaxResponseTime() {
		return this.responseTimeMetrics.getMaxValue();
	}
	
	public synchronized double getMeanResponseTime() {
		return this.responseTimeMetrics.getMean();
	}
	
	public synchronized double getErrorPercentage() {
		return (double) this.nbrErrorsCounter / (double) this.responseTimeMetrics.getTotalCount();
	}
	
	public synchronized double getPct1() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct1);
	}
	
	public synchronized double getPct2() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct2);
	}
	
	public synchronized double getPct3() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct3);
	}
	
	public synchronized double getCurrentResponse() {
		return this.currentResponse;
	}
	
	public synchronized long getErrorRequestCount() {
		return nbrErrorsCounter;
	}

	public synchronized void incrementErrorRequestCounter() {
		this.nbrErrorsCounter += 1;
	}
	
	public synchronized void addResponse(double responseTime, boolean isSuccessful) {

		this.responseTimeMetrics.recordValue((long) responseTime);
		this.currentResponse = responseTime;
		
		if(!isSuccessful) {
			this.nbrErrorsCounter += 1;
		}
		
	}
	
	public synchronized int encodeMetrics(final ByteBuffer byteBuffer) {
		return this.responseTimeMetrics.encodeIntoByteBuffer(byteBuffer);
	}
	
	public synchronized void clearResponseTimeMetrics() {
		this.responseTimeMetrics.reset();
	}
	
	public synchronized void clearNbrErrorsCounter() {
		this.nbrErrorsCounter = 0L;
	}

	public synchronized void clear() {
		this.clearNbrErrorsCounter();
		this.clearResponseTimeMetrics();
	}
	
	@Override
	public String toString() {
		long last = new Date().getTime();
		StringBuilder str = new StringBuilder()
				.append("# HELP "+this.sampleName+"_requests_count The total number of HTTP requests\n")
				.append("# TYPE "+this.sampleName+"_requests_count counter\n")
				.append(this.sampleName+"_requests_count{status=\"total\"} "+ this.getTotalRequestCount() +" "+last+"\n")
				.append(this.sampleName+"_requests_count{status=\"error\"} "+ this.getErrorRequestCount() +" "+last+"\n")
				.append("# HELP "+this.sampleName+"_requests_throughput HTTP request throughput per second\n")
				.append("# TYPE "+this.sampleName+"_requests_throughput gauge\n")
				.append(this.sampleName+"_requests_throughput "+ this.getThroughput() +" "+last+"\n")
				.append("# HELP "+this.sampleName+"_latency_metrics The latency time metrics\n")
				.append("# TYPE "+this.sampleName+"_latency_metrics summary\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"current\"} "+this.getCurrentResponse() +" "+last+"\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"avg\"} "+this.getMeanResponseTime() +" "+last+"\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"max\"} "+this.getMaxResponseTime() +" "+last+"\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"pc"+this.pct1+"\"} "+this.getPct1() +" "+last+"\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"pc"+this.pct2+"\"} "+this.getPct2() +" "+last+"\n")
				.append(this.sampleName+"_latency_metrics{metrics=\"pc"+this.pct3+"\"} "+this.getPct3() +" "+last+"\n");
				
		
		return str.toString();				
	}
	
	
	
	
	

}
