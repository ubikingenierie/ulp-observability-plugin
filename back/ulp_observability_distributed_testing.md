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
??

```
		for(int i = 0; i < this.getThreadSize(); i++) {
			this.micrometerTaskList.add(new MicrometerTask(this.registry, this.sampleQueue));
			this.micrometerTaskList.get(i).start();
		}
```
???

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
private static final class ListenerClientData {
  
    private BlockingQueue<ResponseResult> queue;

    private int instanceCount; // number of active tests
    
    private List<MicrometerTask> micrometerTaskList;
    
    private CronScheduler logCron;

    }
```
contains :

- private BlockingQueue<ResponseResult> queue : equivalent to sampleQueue
- int intanceCount : number of test on this client
- List<MicrometerTask> micrometerTaskList
- CronScheduler logCron
```
private static final Map<String, ListenerClientData> queuesByTestElementName =
            new ConcurrentHashMap<>();
```
 - a mapping between the thread name and his associated ListenerClientData
```
  private transient String myName;

  private transient ListenerClientData listenerClientData;
```
- Declaration of transient String myName
- transient ListenerClientData listenerClientData;


```
public synchronized void testStarted(String host) {
  
  LOG.info("test started ", host);
		
  if (queuesByTestElementName.size() == 0){
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
        }
```
- synchronized method for  queuesByTestElementName.size()
- only if queuesByTestElementName.size() == 0 : 
  - instantiate ```ULPObservabilityServer``` ulpObservabilityServer and start it
  - instantiate ```MicrometerRegistry``` registry


```
myName = getName();
int queueSize = this.getBufferCapacity();

if (listenerClientData == null) {
        listenerClientData = new ListenerClientData();
        listenerClientData.queue = new ArrayBlockingQueue<>(queueSize);

        listenerClientData.logCron.scheduleAtFixedRateSkippingToLatest(
            getLogFreq(), 
            getLogFreq(), 
            TimeUnit.SECONDS, 
            new LogTask(this.registry, listenerClientData.queue)
            );       
        System.out.printf("ULPO Listener will generate log each %d seconds%n",getLogFreq());
        queuesByTestElementName.put(myName, listenerClientData);


}

```
- myName = getName();  get name of the thread
- if listenerClientData doesn't aldready exist : create one and put it in the map



testElement =!
thread =! host
