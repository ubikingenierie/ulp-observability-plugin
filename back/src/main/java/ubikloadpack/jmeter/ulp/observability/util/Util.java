package ubikloadpack.jmeter.ulp.observability.util;


/**
 * Set of static utility methods
 * 
 * @author Valentin ZELIONII
 *
 */
public class Util {


	    /**
	     * Standardize sample name to OpenMetrics format
	     * 
	     * @param name Name to format
	     * @return Name in OpenMetrics format
	     */
	    public static String makeOpenMetricsName(String name) {
	    	  return name.trim().toLowerCase().replace(" ","_").replaceAll("[^a-zA-Z0-9_]", "");
	    }
	    
	    
	    /**
	     * Standardize sample name to micrometer format
	     * 
	     * @param name Name to format
	     * @return Name in micrometer format
	     */
	    public static String makeMicrometerName(String name) {
	    	return name.trim().toLowerCase().replaceAll("[ _]",".").replaceAll("[^a-zA-Z0-9.]", "");
	    }
	    
	    public static String micrometerToOpenMetrics(String name) {
	    	return name.replace(".","_");
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
