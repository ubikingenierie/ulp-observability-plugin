package com.ubikloadpack.jmeter.ulp.observability.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ubikloadpack.jmeter.ulp.observability.server.AbstractConfigTest;

public class ULPObservabilityListenerTest extends AbstractConfigTest {	

	@Test
	@DisplayName("When a sample event occurs and the current thread is interrupted, expect the sample result is added to the queue")
	void whenASampleEventOccursAndTheCurrentThreadIsInterruptedExpectTheSampleResultIsAddedToTheQueue() throws InterruptedException {		
	    SampleResult sampleResult = mock(SampleResult.class);
	    when(sampleResult.getSampleLabel()).thenReturn("sampleTest");

	    SampleEvent sampleEvent = mock(SampleEvent.class);
	    when(sampleEvent.getThreadGroup()).thenReturn("group1");
	    when(sampleEvent.getResult()).thenReturn(sampleResult);

	    assertEquals(0, listener.getSampleQueue().size()); // assert sample is empty before adding a sample

        // Interrupt the current thread to simulate an interruption
        Thread.currentThread().interrupt();

        // Invoke sampleOccured
        listener.sampleOccurred(sampleEvent);

        // Check that the sample has been added to the queue despite thread interruption
	    assertEquals(1, listener.getSampleQueue().size());     

	    Thread.interrupted();
	}

}
