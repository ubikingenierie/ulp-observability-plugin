package ubikloadpack.jmeter.ulp.observability.listener;

import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.config.ULPObservabilityDefaultConfig;
import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;
import ubikloadpack.jmeter.ulp.observability.task.LogTask;
import ubikloadpack.jmeter.ulp.observability.task.MicrometerTask;
import ubikloadpack.jmeter.ulp.observability.util.Util;



/**
 * A class for listening and exposing extended JMeter metrics
 * this class extends AbstractTestElement to define it as a test element
 * and implements :
 * SampleListener: to be notified each time a sample occurred;
 * TestStateListener : to be notified each time a test started or ended;
 * NoThreadClone : to use the same thread for all samples, in an other way to disable cloning the tread for each sample;
 * Includes embedded Jetty server, sample registry and logger.
 */

public class ULPObservabilityListener extends AbstractTestElement
             implements SampleListener, TestStateListener, NoThreadClone, Serializable {
	

	private static final long serialVersionUID = 8170705348132535834L;
	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);

    /**
     * ULP Observability Jetty server
     */
    private ULPObservabilityServer ulpObservabilityServer;
    
    /**
     * Logger timer
     */
    private Timer logTimer;
    /**
     * Occurred sample result queue
     */
    private BlockingQueue<ResponseResult> sampleQueue;
    /**
     * List of registry task threads
     */
    private List<MicrometerTask> micrometerTaskList;
    
    /**
     * Sample registry
     */
    private MicrometerRegistry micrometerReg;
    
    
    public void setBufferCapacity(Integer bufferCapacity) {
    	setProperty(ULPObservabilityDefaultConfig.BUFFER_CAPACITY_PROP, bufferCapacity);
    }
    
    public Integer getBufferCapacity() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.BUFFER_CAPACITY_PROP, 
    			ULPObservabilityDefaultConfig.bufferCapacity()
    			);
    }
    
    public void setJettyPort(Integer jettyPort) {
    	setProperty(ULPObservabilityDefaultConfig.JETTY_SERVER_PORT_PROP,jettyPort);
    }
    
    public Integer getJettyPort() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.JETTY_SERVER_PORT_PROP, 
    			ULPObservabilityDefaultConfig.jettyServerPort()
    			);
    }
    
    public void setMetricsRoute(String metricsRoute) {
    	setProperty(ULPObservabilityDefaultConfig.JETTY_METRICS_ROUTE_PROP,metricsRoute);
    }
    
    public String getMetricsRoute() {
    	return getPropertyAsString(
    			ULPObservabilityDefaultConfig.JETTY_METRICS_ROUTE_PROP, 
    			ULPObservabilityDefaultConfig.jettyMetricsRoute()
    			);
    }
    
    
    public void setWebAppRoute(String webAppRoute) {
    	setProperty(ULPObservabilityDefaultConfig.JETTY_WEBAPP_ROUTE_PROP,webAppRoute);
    }
    
    public String getWebAppRoute() {
    	return getPropertyAsString(
    			ULPObservabilityDefaultConfig.JETTY_WEBAPP_ROUTE_PROP, 
    			ULPObservabilityDefaultConfig.jettyWebAppRoute()
    			);
    }
    
    public void setThreadSize(Integer threadSize) {
    	setProperty(ULPObservabilityDefaultConfig.THREAD_SIZE_PROP,threadSize);
    }
    
    public Integer getThreadSize() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.THREAD_SIZE_PROP, 
    			ULPObservabilityDefaultConfig.threadSize()
    			);
    }
    
    
    public void setPct1(Integer pct1) {
    	setProperty(ULPObservabilityDefaultConfig.PCT1_PROP,pct1);
    }
    
    public Integer getPct1() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.PCT1_PROP, 
    			ULPObservabilityDefaultConfig.pct1()
    			);
    }
    
    public void setPct2(Integer pct2) {
    	setProperty(ULPObservabilityDefaultConfig.PCT2_PROP,pct2);
    }
    
    public Integer getPct2() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.PCT2_PROP, 
    			ULPObservabilityDefaultConfig.pct2()
    			);
    }
    
    public void setPct3(Integer pct3) {
    	setProperty(ULPObservabilityDefaultConfig.PCT3_PROP,pct3);
    }
    
    public Integer getPct3() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.PCT3_PROP, 
    			ULPObservabilityDefaultConfig.pct3()
    			);
    }
    
    public void setPctPrecision(Integer pct_precision) {
    	setProperty(ULPObservabilityDefaultConfig.PCT_PRECISION_PROP, pct_precision);
    }
    
    
    public Integer getPctPrecision() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.PCT_PRECISION_PROP, 
    			ULPObservabilityDefaultConfig.pctPrecision()
    			);
    }
    
    public void setLogFreq(Integer logFreq) {
    	setProperty(ULPObservabilityDefaultConfig.LOG_FREQUENCY_PROP, logFreq);
    }
    
    public Integer getLogFreq() {
    	return getPropertyAsInt(
    			ULPObservabilityDefaultConfig.LOG_FREQUENCY_PROP, 
    			ULPObservabilityDefaultConfig.logFrequecny()
    			);
    }
    
    
    public void setMetricsData(String metricsData) {
    	setProperty(ULPObservabilityDefaultConfig.METRICS_DATA_PROP,metricsData);
    }
    
    public String getMetricsData() {
    	return getPropertyAsString(
    			ULPObservabilityDefaultConfig.METRICS_DATA_PROP, 
    			ULPObservabilityDefaultConfig.metricsData()
    			);
    }
    
    public Boolean dataOutputEnabled() {
    	return getPropertyAsBoolean(
    			ULPObservabilityDefaultConfig.ENABLE_DATA_OUTPUT_PROP,
    			ULPObservabilityDefaultConfig.enableDataOutput()
    			);
    }
    
    public void dataOutputEnabled(Boolean dataOutputEnabled) {
    	setProperty(ULPObservabilityDefaultConfig.ENABLE_DATA_OUTPUT_PROP, dataOutputEnabled);
    }
    
    
    public void setTotalLabel(String totalLbel) {
    	setProperty(ULPObservabilityDefaultConfig.TOTAL_LABEL_PROP,totalLbel);
    }
    
    public String getTotalLabel() {
    	return getPropertyAsString(
    			ULPObservabilityDefaultConfig.TOTAL_LABEL_PROP, 
    			ULPObservabilityDefaultConfig.totalLabel()
    			);
    }
    
    public ULPObservabilityListener(){
	}
    
    /**
     * Initiates sample queue and registry  
     */
    public void init() {
    	this.micrometerReg = new MicrometerRegistry(getTotalLabel(), getPct1(), getPct2(), getPct3(), getPctPrecision(), getLogFreq());
 	    this.sampleQueue = new ArrayBlockingQueue<ResponseResult>(getBufferCapacity());
 	    this.logTimer = new Timer();
 	    this.micrometerTaskList = new ArrayList<>(); 
    }
    
	/**
	 * Starts test with fresh sample registry and log;
	 * Creates a given number of registry worker threads;
	 * Starts Jetty server if possible
	 */
	public void testStarted() {
		log.info("test started...");
		init();
		try {
 	    	this.ulpObservabilityServer = new ULPObservabilityServer(
 	    			getJettyPort(), 
 	    			getMetricsRoute(), 
 	    			getWebAppRoute(), 
 	    			getLogFreq(), 
 	    			getTotalLabel(),
 	    			this.micrometerReg.getLogger()
 	    			);
			ulpObservabilityServer.start();
			log.info("Jetty Endpoint started");
		} catch (Exception e) {
			log.error("error while starting Jetty server {}" ,e);
		}
		
		if(getLogFreq()>0) {
			this.logTimer.scheduleAtFixedRate(new LogTask(this.micrometerReg, this.sampleQueue), getLogFreq()*1000, getLogFreq()*1000);
		}
		
		System.out.printf("ULPO Listener will generate log each %d seconds%n",getLogFreq());
		
		for(int i = 0; i < this.getThreadSize(); i++) {
			this.micrometerTaskList.add(new MicrometerTask(this.micrometerReg, this.sampleQueue));
			this.micrometerTaskList.get(i).start();
		}
		
	}
    
    
	/**
	 * Receives occurred samples and adds them to sample result queue if possible, log buffer overflow exception otherwise
	 */
	public void sampleOccurred(SampleEvent sampleEvent) {
		if(sampleEvent != null) {	
			try {
				SampleResult sample = sampleEvent.getResult();
				if(!this.sampleQueue.offer(
						new ResponseResult(
								 sampleEvent.getThreadGroup(),
								 Util.getResponseTime(sample.getEndTime(),sample.getStartTime()),
								 sample.getErrorCount() > 0,
								 sample.getGroupThreads(),
								 sample.getAllThreads()
								 ),
						1000,
						TimeUnit.MILLISECONDS
						)) {
					System.out.println("Sample queue overflow");
					log.error("Sample queue overflow");
					throw new BufferOverflowException();
				}
			} catch (InterruptedException e) {
				log.warn(sampleEvent.getResult().getThreadName()+": Interrupting sample queue");
			};
		}
	}

	public void sampleStarted(SampleEvent sampleEvent) {
	    log.info("************sampler started**************");
	}

	
	public void sampleStopped(SampleEvent sampleEvent) {
		log.info("event stopped");
	}

	public void testStarted(String host) {}

	/**
	 * Ends test and clears sample registry and log;
	 * Stops all running registry task threads;
	 * Stops Jetty server if it is running
	 */
	public void testEnded() {
		
		log.info("test Ended...");
		try {
			if(ulpObservabilityServer != null) {
				ulpObservabilityServer.stop();
			}
			log.info("Jetty Endpoint stopped");
			
		} catch (Exception e) {
			log.error("error while starting Jetty server {}", getJettyPort() ,e);
		}
		 
		 this.sampleQueue.clear();
		 this.micrometerReg.close();
		 
		 this.logTimer.cancel();
		 this.logTimer.purge();
		 this.micrometerTaskList.forEach((task) -> {
				task.stop();
			});;
	}

	public void testEnded(String host) {
		 log.info("test stopped ", host);
	}
	

	
	
	

	
}
