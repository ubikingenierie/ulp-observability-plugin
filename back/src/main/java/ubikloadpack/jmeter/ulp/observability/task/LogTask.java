package ubikloadpack.jmeter.ulp.observability.task;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

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
	private static final Logger log = LoggerFactory.getLogger(LogTask.class);
	
	/**
	 * Sample registry to log results
	 */
	private MicrometerRegistry registry;
	/**
	 * Sample result buffer for debug purposes
	 */
	private BlockingQueue<ResponseResult> sampleQueue;
	
	public LogTask(MicrometerRegistry registry) {
		this(registry, null);
	}
	
	public LogTask(MicrometerRegistry registry, BlockingQueue<ResponseResult> sampleQueue) {
		this.registry = registry;
		this.sampleQueue = sampleQueue;
	}
	
	
	/**
	 * Logs all samples in registry and shows summary in JMeter terminal, logs current sample buffer size in debug console
	 */
	@Override
	public void run() {
		if(this.sampleQueue != null) {
			log.warn("Sample buffer : {}",this.sampleQueue.size());
		}
		System.out.println(this.registry.logAndReset().guiLog());
	}

}
