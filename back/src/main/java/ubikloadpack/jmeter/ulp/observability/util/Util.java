package ubikloadpack.jmeter.ulp.observability.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Set of static utility methods
 * 
 * @author Valentin ZELIONII
 *
 */
public class Util {
	
	private static final Pattern MATCH_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
	private static final Pattern DELIMITER_PATTERN = Pattern.compile("[.]");
	private static Set<String> delimiters = new HashSet<>(Arrays.asList("_",".", "-", " "));
	
	
	/** 
	 * Convert valid delimiter to OpenMetrics delimiter
	 * 
	 * @param match Match result
	 * @return OpenMetrics delimiter if valid delimiter, else an empty string
	 */
	private static String openMetricsDelimeter(String match) {
		return delimiters.contains(match) ? "_" : "";
	}
	
	/** 
	 * Convert valid delimiter to micrometer delimiter
	 * 
	 * @param match Match result
	 * @return Micrometer delimiter if valid delimiter, else an empty string
	 */
	private static String micrometerDelimeter(String match) {
		return delimiters.contains(match) ? "." : "";
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
     * Convert name from micrometer to OpenMetrics format
     * 
     * @param name Name to format
     * @return Name in OpenMetrics format
     */
    public static String micrometerToOpenMetrics(String name) {
    	return DELIMITER_PATTERN.matcher(name).replaceAll("_");
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
	    
}
