package ubikloadpack.jmeter.ulp.observability.listener;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.jmeter.engine.util.NoThreadClone;
//import org.HdrHistogram.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityConfig;
import ubikloadpack.jmeter.ulp.observability.metric.ULPObservabilityMetricModel;
import ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;
import ubikloadpack.jmeter.ulp.observability.util.Util;




/**
 * a class for listening and exposing Jmeter metrics
 * this class extends AbstractTestElement to define it as a test element
 * and implements :
 * SampleListener: to be notified each time a sample occurred
 * TestStateListener : to be notified each time a test started or ended
 * NoThreadClone : to use the same thread for all samples, in an other way to disable cloning the tread for each sample 
 */

public class ULPObservabilityListener extends AbstractTestElement
             implements SampleListener, TestStateListener, NoThreadClone, Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);
	
	
	// String for the name of the sample,  for the model
	private HashMap<String, ULPObservabilityMetricModel> metrics;
   // Histogram histogram = new Histogram(ULPObservabilityConfig.NBR_SIGNIFICANT_DIGITS);

    private ULPObservabilityServer ulpObservabilityServer;
    private MetricsServlet metricsServlet;
    
    public ULPObservabilityListener(){
    	
    	  // init();
    	   // DefaultExports.initialize();
    	    	    
	}
    
    
    // initiate metric structure and server with servlet
    public void init() {
    	
    	 this.metrics = new HashMap<String, ULPObservabilityMetricModel>();
 	     this.ulpObservabilityServer = new ULPObservabilityServer(ULPObservabilityConfig.JETTY_SERVER_PORT);
 	     this.metricsServlet = new MetricsServlet();
 	     this.ulpObservabilityServer.addServletWithMapping(metricsServlet, ULPObservabilityConfig.METRICS_ENDPOINT_NAME);
 	     
    }
    
    
    /**
     * inititate the hashMap metrics with sample name + a key
     * to be able to distinguish samples in case of multisampling
     * @param samplerName sample name
     */
    private ULPObservabilityMetricModel initSampleAttributes(String samplerName) {
    	     
    	    ULPObservabilityMetricModel ulpObservabilityMetricModel  = new ULPObservabilityMetricModel(Util.makeMetricName(samplerName));
    	    this.metrics.putIfAbsent(samplerName , ulpObservabilityMetricModel);
    	    
    	    return ulpObservabilityMetricModel;
    	   
    }
	
    
    
    
	public void sampleOccurred(SampleEvent sampleEvent) {
		
		    String sampleName = sampleEvent.getResult().getSampleLabel();
		    ULPObservabilityMetricModel ulpObservabilityMetrics;
		    
		    // if the sample association already exists get it, otherwise, create it
		    if(this.metrics.containsKey(sampleName)){
		    	
			     ulpObservabilityMetrics = this.metrics.get(sampleName);
		    }
		    else {
		    	
		    	 ulpObservabilityMetrics = initSampleAttributes(sampleName);
		    }
		    
		    ulpObservabilityMetrics.incrementNbrRequests();
		    ulpObservabilityMetrics.setResponseTime(Util.getResponseTime(sampleEvent.getResult().getEndTime(),
		    		                                    sampleEvent.getResult().getStartTime()));
	
		    
		    log.info("*************************");
		    log.info("Media Type : {} ", sampleEvent.getResult().getMediaType());
		    log.info("Data Type : {}", sampleEvent.getResult().getDataType());
		    log.info("Request Counter : {}", ulpObservabilityMetrics.getNbrRequests());
		    log.info("Thread Name : {}", sampleEvent.getResult().getThreadName());
		    log.info("Sampler Name : {}", sampleEvent.getResult().getSampleLabel());
		    log.info("Sampler Data : {}", sampleEvent.getResult().getSamplerData());
		    log.info("Sample count : {}", sampleEvent.getResult().getSampleCount());
		    log.info("Start Time : {}", sampleEvent.getResult().getStartTime());
		    log.info("End Time : {}", sampleEvent.getResult().getEndTime());
		    log.info("Connection Time : {}", sampleEvent.getResult().getConnectTime());
		    log.info("Response Code : {}", sampleEvent.getResult().getResponseCode());
		    log.info("Response Message : {}", sampleEvent.getResult().getResponseMessage());
		    //log.info("Error count : {}", sampleEvent.getResult().getErrorCount());
		    if(sampleEvent.getResult().getErrorCount() == 1) {
		    	ulpObservabilityMetrics.incrementNbrErrors();
		    }
		    log.info("Error count : {}", ulpObservabilityMetrics.getNbrErrors());
		    /**
		     * this.counters.replace("nbrErrors", this.counters.get("nbrErrors")+sampleEvent.getResult().getErrorCount());
		     */
		    log.info("*************************");
		  
	}

	public void sampleStarted(SampleEvent sampleEvent) {

		    log.info("************sampler started**************");
			
	}

	
	public void sampleStopped(SampleEvent sampleEvent) {
	
		    log.info("event stopped");
	}

	public void testStarted() {
		
		 log.info("test started...");
		 try {
			
			init();
			ulpObservabilityServer.start();
			log.info("Jetty Endpoint started");
			
		} catch (Exception e) {
			
			log.error("error while starting Jetty server {}", ulpObservabilityServer.getPort() ,e);
		
			
		}
	}

	public void testStarted(String host) {}

	public void testEnded() {
		
		
		 try {
			 
			log.info("test Ended...");
			ulpObservabilityServer.stop();
			
			for (ULPObservabilityMetricModel ulpObservabilityMetricModel : metrics.values()) {
				ulpObservabilityMetricModel.clearMetrics();
			}
			
			log.info("Jetty Endpoint stopped");
			
		 } catch (Exception e) {
			
			log.error("error while starting Jetty server {}", ulpObservabilityServer.getPort() ,e);
		
		 }
	}

	public void testEnded(String host) {
		
		 log.info("test stopped ", host);
	}

	public HashMap<String, ULPObservabilityMetricModel> getMetrics() {
		return metrics;
	}

	public void setMetrics(HashMap<String, ULPObservabilityMetricModel> metrics) {
		this.metrics = metrics;
	}

	
	
	

	
}
