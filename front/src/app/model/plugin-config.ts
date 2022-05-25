export interface PluginConfig {
	pluginName: string,
	jettyServerPort: number,
	metricsRoute: string,
	webAppRoute: string,
	threadSize: number,
	bufferCapacity: number,
	pct1: number,
	pct2: number,
	pct3: number,
	pctPrecision: number,
	logFrequency: number,
	totalLabel: string
}