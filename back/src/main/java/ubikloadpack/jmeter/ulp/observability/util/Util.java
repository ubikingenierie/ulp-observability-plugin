package ubikloadpack.jmeter.ulp.observability.util;

public class Util {

	
	    public static String makeMetricName(String name) {
	    	  return name.toLowerCase().replace(" ", "_");
	    }
	    
	    public static double getResponseTime(long endTime, long startTime) {
	    	  return endTime-startTime;
	    }
	    
}
