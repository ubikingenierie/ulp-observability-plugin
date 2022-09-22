package com.ubikloadpack.jmeter.ulp.observability.listener;

import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.timeandspace.cronscheduler.CronScheduler;
import com.ubikloadpack.jmeter.ulp.observability.config.ULPODefaultConfig;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;
import com.ubikloadpack.jmeter.ulp.observability.task.LogTask;
import com.ubikloadpack.jmeter.ulp.observability.task.MicrometerTask;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;



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
	
	/**
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ULPObservabilityListener.class);

    /**
     * ULP Observability Jetty server
     */
    private static volatile ULPObservabilityServer ulpObservabilityServer;
    
    /**
     * Sample record logger.
     */
    private static volatile SampleLogger logger;
    
    /**
     * Sample metrics registry.
     */
    private static volatile MicrometerRegistry registry;
    
    /**
     * Sample logger cron scheduler.
     */
    static volatile CronScheduler logCron;
    
    /**
     * Occurred sample result queue
     */
    private static volatile BlockingQueue<ResponseResult> sampleQueue;
    /**
     * List of registry task threads
     */
    private static volatile List<MicrometerTask> micrometerTaskList;
    
    
    
    public void setBufferCapacity(Integer bufferCapacity) {
    	setProperty(ULPODefaultConfig.BUFFER_CAPACITY_PROP, bufferCapacity);
    }
    
    public Integer getBufferCapacity() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.BUFFER_CAPACITY_PROP, 
    			ULPODefaultConfig.bufferCapacity()
    			);
    }
    
    public void setJettyPort(Integer jettyPort) {
    	setProperty(ULPODefaultConfig.JETTY_SERVER_PORT_PROP,jettyPort);
    }
    
    public Integer getJettyPort() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.JETTY_SERVER_PORT_PROP, 
    			ULPODefaultConfig.jettyServerPort()
    			);
    }
    
    public void setMetricsRoute(String metricsRoute) {
    	setProperty(ULPODefaultConfig.JETTY_METRICS_ROUTE_PROP,metricsRoute);
    }
    
    public String getMetricsRoute() {
    	return getPropertyAsString(
    			ULPODefaultConfig.JETTY_METRICS_ROUTE_PROP, 
    			ULPODefaultConfig.jettyMetricsRoute()
    			);
    }
    
    
    public void setWebAppRoute(String webAppRoute) {
    	setProperty(ULPODefaultConfig.JETTY_WEBAPP_ROUTE_PROP,webAppRoute);
    }
    
    public String getWebAppRoute() {
    	return getPropertyAsString(
    			ULPODefaultConfig.JETTY_WEBAPP_ROUTE_PROP, 
    			ULPODefaultConfig.jettyWebAppRoute()
    			);
    }
    
    public void setThreadSize(Integer threadSize) {
    	setProperty(ULPODefaultConfig.THREAD_SIZE_PROP,threadSize);
    }
    
    public Integer getThreadSize() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.THREAD_SIZE_PROP, 
    			ULPODefaultConfig.threadSize()
    			);
    }
    
    
    public void setPct1(Integer pct1) {
    	setProperty(ULPODefaultConfig.PCT1_PROP,pct1);
    }
    
    public Integer getPct1() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.PCT1_PROP, 
    			ULPODefaultConfig.pct1()
    			);
    }
    
    public void setPct2(Integer pct2) {
    	setProperty(ULPODefaultConfig.PCT2_PROP,pct2);
    }
    
    public Integer getPct2() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.PCT2_PROP, 
    			ULPODefaultConfig.pct2()
    			);
    }
    
    public void setPct3(Integer pct3) {
    	setProperty(ULPODefaultConfig.PCT3_PROP,pct3);
    }
    
    public Integer getPct3() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.PCT3_PROP, 
    			ULPODefaultConfig.pct3()
    			);
    }
    
    public void setPctPrecision(Integer pct_precision) {
    	setProperty(ULPODefaultConfig.PCT_PRECISION_PROP, pct_precision);
    }
    
    
    public Integer getPctPrecision() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.PCT_PRECISION_PROP, 
    			ULPODefaultConfig.pctPrecision()
    			);
    }
    
    public void setLogFreq(Integer logFreq) {
    	setProperty(ULPODefaultConfig.LOG_FREQUENCY_PROP, logFreq);
    }
    
    public Integer getLogFreq() {
    	return getPropertyAsInt(
    			ULPODefaultConfig.LOG_FREQUENCY_PROP, 
    			ULPODefaultConfig.logFrequecny()
    			);
    }    
    
    public void setTotalLabel(String totalLbel) {
    	setProperty(ULPODefaultConfig.TOTAL_LABEL_PROP,totalLbel);
    }
    
    public String getTotalLabel() {
    	return getPropertyAsString(
    			ULPODefaultConfig.TOTAL_LABEL_PROP, 
    			ULPODefaultConfig.totalLabel()
    			);
    }
    
    public BlockingQueue<ResponseResult> getSampleQueue(){
    	return sampleQueue;
    }
    
    public ULPObservabilityListener(){
	}
    
    /**
     * Initiates sample queue and registry  
     */
    public void init() {
    	logger = new SampleLogger(getTotalLabel());
    	registry =
    			new MicrometerRegistry(
    					getTotalLabel(),
    					getPct1(),
    					getPct2(),
    					getPct3(),
    					getPctPrecision(),
    					getLogFreq(),
    					logger
    					);
    	
 	    sampleQueue = new ArrayBlockingQueue<>(getBufferCapacity());
 	    
 	    if(getLogFreq()>0) {
 		  logCron = CronScheduler.create(Duration.ofSeconds(getLogFreq()));
		}
 	   
 	    micrometerTaskList = new ArrayList<>(); 
    }
    
    
    
	/**
	 * Receives occurred samples and adds them to sample result queue if possible, log buffer overflow exception otherwise
	 */
	public synchronized void sampleOccurred(SampleEvent sampleEvent) {
		if(sampleEvent != null) {	
			try {
				SampleResult sample = sampleEvent.getResult();
				if(!sampleQueue.offer(
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
					LOG.error(
							"Sample queue overflow. Sample dropped: {}",
							sampleEvent.getThreadGroup());
				}
			} catch (InterruptedException e) {
				LOG.warn(sampleEvent.getResult().getThreadName()+": Interrupting sample queue");
			};
		}
	}

	public void sampleStarted(SampleEvent sampleEvent) {
	    LOG.info("************sampler started**************");
	}

	
	public void sampleStopped(SampleEvent sampleEvent) {
		LOG.info("event stopped");
	}

    
    private static String myName;
    
    private static int instanceCount;
	
    
	/**
	 * Starts test with fresh sample registry and log;
	 * Creates a given number of registry worker threads;
	 * Starts Jetty server if possible
	 */
	public void testStarted() {
		testStarted("local");
	}
    
	public synchronized void testStarted(String host) {
		
		LOG.info("test started : ", host);
		
		if (instanceCount == 0){
			myName = getName();
			init();		
			try {
	 	    	ulpObservabilityServer =
	 	    			new ULPObservabilityServer(
	 	    					getJettyPort(),
	 	    					getMetricsRoute(),
	 	    					getWebAppRoute(),
	 	    					getLogFreq(),
	 	    					getTotalLabel(),
	 	    					logger
	 	    					);
				ulpObservabilityServer.start();
				LOG.info("Jetty Endpoint started\n"
						+ "Port: {}\n"
						+ "Metrics route: {}\n"
						+ "Web app route: {}",
						ulpObservabilityServer.getPort(),
						ulpObservabilityServer.getServer().getURI()+getMetricsRoute(),
						ulpObservabilityServer.getServer().getURI()+getWebAppRoute()
						);
			} catch (Exception e) {
				LOG.error("error while starting Jetty server: {}", e);
			}
			
			if(logCron != null) {
				logCron.scheduleAtFixedRateSkippingToLatest(
						getLogFreq(), 
						getLogFreq(), 
						TimeUnit.SECONDS, 
						new LogTask(registry, sampleQueue)
						);
			}

			
			System.out.printf("ULPO Listener will generate log each %d seconds%n",getLogFreq());
			
			for(int i = 0; i < this.getThreadSize(); i++) {
				micrometerTaskList.add(new MicrometerTask(registry, sampleQueue));
				micrometerTaskList.get(i).start();
			}
			
		}
		
		if (!myName.equals(getName())) {
			throw new java.lang.Error("Two Ulp Observability Listerner are running at the same time");		
		}
		
		instanceCount++;			
			
		
	}

	/**
	 * Ends test and clears sample registry and log;
	 * Stops all running registry task threads;
	 * Stops Jetty server if it is running
	 */
	public void testEnded() {
		testEnded("local");

	}

	public synchronized void testEnded(String host) {
		LOG.info("test stopped : ", host);
		
		instanceCount--;
		
		if(instanceCount == 0) {
			try {
				if(ulpObservabilityServer != null) {
					ulpObservabilityServer.stop();
				}
				LOG.info("Jetty Endpoint stopped");
				
			} catch (Exception e) {
				LOG.error(
						"Jetty server shutdown error: {}", e);
			}
			 
			if(logCron.isThreadRunning()) {
				logCron.shutdownNow();
				logCron.purge();
			}
			micrometerTaskList.forEach((task) -> {
				task.stop();
			});
			 
			sampleQueue.clear();
			logger.clear();
			registry.close();	
			
			LOG.info("All tests have stopped");			
		}
		 	 
     
	}
	
}

	
