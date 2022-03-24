package apache.jmeter.ulpobservability.listener;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class ULPObservabilityListener extends AbstractTestElement 
             implements SampleListener, TestStateListener, Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(ULPObservabilityListener.class);


	public void sampleOccurred(SampleEvent sampleEvent) {
		
		    log.info("event received");
		  
	}

	public void sampleStarted(SampleEvent sampleEvent) {

		    log.info("event started");
			
	}

	
	public void sampleStopped(SampleEvent sampleEvent) {
	
		    log.info("event stopped");
	}

	public void testStarted() {
		
		
	}

	public void testStarted(String host) {
		
		
	}

	public void testEnded() {
		
		
	}

	public void testEnded(String host) {
		
		
	}

	
}
