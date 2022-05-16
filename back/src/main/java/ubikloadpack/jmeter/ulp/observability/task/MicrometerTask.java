package ubikloadpack.jmeter.ulp.observability.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.ResponseResult;
import ubikloadpack.jmeter.ulp.observability.registry.MicrometerRegistry;

public class MicrometerTask implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(MicrometerTask.class);
	
	private BlockingQueue<ResponseResult> sampleQueue;
	private MicrometerRegistry registry;
	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean terminated = new AtomicBoolean(true);
	private Thread worker;
	
	public MicrometerTask(MicrometerRegistry registry, BlockingQueue<ResponseResult> sampleQueue) {
		this.registry = registry;
		this.sampleQueue = sampleQueue;
	}
	
	public void start() {
        worker = new Thread(this);
        worker.start();
    }
	

	public void stop() {
        running.set(false);
    }

    public void interrupt() {
    	worker.interrupt();
        running.set(false);
    }

    boolean isRunning() {
        return running.get();
    }


    boolean isTerminated() {
        return terminated.get();
    }

	@Override
	public void run() {
		running.set(true);
		terminated.set(false);
		while(running.get()) {
			try {
				registry.addResponse(sampleQueue.take());
			} catch (InterruptedException e) {
				log.warn("Sample process task interrupted");
				interrupt();
				
			};
		}
		terminated.set(true);
		
	}
	

}
