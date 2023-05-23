package com.ubikloadpack.jmeter.ulp.observability.util;

/**
 * ErrorTypeInfo holds the type of the occurred error and it's occurrences.
 * It defines the method like {@link #computeErrorTypeFrequency} to get the frequency 
 * of that error type. Also you can get the rate error by calling 
 * the {@link #computeErrorRateForType} method.
 */
public class ErrorTypeInfo implements Comparable<ErrorTypeInfo> {
	
	private String errorType;
	private Long occurence;
	
	public ErrorTypeInfo(String errorType) {
		this(errorType, 0L);
	}
	
	public ErrorTypeInfo(String errorType, Long occurence) {
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
	 * @param totalErrors the total errors. Should get it's value from the Micrometer registry.
	 * @return Percentage of occurrences of a specific error 
	 * type out of the total number of errors.
	 */
	public double computeErrorTypeFrequency(Long totalErrors) {
		return  totalErrors > 0 ? (double) (getOccurence() / totalErrors) : 0;
	}
	
	/**
	 * Compute error rate of that error type.
	 * @param totalRequests the total requests. Should get it's value from the Micrometer registry.
	 * @return percentage of queries that raised an error of this type.
	 */
	public double computeErrorRate(Long totalRequests) {
		return totalRequests > 0 ? (double) (getOccurence() / totalRequests) : 0;
	}

	
	public String getErrorType() {
		return errorType;
	}
	
	public Long getOccurence() {
		return occurence;
	}

	@Override
	public int compareTo(ErrorTypeInfo o) {
		// For descending order reverse the places of other and this
		return o.occurence.compareTo(this.occurence);
	}
}
