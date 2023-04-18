package com.ubikloadpack.jmeter.ulp.observability.metric;

/**
 * Represents response data received from sampler 
 * 
 * @author Valentin ZELIONII
 *
 */
public class ResponseResult {

	
	/**
	 * Name of thread group the sample belongs to
	 */
	private final String threadGroupLabel;
	/**
	 * Occurred sample's response time 
	 */
	private final Long responseTime;
	/**
	 * True if sample response is KO
	 */
	private final boolean hasError;
	/**
	 * Group thread count
	 */
	private final Integer groupThreads;
	
	/**
	 * Total thread count
	 */
	private final Integer allThreads;
	
	/**
	 * Name of the sampler the sample belongs to
	 */
	private final String samplerLabel;
	
	
    /**
     * Creates new Response result
     * 
     * @param threadGroupLabel Name of thread group the sample belongs to
     * @param responseTime Occurred sample's response time 
     * @param hasError True if sample response is KO
     * @param groupThreads Group thread count
     * @param allThreads Total thread count
     * @param samplerLabel Name of the sampler the sample belongs to
     */
	public ResponseResult(String threadGroupLabel, Long responseTime, boolean hasError, Integer groupThreads, Integer allThreads, String samplerLabel) {
		this.threadGroupLabel = threadGroupLabel;
		this.responseTime = responseTime;
		this.hasError = hasError;
		this.groupThreads = groupThreads;
		this.allThreads = allThreads;
		this.samplerLabel = samplerLabel;
	}
	
	public String getThreadGroupLabel() {
		return threadGroupLabel;
	}


	public Long getResponseTime() {
		return responseTime;
	}


	public Boolean hasError() {
		return hasError;
	}

	public Integer getGroupThreads() {
		return groupThreads;
	}
	
	public Integer getAllThreads() {
		return allThreads;
	}
	
	public String getSamplerLabel() {
		return samplerLabel;
	}

}
