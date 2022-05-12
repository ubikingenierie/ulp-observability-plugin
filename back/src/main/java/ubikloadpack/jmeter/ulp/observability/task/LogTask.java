package ubikloadpack.jmeter.ulp.observability.task;

import java.util.Set;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.SampleRegistry;


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
	private SampleRegistry registry;
	
	public LogTask(SampleRegistry registry) {
		this.registry = registry;
	}
	
	
	/**
	 * Sets name log padding to fit all names in one column, and logs all samples in registry
	 */
	@Override
	public void run() {
		
		Set<String> sampleNames = this.registry.getSampleNames();
		Integer namePadding = 11;
		
		for(String name: sampleNames) {
			if(name.length() > namePadding) {
				namePadding = name.length();
			}
		}
		
		log.info(this.registry.logAndClear().guiLog(namePadding));
	}

}
