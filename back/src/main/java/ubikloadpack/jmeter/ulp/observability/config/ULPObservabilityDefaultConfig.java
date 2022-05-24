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
	public static final String JETTY_METRICS_ROUTE_PROP = "ULPObservability.JettyMetricsRoute";
	public static final String JETTY_WEBAPP_ROUTE_PROP = "ULPObservability.JettyWebAppRoute";
	public static final String THREAD_SIZE_PROP = "ULPObservability.ThreadSize";
	public static final String BUFFER_CAPACITY_PROP = "ULPObservability.SampleQueueBufferCapacity";
	public static final String PCT1_PROP = "ULPObservability.Pct1";
	public static final String PCT2_PROP = "ULPObservability.Pct2";
	public static final String PCT3_PROP = "ULPObservability.Pct3";
	public static final String PCT_PRECISION_PROP = "ULPObservability.PctPrecision";
	public static final String LOG_FREQUENCY_PROP = "ULPObservability.LogFrequency";
	public static final String METRICS_DATA_PROP = "ULPObservability.MetricsData";
	public static final String ENABLE_DATA_OUTPUT_PROP = "ULPObservability.EnableDataOutput";
	public static final String TOTAL_LABEL_PROP = "ULPObservability.TotalLabel";

	
	/**
	 * Default plugin name
	 */
	public static final String PLUGIN_NAME = "ULP Observability";
	
	/**
	 * Default Jetty server port
	 */
	public static final Integer JETTY_SERVER_PORT = 9090;
	
	/**
	 * Default route used to expose sample metrics in OpenMetrics text format
	 */
	public static final String JETTY_METRICS_ROUTE = "/ulp-o-metrics";
	
	/**
	 * Default route used to expose Angular web app page
	 */
	public static final String JETTY_WEBAPP_ROUTE = "/ulp-observability";
	
	/**
	 * Default route reserved for eventual tests
	 */
	public static final String TEST_METRICS_ROUTE_TEST = "/ulp-o-metrics-test";

	
	/**
	 * Default number of sample registry task threads
	 */
	public static final Integer THREAD_SIZE = 15;
	
	/**
	 * Default sample result queue capacity
	 */
	public static final Integer BUFFER_CAPACITY = 5000;
	
	/**
	 * Default 1st percentile score
	 */
	public static final Integer PCT1 = 50;
	
	/**
	 * Default 2nd percentile score
	 */
	public static final Integer PCT2 = 90;
	
	/**
	 * Default 3rd percentile score
	 */
	public static final Integer PCT3 = 95;
	
	/**
	 * Default number of significant decimal digits for percentiles
	 */
	public static final Integer PCT_PRECISION = 5;
	
	/**
	 * Default logging frequency in seconds
	 */
	public static final Integer LOG_FREQUENCY = 60;
	
	/**
	 * Default path to log output folder
	 */
	public static final String METRICS_DATA = "\\";
	
	/**
	 * Default option to enable log output
	 */
	public static final Boolean ENABLE_DATA_OUTPUT = false;
	
	/**
	 * Default OpenMetrics name to denote total metrics
	 */
	public static final String TOTAL_LABEL = "total_info";
	
	public static Double PCT1_ERROR = 0.01;
	public static Double PCT2_ERROR = 0.005;
	public static Double PCT3_ERROR = 0.0025;
	
	public static String pluginName() {
		return PLUGIN_NAME;
	}
	
	public static String totalLabel() {
		return JMeterUtils.getPropDefault(TOTAL_LABEL_PROP, TOTAL_LABEL);
	}
	
	public static Integer jettyServerPort() {
		return JMeterUtils.getPropDefault(JETTY_SERVER_PORT_PROP, JETTY_SERVER_PORT);
	}
	
	public static Integer bufferCapacity() {
		return JMeterUtils.getPropDefault(BUFFER_CAPACITY_PROP, BUFFER_CAPACITY);
	}
	
	public static String jettyMetricsRoute() {
		return JMeterUtils.getPropDefault(JETTY_METRICS_ROUTE_PROP, JETTY_METRICS_ROUTE);
	}
	
	public static String jettyWebAppRoute() {
		return JMeterUtils.getPropDefault(JETTY_WEBAPP_ROUTE_PROP, JETTY_WEBAPP_ROUTE);
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
