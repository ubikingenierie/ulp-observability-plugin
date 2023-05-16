package com.ubikloadpack.jmeter.ulp.observability.config;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Configuration class containing all sampler properties and property labels.
 * @author Valentin ZELIONII
 *
 */
public class ULPODefaultConfig {

	/**
	 * JMeter property key for Jetty server port.
	 */
	public static final String JETTY_SERVER_PORT_PROP =
			"ULPObservability.JettyPort";
	/**
	 * JMeter property key for Jetty metrics route.
	 */
	public static final String JETTY_METRICS_ROUTE_PROP =
			"ULPObservability.JettyMetricsRoute";
	/**
	 * JMeter property key for Jetty webapp route.
	 */
	public static final String JETTY_WEBAPP_ROUTE_PROP =
			"ULPObservability.JettyWebAppRoute";
	/**
	 * JMeter property key for the number of sample registry tasks.
	 */
	public static final String THREAD_SIZE_PROP =
			"ULPObservability.ThreadSize";
	/**
	 * JMeter property key for sample queue size.
	 */
	public static final String BUFFER_CAPACITY_PROP =
			"ULPObservability.SampleQueueBufferCapacity";
	/**
	 * JMeter property key for first percentile score.
	 */
	public static final String PCT1_PROP =
			"ULPObservability.Pct1";
	/**
	 * JMeter property key for second percentile score.
	 */
	public static final String PCT2_PROP =
			"ULPObservability.Pct2";
	/**
	 * JMeter property key for third percentile score.
	 */
	public static final String PCT3_PROP =
			"ULPObservability.Pct3";
	/**
	 * JMeter property key for log frequency.
	 */
	public static final String LOG_FREQUENCY_PROP =
			"ULPObservability.LogFrequency";
	
	/**
	 * JMeter property key for top errors.
	 */
	public static final String TOP_ERRORS_PROP =
			"ULPObservability.TopErrors";
	
	/**
	 * JMeter property key for total label.
	 */
	public static final String TOTAL_LABEL_PROP =
			"ULPObservability.TotalLabel";
	
	public static final String REGEX_PROP =
			"ULPObservability.RegexProp";

	public static final String KEEP_JETTY_SERVER_UP_AFTER_TEST_END_PROP =
			"ULPObservability.KeepJettyServerUpAfterTestEnd";
	
	public static final String MICROMETER_EXPIRY_TIME_IN_SECONDS_PROP =
			"ULPObservability.MicrometerExpiryTimeInSeconds";
	/**
	 * Default plugin name.
	 */
	private static final String PLUGIN_NAME = "UbikLoadPack Observability Plugin";

	/**
	 * Default Jetty server port.
	 */
	private static final Integer JETTY_SERVER_PORT = 9090;

	/**
	 * Default route used to expose
	 *  sample metrics in OpenMetrics text format.
	 */
	private static final String JETTY_METRICS_ROUTE = "/ulp-o-metrics";

	/**
	 * Default route used to expose Angular web app page.
	 */
	private static final String JETTY_WEBAPP_ROUTE = "/ulp-observability";

	/**
	 * Default number of sample registry task threads.
	 */
	private static final Integer THREAD_SIZE = 15;

	/**
	 * Default sample result queue capacity.
	 */
	private static final Integer BUFFER_CAPACITY = 5000;

	/**
	 * Default 1st percentile score.
	 */
	private static final Integer PCT1 = 50;

	/**
	 * Default 2nd percentile score.
	 */
	private static final Integer PCT2 = 90;

	/**
	 * Default 3rd percentile score.
	 */
	private static final Integer PCT3 = 95;

	/**
	 * Default logging frequency in seconds.
	 */
	private static final Integer LOG_FREQUENCY = 30;
	
	/**
	 * Default number of the top errors
	 */
	private static final Integer TOP_ERRORS = 5;

	/**
	 * Default OpenMetrics name to denote total metrics.
	 */
	private static final String TOTAL_LABEL = "total_info";
	
	/**
	 * Default regex to filter samplers by their names.
	 */
	private static final String REGEX = null;

	/**
	 * Default Checkbox value for the jetty server
	 */
	public static final Boolean KEEP_JETTY_SERVER_UP_AFTER_TEST_END = false;
	
	/**
	 * Default OpenMetrics name to denote total metrics.
	 */
	public static final Integer MICROMETER_EXPIRY_TIME_IN_SECONDS = 3600;
	
	/**
	 * Get default ULP Observability plugin name.
	 * @return Default plugin name
	 */
	public static String pluginName() {
		return PLUGIN_NAME;
	}

	/**
	 * Get default total label value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default total label
	 */
	public static String totalLabel() {
		return JMeterUtils
				.getPropDefault(TOTAL_LABEL_PROP, TOTAL_LABEL);
	}
	
	public static Integer micrometerExpiryTimeInSeconds() {
		return JMeterUtils
				.getPropDefault(MICROMETER_EXPIRY_TIME_IN_SECONDS_PROP, MICROMETER_EXPIRY_TIME_IN_SECONDS);
	}
	
	/**
	 * Get default regex value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default null
	 */
	public static String regex() {
		return JMeterUtils
				.getPropDefault(REGEX_PROP, REGEX);
	}

	/**
	 * Get default Jetty server port value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default Jetty server port
	 */
	public static Integer jettyServerPort() {
		return JMeterUtils
				.getPropDefault(JETTY_SERVER_PORT_PROP, JETTY_SERVER_PORT);
	}

	/**
	 * Get default sample result queue capacity value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default sample result queue capacity
	 */
	public static Integer bufferCapacity() {
		return JMeterUtils
				.getPropDefault(BUFFER_CAPACITY_PROP, BUFFER_CAPACITY);
	}

	/**
	 * Get default Jetty metrics route value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default Jetty metrics route
	 */
	public static String jettyMetricsRoute() {
		return JMeterUtils
				.getPropDefault(JETTY_METRICS_ROUTE_PROP, JETTY_METRICS_ROUTE);
	}

	/**
	 * Get default Jetty web app route value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default Jetty web app route
	 */
	public static String jettyWebAppRoute() {
		return JMeterUtils
				.getPropDefault(JETTY_WEBAPP_ROUTE_PROP, JETTY_WEBAPP_ROUTE);
	}

	/**
	 * Get default sample registry task threads value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default sample registry task threads
	 */
	public static Integer threadSize() {
		return JMeterUtils
				.getPropDefault(THREAD_SIZE_PROP, THREAD_SIZE);
	}

	/**
	 * Get default first percentile score value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default first percentile score
	 */
	public static Integer pct1() {
		return JMeterUtils.getPropDefault(PCT1_PROP, PCT1);
	}

	/**
	 * Get default second percentile score value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default second percentile score
	 */
	public static Integer pct2() {
		return JMeterUtils.getPropDefault(PCT2_PROP, PCT2);
	}

	/**
	 * Get default third percentile score value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default third percentile score
	 */
	public static Integer pct3() {
		return JMeterUtils.getPropDefault(PCT3_PROP, PCT3);
	}

	/**
	 * Get default log frequency value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default log frequency
	 */
	public static Integer logFrequency() {
		return JMeterUtils
				.getPropDefault(LOG_FREQUENCY_PROP, LOG_FREQUENCY);
	}
	
	/**
	 * Get default top errors value from JMeter properties file;
	 *  if it not exists, retrieve hardcoded default value instead.
	 * @return Default top errors
	 */
	public static Integer topErrors() {
		return JMeterUtils
				.getPropDefault(TOP_ERRORS_PROP, TOP_ERRORS);
	}

	public static Boolean keepJettyServerUpAfterTestEnd() {
		return JMeterUtils
				.getPropDefault(KEEP_JETTY_SERVER_UP_AFTER_TEST_END_PROP, KEEP_JETTY_SERVER_UP_AFTER_TEST_END);
	}
}
