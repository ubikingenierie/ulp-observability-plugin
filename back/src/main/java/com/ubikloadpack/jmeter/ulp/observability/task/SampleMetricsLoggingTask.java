package com.ubikloadpack.jmeter.ulp.observability.task;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.timeandspace.cronscheduler.CronTask;
import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;


/**
 * Runnable cron task that creates new period log records for accumulated sample metrics and then resets metrics
 * 
 * @author Valentin ZELIONII
 *
 */
public class SampleMetricsLoggingTask implements CronTask{
	
	/**
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SampleMetricsLoggingTask.class);
	/**
     * Sample metrics registry.
     */
	private MicrometerRegistry registry;
	
	/**
     * Occurred sample result queue
     */
	private Collection<ResponseResult> sampleQueue;
	
	
	/**
	 * New sample metrics logging task.
	 * @param Registry sample metrics registry
	 * @param sampleQueue Occurred sample result queue
	 */
	public SampleMetricsLoggingTask(
			MicrometerRegistry registry,
			Collection<ResponseResult> sampleQueue
			) {
		this.registry = registry;
		this.sampleQueue = sampleQueue;
	}
	
	
	/**
	 * Logs all samples in registry and shows summary in JMeter terminal, logs current sample buffer size in debug console
	 */
	@Override
	public void run(long scheduledRunTimeMillis) {
		try {
			LOG.info("Sample buffer : {}",this.sampleQueue.size());
			this.registry.computeAndReset();
			LOG.info(this.registry.guiLog());
		} catch (Exception e) {
			LOG.error("Error while running the log task.", e);
		}
	}

}
