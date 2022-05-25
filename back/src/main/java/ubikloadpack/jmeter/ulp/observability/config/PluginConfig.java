package ubikloadpack.jmeter.ulp.observability.config;

/**
 * Class containing plugin configuration properties
 * 
 * @author Valentin ZELIONII
 *
 */
public class PluginConfig {

	private final String pluginName;
	private final Integer jettyServerPort;
	private final String metricsRoute;
	private final String webAppRoute;
	private final Integer threadSize;
	private final Integer bufferCapacity;
	private final Integer pct1;
	private final Integer pct2;
	private final Integer pct3;
	private final Integer pctPrecision;
	private final Integer logFrequency;
	private final String totalLabel;
	
	public PluginConfig(String pluginName, Integer jettyServerPort, String metricsRoute, String webAppRoute,
			Integer threadSize, Integer bufferCapacity, Integer pct1, Integer pct2, Integer pct3, Integer pctPrecision, Integer logFrequency,
			String totalLabel) {
		this.pluginName = pluginName;
		this.jettyServerPort = jettyServerPort;
		this.metricsRoute = metricsRoute;
		this.webAppRoute = webAppRoute;
		this.threadSize = threadSize;
		this.bufferCapacity = bufferCapacity;
		this.pct1 = pct1;
		this.pct2 = pct2;
		this.pct3 = pct3;
		this.pctPrecision = pctPrecision;
		this.logFrequency = logFrequency;
		this.totalLabel = totalLabel;
	}
	
	public PluginConfig() {
		this(
				ULPObservabilityDefaultConfig.pluginName(),
				ULPObservabilityDefaultConfig.jettyServerPort(),
				ULPObservabilityDefaultConfig.jettyMetricsRoute(),
				ULPObservabilityDefaultConfig.jettyWebAppRoute(),
				ULPObservabilityDefaultConfig.threadSize(),
				ULPObservabilityDefaultConfig.bufferCapacity(),
				ULPObservabilityDefaultConfig.pct1(),
				ULPObservabilityDefaultConfig.pct2(),
				ULPObservabilityDefaultConfig.pct3(),
				ULPObservabilityDefaultConfig.pctPrecision(),
				ULPObservabilityDefaultConfig.logFrequecny(),
				ULPObservabilityDefaultConfig.totalLabel()
				);
	}
	
	
	public String getPluginName() {
		return pluginName;
	}

	public Integer getJettyServerPort() {
		return jettyServerPort;
	}

	public String getMetricsRoute() {
		return metricsRoute;
	}

	public String getWebAppRoute() {
		return webAppRoute;
	}

	public Integer getThreadSize() {
		return threadSize;
	}
	
	public Integer getBufferCapacity() {
		return bufferCapacity;
	}

	public Integer getPct1() {
		return pct1;
	}

	public Integer getPct2() {
		return pct2;
	}

	public Integer getPct3() {
		return pct3;
	}

	public Integer getPctPrecision() {
		return pctPrecision;
	}

	public Integer getLogFrequency() {
		return logFrequency;
	}

	public String getTotalLabel() {
		return totalLabel;
	}
	

}
