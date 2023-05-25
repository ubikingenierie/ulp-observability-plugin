package com.ubikloadpack.jmeter.ulp.observability.util;

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

	@Override
	public int compareTo(ErrorTypeInfo o) {
		// For descending order reverse the places of other and this
		return Long.compare(o.occurence, this.occurence);
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
