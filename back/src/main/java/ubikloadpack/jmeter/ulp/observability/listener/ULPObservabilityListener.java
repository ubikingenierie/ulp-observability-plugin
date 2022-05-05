package ubikloadpack.jmeter.ulp.observability.listener;

import java.io.Serializable;
import org.apache.jmeter.engine.util.NoThreadClone;

//import org.HdrHistogram.Histogram;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;
import ubikloadpack.jmeter.ulp.observability.metric.ULPObservabilitySampleRegistry;
import ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;
import ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServlet;
import ubikloadpack.jmeter.ulp.observability.util.SampleProcessThread;




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
	

	private static final long serialVersionUID = 8170705348132535834L;
	public static final String JETTY_SERVER_PORT = "ULPObservability.JettyPort";
	public static final String JETTY_METRICS_ENDPOINT = "ULPObservability.JettyMetricsEndpoint";
	public static final String PCT1 = "ULPObservability.Pct1";
	public static final String PCT2 = "ULPObservability.Pct2";
	public static final String PCT3 = "ULPObservability.Pct3";
	public static final String PCT_PRECISION = "ULPObservability.PctPrecision";
	public static final String LOG_FREQUENCY = "ULPObservability.LogFrequency";
	public static final String METRICS_DATA = "ULPObservability.MetricsData";
	public static final String ENABLE_DATA_OUTPUT = "ULPObservability.EnableDataOutput";
	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);
	
	
	private ULPObservabilitySampleRegistry sampleRegistry;
    private ULPObservabilityServer ulpObservabilityServer;
    private ULPObservabilityServlet ulpObservabilityServlet;
    
    public void setJettyPort(Integer jettyPort) {
    	setProperty(JETTY_SERVER_PORT,jettyPort);
    }
    
    public Integer getJettyPort() {
    	return getPropertyAsInt(JETTY_SERVER_PORT, ULPObservabilityDefaultConfig.JETTY_SERVER_PORT);
    }
    
    public void setMetricsEndpoint(String metricsEndpoint) {
    	setProperty(JETTY_METRICS_ENDPOINT,metricsEndpoint);
    }
    
    public String getMetricsEndpoint() {
    	return getPropertyAsString(JETTY_METRICS_ENDPOINT, ULPObservabilityDefaultConfig.METRICS_ENDPOINT_NAME);
    }
    
    
    public void setPct1(Integer pct1) {
    	setProperty(PCT1,pct1);
    }
    
    public Integer getPct1() {
    	return getPropertyAsInt(PCT1, ULPObservabilityDefaultConfig.PCT1);
    }
    
    public void setPct2(Integer pct2) {
    	setProperty(PCT2,pct2);
    }
    
    public Integer getPct2() {
    	return getPropertyAsInt(PCT2, ULPObservabilityDefaultConfig.PCT2);
    }
    
    public void setPct3(Integer pct3) {
    	setProperty(PCT3,pct3);
    }
    
    public Integer getPct3() {
    	return getPropertyAsInt(PCT3, ULPObservabilityDefaultConfig.PCT3);
    }
    
    public void setPctPrecision(Integer pct_precision) {
    	setProperty(PCT_PRECISION, pct_precision);
    }
    
    
    public Integer getPctPrecision() {
    	return getPropertyAsInt(PCT_PRECISION, ULPObservabilityDefaultConfig.NBR_SIGNIFICANT_DIGITS);
    }
    
    public void setLogFreq(Integer logFreq) {
    	setProperty(LOG_FREQUENCY,logFreq);
    }
    
    public Integer getLogFreq() {
    	return getPropertyAsInt(LOG_FREQUENCY, ULPObservabilityDefaultConfig.LOG_FREQUENCY);
    }
    
    
    public void setMetricsData(String metricsData) {
    	setProperty(METRICS_DATA,metricsData);
    }
    
    public String getMetricsData() {
    	return getPropertyAsString(METRICS_DATA, ULPObservabilityDefaultConfig.METRIC_DATA);
    }
    
    public Boolean dataOutputEnabled() {
    	return getPropertyAsBoolean(ENABLE_DATA_OUTPUT, ULPObservabilityDefaultConfig.ENABLE_DATA_OUTPUT);
    }
    
    public void dataOutputEnabled(Boolean dataOutputEnabled) {
    	setProperty(ENABLE_DATA_OUTPUT, dataOutputEnabled);
    }
    
    public ULPObservabilityListener(){
	}
    
    
    
    // initiate metric structure and server with servlet
    public void init() {
    	this.sampleRegistry = new ULPObservabilitySampleRegistry(
    			getPct1(),
    			getPct2(),
    			getPct3(),
    			getPctPrecision(),
    			getMetricsData(),
    			dataOutputEnabled()
    	);
    	this.ulpObservabilityServer = new ULPObservabilityServer(getJettyPort());
 	    this.ulpObservabilityServlet = new ULPObservabilityServlet(sampleRegistry);
 	    this.ulpObservabilityServer.addServletWithMapping(ulpObservabilityServlet, "/"+getMetricsEndpoint());
    }
    
    
    
    
	public void sampleOccurred(SampleEvent sampleEvent) {
		new SampleProcessThread(this.sampleRegistry, sampleEvent.getResult()).run();
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
			log.info("Jetty Endpoint stopped");
			
			sampleRegistry.clear();
			
		 } catch (Exception e) {
			
			log.error("error while starting Jetty server {}", ulpObservabilityServer.getPort() ,e);
		
		 }
	}

	public void testEnded(String host) {
		
		 log.info("test stopped ", host);
	}
	

	
	
	

	
}
