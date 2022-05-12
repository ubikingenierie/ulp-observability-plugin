package ubikloadpack.jmeter.ulp.observability.metric;

import java.nio.ByteBuffer;
import java.util.Date;

import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.util.Util;

public class Sample {
	
	private static final Logger log = LoggerFactory.getLogger(Sample.class);
	
	private final Integer pct1;
	private final Integer pct2;
	private final Integer pct3;
	
	private String sampleName;
	private Long nbrErrorsCounter;
	private Long total;
	private Date timeStamp;
	private Histogram responseTimeMetrics;
	private Long sum;
	
	public Sample(String sampleName, 
			Integer pct1, 
			Integer pct2, 
			Integer pct3, 
			Integer pct_precision) {
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		
		this.sampleName = Util.makeMetricName(sampleName);
		this.nbrErrorsCounter = 0L;
		this.total = 0L;
		this.sum = 0L;
		this.responseTimeMetrics = new ConcurrentHistogram(pct_precision);
		this.timeStamp = new Date();

	}


	public String getSampleName() {
		return sampleName;
	}
	
	public Date getTimestamp() {
		return this.timeStamp;
	}
	
	public long getThroughput() {
		long time = ((new Date().getTime() - this.timeStamp.getTime()) / 1000);
		return time>0 ? this.getCurrentRequestCount() / time : 0;
	}

	public long getTotalRequestCount() {
		return this.total;
	}
	
	public long getSum() {
		return this.sum;
	}
	
	public long getCurrentRequestCount() {
		return this.responseTimeMetrics.getTotalCount();
	}
	
	public long getMaxResponseTime() {
		return this.responseTimeMetrics.getMaxValue();
	}
	
	public long getMeanResponseTime() {
		return this.sum / this.responseTimeMetrics.getTotalCount();
	}
	
	public long getErrorPercentage() {
		return this.nbrErrorsCounter / this.responseTimeMetrics.getTotalCount();
	}
	
	public long getPct1() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct1);
	}
	
	public long getPct2() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct2);
	}
	
	public long getPct3() {
		return this.responseTimeMetrics.getValueAtPercentile(this.pct3);
	}
	
	public long getErrorRequestCount() {
		return this.nbrErrorsCounter;
	}

	public void incrementErrorRequestCounter() {
		this.nbrErrorsCounter += 1;
	}
	
	public void incrementTotalRequestCounter() {
		this.total += 1;
	}
	
	public void incrementSumBy(long val) {
		this.sum += val;
	}
	
	public int encodeMetrics(final ByteBuffer byteBuffer) {
		return this.responseTimeMetrics.encodeIntoByteBuffer(byteBuffer);
	}
	
	public void clearSum() {
		this.sum = 0L;
	}
	
	public void clearResponseTimeMetrics() {
		this.responseTimeMetrics.reset();
	}
	
	public void clearNbrErrorsCounter() {
		this.nbrErrorsCounter = 0L;
	}
	
	public void recordValue(Long responseTime) {
		this.responseTimeMetrics.recordValue(responseTime);
	}
	
	public synchronized void clear() {
		this.clearNbrErrorsCounter();
		this.clearResponseTimeMetrics();
		this.clearSum();
		this.timeStamp = new Date();
	}
	
	public synchronized void addResponse(long responseTime, boolean hasError) {

		this.recordValue(responseTime);
		this.incrementSumBy(responseTime);
		this.incrementTotalRequestCounter();
		
		if(hasError) {
			this.incrementErrorRequestCounter();
		}
		
	}
	
	public synchronized SampleLog log() {
		return new SampleLog(
				this.sampleName,
				this.timeStamp,
				this.total,
				this.responseTimeMetrics.getTotalCount(),
				this.nbrErrorsCounter,
				this.pct1,
				this.pct2, 
				this.pct3,
				this.getPct1(),
				this.getPct2(),
				this.getPct3(),
				this.getMeanResponseTime(),
				this.getMaxResponseTime(),
				this.getThroughput());
	}

	public synchronized SampleLog logAndClear() {
		log.info("Sample "+getSampleName()+": Log and flush");
		SampleLog log = this.log();
		this.clear();
		return log;
	}
	

}
