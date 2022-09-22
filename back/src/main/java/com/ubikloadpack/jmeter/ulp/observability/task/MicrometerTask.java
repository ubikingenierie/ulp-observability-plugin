package com.ubikloadpack.jmeter.ulp.observability.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import com.ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;

/**
 * Runnable task that retrieves sample results
 *  from buffer and records them in registry.
 * @author Valentin ZELIONII
 *
 */
public class MicrometerTask implements Runnable {
	
	/**
	 * Debug logger.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(MicrometerTask.class);

	/**
	 * Task run status.
	 */
	private AtomicBoolean running = new AtomicBoolean(false);
	/**
	 * Task terminated status.
	 */
	private AtomicBoolean terminated = new AtomicBoolean(true);
	/**
	 * Main worker thread.
	 */
	private Thread worker;
	
	/**
     * Sample metrics registry.
     */
	private MicrometerRegistry registry;
	
	/**
     * Occurred sample result queue
     */
	private BlockingQueue<ResponseResult> sampleQueue;

	/**
	 * New micrometer sample record task.
	 * @param Registry sample metrics registry
	 * @param sampleQueue Occurred sample result queue
	 */
	public MicrometerTask(
			MicrometerRegistry registry,
			BlockingQueue<ResponseResult> sampleQueue
			) {
		this.registry = registry;
		this.sampleQueue = sampleQueue;
	}

	/**
	 * Starts the task with new worker thread.
	 */
	public void start() {
        worker = new Thread(this);
        worker.start();
    }

	/**
	 * Set run status to stop.
	 */
	public void stop() {
        running.set(false);
    }

    /**
     * Interrupt worker thread and set run status to stop.
     */
    public void interrupt() {
    	worker.interrupt();
        running.set(false);
    }

    /**
     * @return Task run status
     */
    boolean isRunning() {
        return running.get();
    }


    /**
     * @return Task terminated status
     */
    boolean isTerminated() {
        return terminated.get();
    }

	/**
	 * Retrieves sample results from queue
	 *  and records them to registry while task is run,
	 *   sets terminated status to true when stopped.
	 */
	@Override
	public void run() {
		running.set(true);
		terminated.set(false);
		while (running.get()) {
			try {
				this.registry.addResponse(this.sampleQueue.take());
			} catch (InterruptedException e) {
				LOG.warn("Sample process task interrupted");
				interrupt();
			}
		}
		terminated.set(true);
	}

}
