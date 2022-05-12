package ubikloadpack.jmeter.ulp.observability.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.metric.SampleRegistry;

/**
 * Runnable task that retrieves sample results from queue if not empty and adds them to registry
 * 
 * @author Valentin ZELIONII
 *
 */
public class SampleProcessTask implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(SampleProcessTask.class);
	
	/**
	 * Sample registry to add results
	 */
	
	private SampleRegistry sampleRegistry;
	
	/**
	 * Sample queue to retrieve results from
	 */
	private BlockingQueue<ResponseResult> sampleQueue;
	
	/**
	 * Represents the running state of task
	 */
	private AtomicBoolean running = new AtomicBoolean(false);
	
    /**
     * Represents the terminated state of task
     */
    private AtomicBoolean terminated = new AtomicBoolean(true);
    
    
	/**
	 * Worker thread
	 */
	private Thread worker;
	
	public SampleProcessTask(SampleRegistry sampleRegistry, BlockingQueue<ResponseResult> sampleQueue) {
		this.sampleRegistry = sampleRegistry;
		this.sampleQueue = sampleQueue;
	}
	
	/**
	 * Start the task and initiate worker thread
	 */
	public void start() {
        worker = new Thread(this);
        worker.start();
    }
	
	/**
	 * Stop the task
	 */
	public void stop() {
        running.set(false);
    }

    /**
     * Interrupt the task
     */
    public void interrupt() {
    	worker.interrupt();
        running.set(false);
    }

    /**
     * Check task is running
     * 
     * @return true if running
     */
    boolean isRunning() {
        return running.get();
    }

    /**
     * Check task was terminated without interruption
     * 
     * @return
     */
    boolean isTerminated() {
        return terminated.get();
    }

    
	/**
	 * Sets task to running state and starts the loop of adding retrieved from queue sample results to registry, wait if queue is empty. 
	 * Sets task to terminated state if stopped without interruption, warn if interrupted unexpectedly
	 */
	@Override
	public void run() {
		running.set(true);
		terminated.set(false);
		while(running.get()) {
			try {
				sampleRegistry.processSample(sampleQueue.take());
			} catch (InterruptedException e) {
				log.warn("Sample process task interrupted");
				interrupt();
				
			};
		}
		terminated.set(true);
	}
	


}
