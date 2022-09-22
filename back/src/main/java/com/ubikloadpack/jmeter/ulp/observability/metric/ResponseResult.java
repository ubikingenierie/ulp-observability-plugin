package ubikloadpack.jmeter.ulp.observability.metric;

/**
 * Represents response data received from sampler 
 * 
 * @author Valentin ZELIONII
 *
 */
public class ResponseResult {

	
	/**
	 * Name of thread group sample belongs to
	 */
	private final String sampleLabel;
	/**
	 * Occurred sample's response time 
	 */
	private final Long responseTime;
	/**
	 * True if sample response is KO
	 */
	private final Boolean hasError;
	/**
	 * Group thread count
	 */
	private final Integer groupThreads;
	
	/**
	 * Total thread count
	 */
	private final Integer allThreads;
	
	
	public ResponseResult(String sampleLabel, Long responseTime, Boolean hasError, Integer groupThreads, Integer allThreads) {
		this.sampleLabel = sampleLabel;
		this.responseTime = responseTime;
		this.hasError = hasError;
		this.groupThreads = groupThreads;
		this.allThreads = allThreads;
	}
	
	public String getSampleLabel() {
		return sampleLabel;
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
	

}
