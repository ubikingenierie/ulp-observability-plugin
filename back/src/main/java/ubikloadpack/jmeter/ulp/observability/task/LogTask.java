package ubikloadpack.jmeter.ulp.observability.task;

import java.util.Collection;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;


/**
 * Runnable task that creates new period log records for accumulated sample metrics and then resets metrics
 * 
 * @author Valentin ZELIONII
 *
 */
public class LogTask extends TimerTask{
	
	/**
	 * Debug logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LogTask.class);
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
	public LogTask(
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
	public void run() {
		LOG.warn("Sample buffer : {}",this.sampleQueue.size());
		this.registry.logAndReset();
		System.out.println(this.registry.guiLog());
	}

}
