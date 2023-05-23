package com.ubikloadpack.jmeter.ulp.observability.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The ErrorsMap stores for each error type it's occurrences.
 * The occurrences of an error type is handled by {@link ErrorTypeInfo}
 */ 
public class ErrorsMap {
	private ConcurrentHashMap<String, ErrorTypeInfo> errorsPerType;
	
	public ErrorsMap() {
		this(new ConcurrentHashMap<>());
	}


	public ErrorsMap(ConcurrentHashMap<String, ErrorTypeInfo> errorsPerType) {
		super();
		this.errorsPerType = errorsPerType;
	}



	/**
	 * Add an error type and increment it's occurrence.
	 * @param errorType the error type to add.
	 * @return The information of the added error type.
	 */
	public ErrorTypeInfo addErrorTypeAndCount(String errorType) {
		ErrorTypeInfo errorTypeInfo = this.errorsPerType.computeIfAbsent(errorType, s -> new ErrorTypeInfo(errorType));
	    errorTypeInfo.increment();
	    return errorTypeInfo;
	}
	
	/**
	 * Compute the frequency of that error type.
	 * @param errorType the type of error for which the frequency will be calculated
	 * @param errorsTotal the total errors. Should get it's value from the Micrometer registry.
	 * @return Percentage of occurrences of a specific error type out of the total number of errors.
	 * Returns null if no errorType key was found.
	 */
	public Double computeErrorTypeFrequency(String errorType, Long errorsTotal) {
		if (this.errorsPerType.contains(errorType)) {
			return errorsPerType.get(errorType).computeErrorRateAmongErrors(errorsTotal);
		}
		return null;
	}
	
	/**
	 * Compute error rate of that error type.
	 * @param errorType the type of error for which the error rate will be calculated.
	 * @param requestsTotal the total requests. Should get it's value from the Micrometer registry.
	 * @return percentage of queries that raised an error of this type. Returns null if no errorType 
	 * key was found.
	 */
	public Double computeErrorRateForType(String errorType, Long requestsTotal) {
		if (this.errorsPerType.contains(errorType)) {
			return errorsPerType.get(errorType).computeErrorRateAmongRequests(requestsTotal);
		}
		return null;
	}
	
	/**
	 * Build an ErrorsMap that contains the X top Errors.
	 * @param maxErrors The maximum type errors to keep.
	 * @return a new ErrorMap with only the top X Errors.
	 */
	public ErrorsMap collectTopXErrors(int maxErrors) {
	    Map<String, ErrorTypeInfo> topErrors = this.errorsPerType.entrySet()
										           .stream()
										           .sorted(Map.Entry.<String, ErrorTypeInfo>comparingByValue().reversed())
										           .limit(maxErrors)
										           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	    return new ErrorsMap(new ConcurrentHashMap<>(topErrors));
	}
	
	/**
	 * Get the openMetric format of the error types.
	 * @param sampleName the name of the sample (should be total_label)
	 * @param totalThreads the number of the total threads
	 * @param totalErrors
	 * @param timeStamp 
	 * @return
	 */
	public String toOpenMetric(String sampleName, Long totalThreads, Long totalErrors, Long timeStamp) {
		StringBuilder str = new StringBuilder();
		
		this.errorsPerType.forEach((errorType, errorInfo) -> {
			Double errorFrequency = errorInfo.computeErrorRateAmongErrors(totalErrors);
			Double errorRate = errorInfo.computeErrorRateAmongRequests(totalThreads);
			
			String metric = String.format("%s_total{count=\"error_every_periods\",errorType=\"%s\",errorRate=\"%s\",freq=\"%s\"}", sampleName, errorType, errorRate, errorFrequency);
			str.append(metric + " " + errorInfo.getOccurence() + " " + timeStamp +"\n");
		});
		return str.toString();
	}

	/**
	 * @return the error map
	 */
	public ConcurrentHashMap<String, ErrorTypeInfo> getErrorPerType() {
		return errorsPerType;
	}
}
