package com.ubikloadpack.jmeter.ulp.observability.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.utils.MetricUtils;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Set of static utility methods
 * 
 * @author Valentin ZELIONII
 *
 */
public class Util {
	
	private static final Pattern MATCH_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
	private static final Pattern DELIMITER_PATTERN = Pattern.compile("[.]");
	private static final Set<String> DELIMITERS = new HashSet<>(Arrays.asList("_",".", "-", " "));	
	
	/** 
	 * Convert valid delimiter to OpenMetrics delimiter
	 * 
	 * @param match Match result
	 * @return OpenMetrics delimiter if valid delimiter, else an empty string
	 */
	private static String openMetricsDelimeter(String match) {
		return DELIMITERS.contains(match) ? "_" : "";
	}
	
	/** 
	 * Convert valid delimiter to micrometer delimiter
	 * 
	 * @param match Match result
	 * @return Micrometer delimiter if valid delimiter, else an empty string
	 */
	private static String micrometerDelimeter(String match) {
		return DELIMITERS.contains(match) ? "." : "";
	}
    
    /**
     * Standardize sample name to OpenMetrics format
     * 
     * @param name Name to format
     * @return Name in OpenMetrics format
     */
	public static String makeOpenMetricsName(String name) {
    	return MATCH_PATTERN.matcher(name.trim().toLowerCase()).replaceAll(match -> openMetricsDelimeter(match.group()));
    }
	    
	    
    /**
     * Standardize sample name to micrometer format
     * 
     * @param name Name to format
     * @return Name in micrometer format
     */
    public static String makeMicrometerName(String name) {
    	return MATCH_PATTERN.matcher(name.trim().toLowerCase()).replaceAll(match -> micrometerDelimeter(match.group()));
    }
    
    /**
     * Get sample response time
     * 
     * @param endTime End response time stamp
     * @param startTime Start response time stamp
     * @return Total response time
     */
    public static long getResponseTime(long endTime, long startTime) {
    	  return endTime-startTime;
    }
    
    
    /**
     * Get the key error from the response code. The returned key error is by 
     * default the response code. If the response code is a success code or if the response
     * code is empty but the the failure message exists, so the key error
     * will be marked as {@link MetricUtils#ASSERTION_FAILED ASSERTION_FAILED}. 
     * 
     * @param responseCode the response code of a sample.
     * @param failureMessage the failure message of a sample.
     * @return The key error from the response code.
     */
    public static String getErrorKey(String responseCode, String failureMessage) {
         String key = responseCode;
         
         // 
         if (MetricUtils.isSuccessCode(responseCode) ||
                 (StringUtils.isEmpty(responseCode) && StringUtils.isNotBlank(failureMessage))) {
             key = MetricUtils.ASSERTION_FAILED;
         }
         return key;
    }
	    
}
