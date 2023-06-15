package com.ubikloadpack.jmeter.ulp.observability.listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubikloadpack.jmeter.ulp.observability.config.ULPODefaultConfig;
import com.ubikloadpack.jmeter.ulp.observability.log.SampleLogger;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;
import com.ubikloadpack.jmeter.ulp.observability.server.ULPObservabilityServer;
import com.ubikloadpack.jmeter.ulp.observability.task.SampleMetricsLoggingTask;
import com.ubikloadpack.jmeter.ulp.observability.task.MicrometerTask;
import com.ubikloadpack.jmeter.ulp.observability.util.Util;

import io.timeandspace.cronscheduler.CronScheduler;

/**
 * A class for listening and exposing extended JMeter metrics this class extends
 * AbstractTestElement to define it as a test element and implements :
 * SampleListener: to be notified each time a sample occurred; TestStateListener
 * : to be notified each time a test started or ended; NoThreadClone : to use
 * the same thread for all samples, in an other way to disable cloning the tread
 * for each sample; Includes embedded Jetty server, sample registry and logger.
 */

public class ULPObservabilityListener extends AbstractTestElement
		implements SampleListener, TestStateListener, NoThreadClone, Serializable, Remoteable {

	private static final long serialVersionUID = 8170705348132535834L;

	/**
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ULPObservabilityListener.class);

	private class ListenerClientData {
		/**
		 * ULP Observability Jetty server
		 */
		private ULPObservabilityServer ulpObservabilityServer;

		/**
		 * Sample record logger.
		 */
		private SampleLogger logger;

		/**
		 * Sample metrics registry.
		 */
		private MicrometerRegistry registry;

		/**
		 * Sample logger cron scheduler.
		 */
		private CronScheduler logCron;

		/**
		 * Occurred sample result queue
		 */
		private BlockingQueue<ResponseResult> sampleQueue;
		/**
		 * List of registry task threads
		 */
		private List<MicrometerTask> micrometerTaskList;

		/**
		 * Name of the listener in the test plan
		 */
		private String myName;
	}

	private static volatile Boolean isServerRunning = false;
	private static volatile ListenerClientData listenerClientData;

	/**
	 * Lock used to protect instanceCount update
	 */
	private static final Object LOCK = new Object();

	/**
	 * Number of JMeter Servers running for distributed testing
	 */
	private static volatile int instanceCount;

	/**
	 * Optional regex. If empty, every samplers are processed.
	 */
	private transient Optional<Pattern> regex = Optional.empty();
	
	public void setKeepJettyServerUpAfterTestEnd(Boolean bool) {
		setProperty(ULPODefaultConfig.KEEP_JETTY_SERVER_UP_AFTER_TEST_END_PROP, bool);
	}

	public Boolean getKeepJettyServerUpAfterTestEnd() {
		return getPropertyAsBoolean(ULPODefaultConfig.KEEP_JETTY_SERVER_UP_AFTER_TEST_END_PROP,
				ULPODefaultConfig.keepJettyServerUpAfterTestEnd());
	}

	public void setBufferCapacity(Integer bufferCapacity) {
		setProperty(ULPODefaultConfig.BUFFER_CAPACITY_PROP, bufferCapacity);
	}

	public Integer getBufferCapacity() {
		return getPropertyAsInt(ULPODefaultConfig.BUFFER_CAPACITY_PROP, ULPODefaultConfig.bufferCapacity());
	}

	public void setJettyPort(Integer jettyPort) {
		setProperty(ULPODefaultConfig.JETTY_SERVER_PORT_PROP, jettyPort);
	}

	public Integer getJettyPort() {
		return getPropertyAsInt(ULPODefaultConfig.JETTY_SERVER_PORT_PROP, ULPODefaultConfig.jettyServerPort());
	}

	public void setMetricsRoute(String metricsRoute) {
		setProperty(ULPODefaultConfig.JETTY_METRICS_ROUTE_PROP, metricsRoute);
	}

	public String getMetricsRoute() {
		return getPropertyAsString(ULPODefaultConfig.JETTY_METRICS_ROUTE_PROP, ULPODefaultConfig.jettyMetricsRoute());
	}

	public void setWebAppRoute(String webAppRoute) {
		setProperty(ULPODefaultConfig.JETTY_WEBAPP_ROUTE_PROP, webAppRoute);
	}

	public String getWebAppRoute() {
		return getPropertyAsString(ULPODefaultConfig.JETTY_WEBAPP_ROUTE_PROP, ULPODefaultConfig.jettyWebAppRoute());
	}

	public void setThreadSize(Integer threadSize) {
		setProperty(ULPODefaultConfig.THREAD_SIZE_PROP, threadSize);
	}

	public Integer getThreadSize() {
		return getPropertyAsInt(ULPODefaultConfig.THREAD_SIZE_PROP, ULPODefaultConfig.threadSize());
	}

	public void setPct1(Integer pct1) {
		setProperty(ULPODefaultConfig.PCT1_PROP, pct1);
	}

	public Integer getPct1() {
		return getPropertyAsInt(ULPODefaultConfig.PCT1_PROP, ULPODefaultConfig.pct1());
	}

	public void setPct2(Integer pct2) {
		setProperty(ULPODefaultConfig.PCT2_PROP, pct2);
	}

	public Integer getPct2() {
		return getPropertyAsInt(ULPODefaultConfig.PCT2_PROP, ULPODefaultConfig.pct2());
	}

	public void setPct3(Integer pct3) {
		setProperty(ULPODefaultConfig.PCT3_PROP, pct3);
	}

	public Integer getPct3() {
		return getPropertyAsInt(ULPODefaultConfig.PCT3_PROP, ULPODefaultConfig.pct3());
	}

	public void setLogFreq(Integer logFreq) {
		setProperty(ULPODefaultConfig.LOG_FREQUENCY_PROP, logFreq);
	}

	public Integer getLogFreq() {
		return getPropertyAsInt(ULPODefaultConfig.LOG_FREQUENCY_PROP, ULPODefaultConfig.logFrequency());
	}
	
	public int getMicrometerExpiryTimeInSecondsAsInt() {
	    return Integer.parseInt(getMicrometerExpiryTimeInSeconds());
	}

	public String getMicrometerExpiryTimeInSeconds() {
        return getPropertyAsString(ULPODefaultConfig.MICROMETER_EXPIRY_TIME_IN_SECONDS_PROP,
                Integer.toString(ULPODefaultConfig.micrometerExpiryTimeInSeconds()));
    }
    
    public void setMicrometerExpiryTimeInSeconds(String expiryTimeInSeconds) {
        setProperty(ULPODefaultConfig.MICROMETER_EXPIRY_TIME_IN_SECONDS_PROP, expiryTimeInSeconds);
    }

	public void setTotalLabel(String totalLabel) {
		setProperty(ULPODefaultConfig.TOTAL_LABEL_PROP, totalLabel);
	}
	
	public void setRegex(String regex) {
		if(regex == null) {
			setProperty(ULPODefaultConfig.REGEX_PROP, "");
			this.regex = Optional.empty();
		} else {
			try {
				this.regex = Optional.of(Pattern.compile(regex));
				setProperty(ULPODefaultConfig.REGEX_PROP, regex);
			} catch (Exception e) {
				LOG.error("Following regex is not valid : {}", regex);
				setProperty(ULPODefaultConfig.REGEX_PROP, "");
				this.regex = Optional.empty();
			}
		}
	}

	public String getTotalLabel() {
		return Util.makeOpenMetricsName(getPropertyAsString(ULPODefaultConfig.TOTAL_LABEL_PROP, ULPODefaultConfig.totalLabel()));
	}
	
	public String getRegex() {
		return getPropertyAsString(ULPODefaultConfig.REGEX_PROP, ULPODefaultConfig.regex());
	}

	public BlockingQueue<ResponseResult> getSampleQueue() {
		return listenerClientData.sampleQueue;
	}

	public ULPObservabilityListener() {
	}

	/**
	 * Initiates sample queue and registry
	 */
	public void init(ListenerClientData listenerClientData) {
		listenerClientData.logger = new SampleLogger(getTotalLabel());
		listenerClientData.registry = new MicrometerRegistry(getTotalLabel(), getPct1(), getPct2(), getPct3(),
				getLogFreq(), listenerClientData.logger, getMicrometerExpiryTimeInSecondsAsInt());

		listenerClientData.sampleQueue = new ArrayBlockingQueue<>(getBufferCapacity());

		listenerClientData.logCron = CronScheduler.create(Duration.ofSeconds(getLogFreq()));

		listenerClientData.micrometerTaskList = new ArrayList<>();
	}

	/**
	 * Receives occurred samples and adds them to sample result queue if possible,
	 * log buffer overflow exception otherwise
	 */
	public void sampleOccurred(SampleEvent sampleEvent) {
		
		if (sampleEvent != null) {
			try {
				SampleResult sample = sampleEvent.getResult();
				boolean hasError = !sample.isSuccessful();
				String sampleLabel = sample.getSampleLabel();
				
				if(isStringMatchingRegex(sampleLabel)) {
					if (!listenerClientData.sampleQueue.offer(new ResponseResult(sampleEvent.getThreadGroup(),
							Util.getResponseTime(sample.getEndTime(), sample.getStartTime()), hasError,
							sample.getGroupThreads(), sample.getAllThreads(), sample.getSampleLabel(), sample.getStartTime(),
							sample.getEndTime()), 1000, TimeUnit.MILLISECONDS)) {
						LOG.error("Sample queue overflow. Sample dropped: {}", sampleEvent.getThreadGroup());
					}
				}
				
			} catch (InterruptedException e) {
				LOG.warn(sampleEvent.getResult().getThreadName() + ": Interrupting sample queue");
			}
		}
	}
	
	private boolean isStringMatchingRegex(String str) {
		try {
			Optional<Pattern> optionalPattern = this.regex;
			if(optionalPattern.isPresent()) {
				Matcher matcher = optionalPattern.get().matcher(str);
				return matcher.find();
			} else {
				return true; // if there is no regex, everything is considered a match
			}
		} catch(Exception e) {
			return false;
		}
	}

	public void sampleStarted(SampleEvent sampleEvent) {
		// NOOP
	}

	public void sampleStopped(SampleEvent sampleEvent) {
		// NOOP
	}

	/**
	 * Starts test with fresh sample registry and log; Creates a given number of
	 * registry worker threads; Starts Jetty server if needed
	 */
	public void testStarted() {
		testStarted("local");
	}

	/**
	 * Starts test with fresh sample registry and log; Creates a given number of
	 * registry worker threads; Starts Jetty server if needed
	 * 
	 * @param host Host of jmeter-server
	 */
	public void testStarted(String host) {

		LOG.info("Test started from host {}", host);

		synchronized (LOCK) {
			// Init the Pattern regex object of the listener based on its saved String value.
			String regexString = getRegex();
			this.setRegex(regexString);
			if(regexString != null && regexString != "") {
				LOG.info("Observability plugin uses this regex : {} to filter rendered samplers based on their names.", regexString);
			} else {
				LOG.info("Observability plugin does not uses any regex to filter samplers based on their names.");
			}
			
			if (instanceCount == 0) {
				if (isServerRunning) {
					LOG.info("Jetty server was running from host {}, stopping it", host);
					stopJettyServer();
				}
				listenerClientData = new ListenerClientData();
				isServerRunning = true;
				listenerClientData.myName = getName();
				init(listenerClientData);
				try {
					listenerClientData.ulpObservabilityServer = new ULPObservabilityServer(getJettyPort(),
							getMetricsRoute(), getWebAppRoute(), getLogFreq(), getTotalLabel(),
							listenerClientData.logger);
					listenerClientData.ulpObservabilityServer.start();
					LOG.info("Webapp Endpoint started\n" + "Port: {}\n" + "Metrics route: {}\n" + "Web app route: {}",
							listenerClientData.ulpObservabilityServer.getPort(),
							computeUrl(listenerClientData.ulpObservabilityServer.getServer(), getMetricsRoute()),
							computeUrl(listenerClientData.ulpObservabilityServer.getServer(), getWebAppRoute()));
				} catch (Exception e) {
					throw new IllegalStateException("Error while starting Jetty server on port "
							+ listenerClientData.ulpObservabilityServer.getPort() + " : ", e);
				}

				listenerClientData.logCron.scheduleAtFixedRateSkippingToLatest(getLogFreq(), getLogFreq(),
						TimeUnit.SECONDS, new SampleMetricsLoggingTask(listenerClientData.registry, listenerClientData.sampleQueue));

				System.out.printf("UbikLoadPack Observability Plugin will generate log each %d seconds%n", getLogFreq());

				for (int i = 0; i < this.getThreadSize(); i++) {
					MicrometerTask task = new MicrometerTask(listenerClientData.registry,
							listenerClientData.sampleQueue);
					listenerClientData.micrometerTaskList.add(task);
					task.start();
				}

			}

			if (!listenerClientData.myName.equals(getName())) {
				throw new IllegalStateException("You have at least 2 UbikLoadPack Observability Plugins in your Test plan : "
						+ listenerClientData.myName + " and " + getName());
			}

			instanceCount++;
		}
	}

	private String computeUrl(Server server, String contextPath) {
	    URI uri = server.getURI();
        return uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort()+contextPath;
    }

    /**
	 * Ends test and clears sample registry and log; Stops all running registry task
	 * threads; Stops Jetty server if it is running
	 */
	public void testEnded() {
		testEnded("local");

	}

	/**
	 * Ends test and clears sample registry and log; Stops all running registry task
	 * threads; Stops Jetty server if it is running
	 * 
	 * @param host Host of jmeter-server
	 */
	public void testEnded(String host) {
		LOG.info("Test stopped : {}", host);
		synchronized (LOCK) {
			instanceCount--;
			if (instanceCount == 0) {
				LOG.info("No more test running, shutting down");

				try {
					if (listenerClientData.logCron.isThreadRunning()) {
						// make last logs, then shutdown cron task
						listenerClientData.registry.logAndReset();
						System.out.println(listenerClientData.registry.guiLog());
						listenerClientData.logCron.shutdownNow();
						listenerClientData.logCron.purge();
					}
				} catch (Exception e) {
					LOG.error("Logcron shutdown error: {}", e);
				}

				try {
					listenerClientData.micrometerTaskList.forEach((task) -> {
						task.stop();
					});
				} catch (Exception e) {
					LOG.error("Micrometer shutdown error: {}", e);
				}

				listenerClientData.sampleQueue.clear();
				listenerClientData.registry.close();

				if (!getKeepJettyServerUpAfterTestEnd()) {
					stopJettyServer();
				}
				LOG.info("All JMeter servers have stopped their test");
			}
		}
	}

	public void stopJettyServer() {
		synchronized (LOCK) {
			listenerClientData.logger.clear();
			try {
				if (listenerClientData.ulpObservabilityServer != null) {
					listenerClientData.ulpObservabilityServer.stop();
				}
				isServerRunning = false;
				LOG.info("Jetty Server stopped");

			} catch (Exception e) {
				LOG.error("Jetty Server shutdown error: {}", e);
			}
		}
	}

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String regex = getRegex();
        setRegex(regex);
    }
}
