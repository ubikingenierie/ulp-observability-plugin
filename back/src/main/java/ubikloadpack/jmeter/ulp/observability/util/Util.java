package ubikloadpack.jmeter.ulp.observability.util;


/**
 * Set of static utility functions
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
	    public static String makeMetricName(String name) {
	    	  return name.trim().toLowerCase().replaceAll(" ","_").replaceAll("[^a-zA-Z0-9_]", "");
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
