package ulbobservability;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;

public class ULBObservabilityListener implements SampleListener {

	public void sampleOccurred(SampleEvent sampleEvent) {
		
		   System.out.println("event received");

	}

	public void sampleStarted(SampleEvent sampleEvent) {

			System.out.println("event started");
	}

	public void sampleStopped(SampleEvent sampleEvent) {
	
			System.out.println("event stopped");
	}

	
}
