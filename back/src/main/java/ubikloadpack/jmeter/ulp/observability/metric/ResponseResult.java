package ubikloadpack.jmeter.ulp.observability.metric;

public class ResponseResult {
	
	private final String sampleLabel;
	private final Long responseTime;
	private final Boolean hasError;
	private final Long endTime;
	
	
	public ResponseResult(String sampleLabel, Long responseTime, Boolean hasError, Long endTime) {
		super();
		this.sampleLabel = sampleLabel;
		this.responseTime = responseTime;
		this.hasError = hasError;
		this.endTime = endTime;
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

	
	

}
