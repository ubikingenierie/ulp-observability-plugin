package ubikloadpack.jmeter.ulp.observability.metric;

public class ResponseResult {

	private final String sampleLabel;
	private final Long responseTime;
	private final Boolean hasError;
	private final Long endTime;
	private final Integer threads;
	
	
	public ResponseResult(String sampleLabel, Long responseTime, Boolean hasError, Long endTime, Integer threads) {
		this.sampleLabel = sampleLabel;
		this.responseTime = responseTime;
		this.hasError = hasError;
		this.endTime = endTime;
		this.threads = threads;
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

	public Integer getThreads() {
		return threads;
	}
	

}
