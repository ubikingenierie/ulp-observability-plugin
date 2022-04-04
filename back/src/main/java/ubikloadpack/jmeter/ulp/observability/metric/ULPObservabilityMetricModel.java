package ubikloadpack.jmeter.ulp.observability.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
//import org.HdrHistogram.Histogram;
import ubikloadpack.jmeter.ulp.observability.listener.ULPObservabilityListener;


/**
 * a class for storing and keep tracking of the metrics gotten and calculated from Jmeter Sample Listener
 *
 */

public class ULPObservabilityMetricModel {
	
	
	    private String sampleName;

		//private Histogram histogram;
		private Counter nbrRequestsCounter;
		private Counter nbrErrorsCounter;
		private Gauge responseTime;

		private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);

		
		public ULPObservabilityMetricModel(String sampleName) {
			
			this.sampleName = sampleName;
			
			log.info("sample name {}", sampleName);
				
			this.nbrRequestsCounter = Counter.build()
					                       .namespace(this.sampleName)
										   .name("nbr_requests")
					                       .help("the total number of requests for all threads")
					                       .register();
			
			this.nbrErrorsCounter = Counter.build()
											.namespace(this.sampleName)
											.name("nbr_errors")
											.help("the total number of errors for all threads")
											.register();
			
			this.responseTime = Gauge.build()
										.namespace(this.sampleName)
										.name("response_time")
										.help("response time of the request")
										.register();
			
		//	this.histogram = new Histogram(ULPObservabilityConfig.NBR_SIGNIFICANT_DIGITS);
		}
		
	
		public double getNbrRequests() {
			return nbrRequestsCounter.get();
		}
		
		
		public void incrementNbrRequests() {
		 	  this.nbrRequestsCounter.inc();
		}
		
		public void incrementNbrRequestsWith(double nbrRequests) {
			
		 	  this.nbrRequestsCounter.inc(nbrRequests);
		}
	


		public double getResponseTime() {
			
			  return this.responseTime.get();
		}

		public void setResponseTime(double responseTime) {
			
		 	  this.responseTime.set(responseTime);
		}
		
		public void setNbrErrorsWith(double nbrErrors){
			
		 	  this.nbrErrorsCounter.inc(nbrErrors);
		}
		
		public double getNbrErrors() {
			
			return this.nbrErrorsCounter.get();
		}

		public void incrementNbrErrors() {
			
		 	  this.nbrErrorsCounter.inc();
		}
		
		public void incrementNbrErrorsWith(double nbrErrors){
			
		 	  this.nbrErrorsCounter.inc(nbrErrors);
		}

		
		
		public void clearMetrics() {
			
			this.nbrErrorsCounter.clear();
			this.nbrRequestsCounter.clear();
			this.responseTime.clear();
			
			this.nbrErrorsCounter.describe();
			this.nbrRequestsCounter.describe();
			this.responseTime.describe();
			
			this.nbrRequestsCounter.remove();
			this.nbrErrorsCounter.remove();
			this.responseTime.remove();
		}
		

//		public Histogram getHistogram() {
//			return histogram;
//		}
//
//		public void setHistogram(Histogram histogram) {
//			this.histogram = histogram;
//		}
		
		
		
		
		
		
		
		
}
