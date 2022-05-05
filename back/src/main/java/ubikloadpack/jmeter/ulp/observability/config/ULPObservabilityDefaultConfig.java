package ubikloadpack.jmeter.ulp.observability.config;

public class ULPObservabilityDefaultConfig {
	
	
	   public static String PLUGIN_NAME = "ULP Observability";
	   public static String METRICS_ENDPOINT_NAME = "metrics";
	   public static String TEST_METRICS_ENDPOINT_NAME = "/test";
	   public static String METRIC_DATA = "\\";
	   public static Integer JETTY_SERVER_PORT = 9090;
	   public static Integer NBR_SIGNIFICANT_DIGITS = 5;
	   
	   public static Integer PCT1 = 50;
	   public static Integer PCT2 = 90;
	   public static Integer PCT3 = 95;
	   
	   public static Double PCT1_ERROR = 0.01;
	   public static Double PCT2_ERROR = 0.005;
	   public static Double PCT3_ERROR = 0.0025;
	   
	   public static Integer LOG_FREQUENCY = 2;
	   
	   public static Boolean ENABLE_DATA_OUTPUT = false;
	   

}
