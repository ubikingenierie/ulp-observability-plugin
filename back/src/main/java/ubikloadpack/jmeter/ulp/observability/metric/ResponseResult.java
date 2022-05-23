package ubikloadpack.jmeter.ulp.observability.metric;

public class ResponseResult {

	private final String sampleLabel;
	private final Long responseTime;
	private final Boolean hasError;
	private final Long endTime;
	private final Integer groupThreads;
	private final Integer allThreads;
	
	
	public ResponseResult(String sampleLabel, Long responseTime, Boolean hasError, Long endTime, Integer groupThreads, Integer allThreads) {
		this.sampleLabel = sampleLabel;
		this.responseTime = responseTime;
		this.hasError = hasError;
		this.endTime = endTime;
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
	
	public Long getEndTime() {
		return endTime;
	}

	public Integer getGroupThreads() {
		return groupThreads;
	}
	
	public Integer getAllThreads() {
		return allThreads;
	}
	

}
