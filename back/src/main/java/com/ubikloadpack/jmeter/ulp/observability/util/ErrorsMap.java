package com.ubikloadpack.jmeter.ulp.observability.util;

import java.util.Map;
import java.util.OptionalDouble;
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
	 * Add an error type and increment it's occurrence. If the error type already exists
	 * so it's occurrence will be incremented.
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
	 * @return an optional that contains the percentage of occurrences of a specific error type 
	 * out of the total number of errors. Returns an empty optional if no errorType key was found.
	 */
	public OptionalDouble computeErrorTypeFrequency(String errorType, Long errorsTotal) {
		if (this.errorsPerType.contains(errorType)) {
			return OptionalDouble.of(errorsPerType.get(errorType).computeErrorRateAmongErrors(errorsTotal));
		}
		return OptionalDouble.empty();
	}
	
	/**
	 * Compute error rate of that error type.
	 * @param errorType the type of error for which the error rate will be calculated.
	 * @param requestsTotal the total requests. Should get it's value from the Micrometer registry.
	 * @return an optional that contains the percentage of queries that raised an error of this type. 
	 * Returns an empty optional if no errorType key was found.
	 */
	public OptionalDouble computeErrorRateForType(String errorType, Long requestsTotal) {
		if (this.errorsPerType.contains(errorType)) {
			return OptionalDouble.of(errorsPerType.get(errorType).computeErrorRateAmongRequests(requestsTotal));
		}
		return OptionalDouble.empty();
	}
	
	/**
	 * Build an ErrorsMap that contains the X top Errors.
	 * @param maxErrors The maximum type errors to keep.
	 * @return a new ErrorsMap with only the top X Errors.
	 */
	public ErrorsMap collectTopXErrors(int maxErrors) {
	    Map<String, ErrorTypeInfo> topErrors = this.errorsPerType.entrySet()
										           .stream()
										           .sorted(Map.Entry.<String, ErrorTypeInfo>comparingByValue().reversed())
										           .limit(maxErrors)
										           // we should create new references for ErrorTypeInfo so that the threads will not affect the ErrorTypeInfo stored in the new Map
										           .collect(Collectors.toMap(Map.Entry::getKey, e -> new ErrorTypeInfo(e.getValue().getErrorType(), e.getValue().getOccurence())));

	    return new ErrorsMap(new ConcurrentHashMap<>(topErrors));
	}
	
	/**
	 * Get the openMetric format of the error types.
	 * @param sampleName the name of the sample (should be total_label)
	 * @param requestsTotal the number of the total threads
	 * @param errorsTotal
	 * @param timeStamp 
	 * @return
	 */
	public String toOpenMetric(String sampleName, Long requestsTotal, Long errorsTotal, Long timeStamp) {
		StringBuilder str = new StringBuilder();
		
		this.errorsPerType.forEach((errorType, errorInfo) -> {
			Double errorFrequency = errorInfo.computeErrorRateAmongErrors(errorsTotal);
			Double errorRate = errorInfo.computeErrorRateAmongRequests(requestsTotal);
			
			String metric = String.format("%s_total{count=\"error_every_periods\",errorType=\"%s\",errorRate=\"%s\",errorFreq=\"%s\"}", 
										  sampleName, errorType, errorRate, errorFrequency);
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


	@Override
	public String toString() {
		return "ErrorsMap {" + errorsPerType + "}";
	}
	
	
}
