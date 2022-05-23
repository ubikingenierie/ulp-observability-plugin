package ubikloadpack.jmeter.ulp.observability.task;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;


/**
 * Runnable task that creates new period log for accumulated sample metrics and then resets metrics
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
	
	private BlockingQueue<ResponseResult> sampleQueue;
	
	
	
	public LogTask(MicrometerRegistry registry, BlockingQueue<ResponseResult> sampleQueue) {
		this.registry = registry;
		this.sampleQueue = sampleQueue;
	}
	
	
	/**
	 * Sets name log padding to fit all names in one column, and logs all samples in registry
	 */
	@Override
	public void run() {
		
		Integer namePadding = 11;
		List<String> names = this.registry.getSampleNames();
		for(String name: names) {
			if(name.length() > namePadding) {
				namePadding = name.length();
			}
		}
		this.registry.logAndReset().guiLog(namePadding);
		log.info("{}",this.sampleQueue.size());
//		log.info(this.registry.logAndReset().guiLog(namePadding));
	}

}
