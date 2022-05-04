package ubikloadpack.jmeter.ulp.observability.util;

import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ubikloadpack.jmeter.ulp.observability.metric.ULPObservabilitySampleRegistry;

public class SampleProcessThread extends Thread{
	
	private static final Logger log = LoggerFactory.getLogger(SampleProcessThread.class);
	private ULPObservabilitySampleRegistry sampleRegistry;
	private SampleResult sampleResult;
	
	public SampleProcessThread(ULPObservabilitySampleRegistry sampleRegistry, SampleResult sampleResult) {
		this.sampleRegistry = sampleRegistry;
		this.sampleResult = sampleResult;
	}
	
	public void run() {
			this.sampleRegistry.processSample(sampleResult);
	}

}
