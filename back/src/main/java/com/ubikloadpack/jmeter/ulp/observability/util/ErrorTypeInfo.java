package com.ubikloadpack.jmeter.ulp.observability.util;

import java.util.Objects;

/**
 * ErrorTypeInfo holds the type of an error and it's occurrences.
 * It defines the method like {@link #computeErrorTypeFrequency} to get the frequency 
 * of that error type. Also you can get the rate error by calling 
 * the {@link #computeErrorRate} method.
 */
public class ErrorTypeInfo implements Comparable<ErrorTypeInfo> {
	
	private String errorType;
	private long occurence;
	
	public ErrorTypeInfo(String errorType) {
		this(errorType, 0L);
	}
	
	public ErrorTypeInfo(String errorType, long occurence) {
		super();
		this.errorType = errorType;
		this.occurence = occurence;
	}

	/**
	 * Increment by one the occurrence of this error type.
	 */
	public void increment() {
		this.occurence++;
	}
	
	/**
	 * Compute the frequency of that error type.
	 * @param errorsTotal the total errors. Should get it's value from the Micrometer registry.
	 * @return Percentage of occurrences of a specific error 
	 * type out of the total number of errors.
	 */
	public double computeErrorRateAmongErrors(Long errorsTotal) {
		return  errorsTotal > 0 ? (double) getOccurence() / (double) errorsTotal : 0;
	}
	
	/**
	 * Compute error rate of that error type.
	 * @param requestsTotal the total requests. Should get it's value from the Micrometer registry.
	 * @return percentage of queries that raised an error of this type.
	 */
	public double computeErrorRateAmongRequests(Long requestsTotal) {
		return requestsTotal > 0 ? (double) getOccurence() / (double) requestsTotal : 0;
	}
	
	/**
	 * Get the openMetric format of the error types.
	 * @param sampleName the name of the sample (should be total_label)
	 * @param requestsTotal the number of the total threads
	 * @param errorsTotal the number of total requests
	 * @param timeStamp the time stamp 
	 * @return a string that represents the openMetrics format of that error type
	 */
	public String toOpenMetric(String sampleName, Long requestsTotal, Long errorsTotal, Long timeStamp) {
		StringBuilder str = new StringBuilder();
		
		Double errorFrequency = this.computeErrorRateAmongErrors(errorsTotal);
		Double errorRate = this.computeErrorRateAmongRequests(requestsTotal);
		
		String metric = String.format("%s_total{count=\"error_every_periods\",errorType=\"%s\",errorRate=\"%s\",errorFreq=\"%s\"}", 
									  sampleName, errorType, errorRate, errorFrequency);
		str.append(metric + " " + this.getOccurence() + " " + timeStamp +"\n");
		return str.toString();
	}

	@Override
	public int compareTo(ErrorTypeInfo o) {
		// For descending order reverse the places of other and this
		return Long.compare(o.occurence, this.occurence);
	}
		
	@Override
	public int hashCode() {
		return Objects.hash(errorType, occurence);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorTypeInfo other = (ErrorTypeInfo) obj;
		return Objects.equals(errorType, other.errorType) && occurence == other.occurence;
	}

	public String getErrorType() {
		return errorType;
	}
	
	public Long getOccurence() {
		return occurence;
	}

	@Override
	public String toString() {
		return "ErrorTypeInfo {errorType=" + errorType + ", occurence=" + occurence + "}";
	}
}
