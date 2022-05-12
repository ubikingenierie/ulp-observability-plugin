package ubikloadpack.jmeter.ulp.observability.config;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Configuration class containing all sampler properties and property labels
 * 
 * @author Valentin ZELIONII
 *
 */
public class ULPObservabilityDefaultConfig {
	
	public static final String JETTY_SERVER_PORT_PROP = "ULPObservability.JettyPort";
	public static final String JETTY_METRICS_ENDPOINT_PROP = "ULPObservability.JettyMetricsEndpoint";
	public static final String THREAD_SIZE_PROP = "ULPObservability.ThreadSize";
	public static final String BUFFER_CAPACITY_PROP = "ULPObservability.SampleQueueBufferCapacity";
	public static final String PCT1_PROP = "ULPObservability.Pct1";
	public static final String PCT2_PROP = "ULPObservability.Pct2";
	public static final String PCT3_PROP = "ULPObservability.Pct3";
	public static final String PCT_PRECISION_PROP = "ULPObservability.PctPrecision";
	public static final String LOG_FREQUENCY_PROP = "ULPObservability.LogFrequency";
	public static final String METRICS_DATA_PROP = "ULPObservability.MetricsData";
	public static final String ENABLE_DATA_OUTPUT_PROP = "ULPObservability.EnableDataOutput";

	
	/**
	 * Default plugin name
	 */
	public static String PLUGIN_NAME = "ULP Observability";
	
	/**
	 * Default Jetty server port
	 */
	public static Integer JETTY_SERVER_PORT = 9090;
	
	/**
	 * Default endpoint used to expose sample metrics in OpenMetrics text format
	 */
	public static String JETTY_METRICS_ENDPOINT = "/ulp-o-metrics";
	
	/**
	 * Default endpoint reserved for eventual tests
	 */
	public static String TEST_METRICS_ENDPOINT_TEST = "/ulp-o-metrics-test";
	
	/**
	 * Default sample processing thread size
	 */
	public static Integer THREAD_SIZE = 5;
	
	/**
	 * Default sample result queue capacity
	 */
	public static Integer BUFFER_CAPACITY = 20000;
	
	/**
	 * Default 1st percentile score
	 */
	public static Integer PCT1 = 50;
	
	/**
	 * Default 2nd percentile score
	 */
	public static Integer PCT2 = 90;
	
	/**
	 * Default 3rd percentile score
	 */
	public static Integer PCT3 = 95;
	
	/**
	 * Default number of significant decimal digits for percentiles
	 */
	public static Integer PCT_PRECISION = 5;
	
	/**
	 * Default logging frequency in seconds
	 */
	public static Integer LOG_FREQUENCY = 60;
	
	/**
	 * Default path to log output folder
	 */
	public static String METRICS_DATA = "\\";
	
	/**
	 * Default option to enable log output
	 */
	public static Boolean ENABLE_DATA_OUTPUT = false;
	
	public static Double PCT1_ERROR = 0.01;
	public static Double PCT2_ERROR = 0.005;
	public static Double PCT3_ERROR = 0.0025;
	
	public static String pluginName() {
		return PLUGIN_NAME;
	}
	
	public static Integer jettyServerPort() {
		return JMeterUtils.getPropDefault(JETTY_SERVER_PORT_PROP, JETTY_SERVER_PORT);
	}
	
	public static Integer bufferCapacity() {
		return JMeterUtils.getPropDefault(BUFFER_CAPACITY_PROP, BUFFER_CAPACITY);
	}
	
	public static String jettyMetricsEndpoint() {
		return JMeterUtils.getPropDefault(JETTY_METRICS_ENDPOINT_PROP, JETTY_METRICS_ENDPOINT);
	}
	
	public static Integer threadSize() {
		return JMeterUtils.getPropDefault(THREAD_SIZE_PROP, THREAD_SIZE);
	}
	
	public static Integer pct1() {
		return JMeterUtils.getPropDefault(PCT1_PROP, PCT1);
	}
	
	public static Integer pct2() {
		return JMeterUtils.getPropDefault(PCT2_PROP, PCT2);
	}
	
	public static Integer pct3() {
		return JMeterUtils.getPropDefault(PCT3_PROP, PCT3);
	}
	
	public static Integer pctPrecision() {
		return JMeterUtils.getPropDefault(PCT_PRECISION_PROP, PCT_PRECISION);
	}
	
	public static Integer logFrequecny() {
		return JMeterUtils.getPropDefault(LOG_FREQUENCY_PROP, LOG_FREQUENCY);
	}
	
	public static String metricsData() {
		return JMeterUtils.getPropDefault(METRICS_DATA_PROP, METRICS_DATA);
	}
	
	public static Boolean enableDataOutput() {
		return JMeterUtils.getPropDefault(ENABLE_DATA_OUTPUT_PROP, ENABLE_DATA_OUTPUT);
	}

}
