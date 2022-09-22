# Distributed load testing JMeter


## 1)  Without distributed testing :


### testStarted :
	

```
public void testStarted() {
  LOG.info("test started...");
  init();
  ...
  ..


public void init() {
  this.logger = new SampleLogger(getTotalLabel());
  this.registry =
      new MicrometerRegistry(
          getTotalLabel(),
          getPct1(),
          getPct2(),
          getPct3(),
          getPctPrecision(),
          getLogFreq(),
          this.logger
          );
  
  this.sampleQueue = new ArrayBlockingQueue<>(getBufferCapacity());
  
  if(getLogFreq()>0) {
  this.logCron = CronScheduler.create(Duration.ofSeconds(getLogFreq()));
}
  
  this.micrometerTaskList = new ArrayList<>(); 
}
```

- Call to the init method 
- instantiate ```SampleLogger``` logger
- instantiate ```MicrometerRegistry``` registry
- instantiate ```BlockingQueue<ResponseResult>``` sampleQueue
- instantiate ```CronScheduler``` logCron
- instantiate ```List<MicrometerTask>``` micrometerTaskList



```
try {
 	    	this.ulpObservabilityServer =
 	    			new ULPObservabilityServer(
 	    					getJettyPort(),
 	    					getMetricsRoute(),
 	    					getWebAppRoute(),
 	    					getLogFreq(),
 	    					getTotalLabel(),
 	    					this.logger
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

```
- instantiate ```ULPObservabilityServer``` ulpObservabilityServer and start it

```
		if(this.logCron != null) {
			this.logCron.scheduleAtFixedRateSkippingToLatest(
					getLogFreq(), 
					getLogFreq(), 
					TimeUnit.SECONDS, 
					new LogTask(this.registry, this.sampleQueue)
					);
		}

		
		System.out.printf("ULPO Listener will generate log each %d seconds%n",getLogFreq());
```
logCron : delay logging by a specified time


```
		for(int i = 0; i < this.getThreadSize(); i++) {
			this.micrometerTaskList.add(new MicrometerTask(this.registry, this.sampleQueue));
			this.micrometerTaskList.get(i).start();
		}
```
Create new Micrometer tasks. 
Start the worker on each one of the tasks

---
### testEnded
```
public void testEnded() {
		
		LOG.info("test Ended...");
		try {
			if(ulpObservabilityServer != null) {
				ulpObservabilityServer.stop();
			}
			LOG.info("Jetty Endpoint stopped");
			
		} catch (Exception e) {
			LOG.error(
					"Jetty server shutdown error: {}", e);
		}
```		 

- Stop ulpObservabilityServer
```
		if(this.logCron.isThreadRunning()) {
			this.logCron.shutdownNow();
			this.logCron.purge();
		}
		this.micrometerTaskList.forEach((task) -> {
			task.stop();
		});
		 
		this.sampleQueue.clear();
		this.logger.clear();
		this.registry.close();
	}

```
- Stop logCron
- Stop micrometerTaskList
- Clear sampleQueue
- Clear logger
- Close registry


---

## 1)  With distributed testing :
```
    private static volatile String myName;
    
    private static volatile int instanceCount;
```

- two new volatile variable shared between all threads.
- myName is the name of the listener in the test plan
- instanceCount is the number of threads running in distributed testing


```
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
```
- Synchronized method to avoid problems with instanceCount
- Does the same as in local but only for the first thread going into this method.
- instanceCount is incremented.
- If an other ulp Observability listener with another name is running this method, an exeption is throwed.


```
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
```
- going into this method decrement the instanceCount
- If the instanceCount reaches 0, does the same as the former testEnded method

```
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

/**
  * Name of the listener in the test plan
  */
private static volatile String myName;

/**
  * Number of threads running for distributed testing 
  */
private static volatile int instanceCount;
```

- Variables are made ``static volatile`` so they can be shared between the different threads