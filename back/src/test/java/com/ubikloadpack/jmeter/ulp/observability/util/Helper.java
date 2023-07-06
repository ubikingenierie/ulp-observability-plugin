package com.ubikloadpack.jmeter.ulp.observability.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ubikloadpack.jmeter.ulp.observability.log.SampleLog;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;

public class Helper {
	/**
	 * Generates a string representation of expected open metrics from a given SampleLog instance.
	 * This method uses a FreeMarker template (template.ftl) to format the string.
	 * 
	 * @param sampleLog The SampleLog instance for which the open metrics string should be generated.
	 * @return A string representation of expected open metrics, formatted according to the FreeMarker template.
	 * 
	 * @throws IOException If an I/O exception occurs while processing the FreeMarker template.
	 * @throws TemplateException If a FreeMarker exception occurs while processing the template.
	 */
	private static String getExpectedOpenMetrics(SampleLog sampleLog, String templateFilename) throws IOException, TemplateException {
	    Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
	    
	    // Charger les templates à partir du classpath
	    cfg.setClassLoaderForTemplateLoading(Helper.class.getClassLoader(), "/templates");
	    
	    cfg.setDefaultEncoding("UTF-8");
	    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	    cfg.setLogTemplateExceptions(false);
	    cfg.setWrapUncheckedExceptions(true);
	    cfg.setFallbackOnNullLoopVariable(false);
	    cfg.setNumberFormat("0.##");
	    
	    /* Create a data model */
	    Map<String, Object> root = new HashMap<>();
	    root.put("sampleName", sampleLog.getSampleName());
	    
	    ValueAtPercentile[] pcts = sampleLog.getPct();
	    ValueAtPercentile[] pctsTotal = sampleLog.getPctTotal();
	    for (int i = 0; i < pcts.length; i++) {
	    	root.put("quantile"+(i+1), (long) (pcts[i].percentile() * 100));
	    	root.put("quantile"+(i+1), (long) (pctsTotal[i].percentile() * 100));
	    	
	    	root.put("pct"+(i+1), (long) pcts[i].value());
	    	root.put("pctTotal"+(i+1), (long) pctsTotal[i].value());
	    }
	    root.put("timestamp", sampleLog.getTimeStamp().getTime());
	    root.put("sum", sampleLog.getSum());
	    root.put("max", sampleLog.getMax());
	    root.put("maxTotal", sampleLog.getMaxTotal());
	    root.put("avg", sampleLog.getAvg());
	    root.put("avgTotal", sampleLog.getAvgTotal());
	    root.put("samplerCountTotal", sampleLog.getSamplerCountTotal());
	    root.put("samplerCount", sampleLog.getSamplerCount());
	    root.put("error", sampleLog.getError());
	    root.put("errorTotal", sampleLog.getErrorTotal());
	    
	    if (sampleLog.getTopErrors().isPresent()) {
	    	List<ErrorTypeInfo> errors = sampleLog.getTopErrors().get();
	    	
	    	for (int i = 0; i < errors.size(); i++) {
	    		long occurrenceTotal = errors.stream().mapToLong(e -> e.getOccurence()).sum();
	    		ErrorTypeInfo error = errors.get(i);
	    		root.put("errorType_"+error.getErrorType(), error.getErrorType());
	    		root.put("errorOccurrence_"+error.getErrorType(), error.getOccurence());
	    		root.put("errorFreq_"+error.getErrorType(), error.computeErrorRateAmongErrors(occurrenceTotal));
	    		root.put("errorRate_"+error.getErrorType(), error.computeErrorRateAmongRequests(sampleLog.getSamplerCountTotal()));
	    	}
	    }
	    
	    root.put("throughput", sampleLog.getThroughput());
	    root.put("throughputTotal", sampleLog.getThroughputTotal());
	    root.put("threads", sampleLog.getThreads());
	    root.put("threadsTotal", sampleLog.getThreadsTotal());
	    

	    Template temp = cfg.getTemplate(templateFilename);
	    try (Writer out = new StringWriter()) {
	        temp.process(root, out);
	        return out.toString();
	    }
	}
	
	public static String getExpectedOpenMetrics(SampleLog sampleLog) throws Exception {
		return getExpectedOpenMetrics(sampleLog, "expectedOpenMetrics.ftl");
	}
	
	public static String getExpectedOpenMetricsWithTopErrors(SampleLog sampleLog) throws Exception {
		return getExpectedOpenMetrics(sampleLog, "expectedOpenMetrics_with_topErrors.ftl");
	}



}
