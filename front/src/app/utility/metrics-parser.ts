import { MetricsRecord } from "../model/metrics_record";
import { Sample } from "../model/sample";
import { ULPObservabilityMetrics } from "../model/ulpobservability_metrics";

export function getMetricsData(samples: Array<Sample>): ULPObservabilityMetrics {
    var requestMetrics = new Array<MetricsRecord>();
    var responseMetrics = new Array<MetricsRecord>();
    var sampleNames = new Set<string>();

    samples.forEach((sample) => {
        if(sample.name.endsWith('_request')){
            var sampleName = sample.name.slice(0,sample.name.lastIndexOf('_request'));
            if(!sampleNames.has(sampleName)){
                sampleNames.add(sampleName);
            }
            
            sample.metrics?.forEach((sampleMetrics) => {
                var label : string = sampleMetrics.labels.metrics;
                if(sampleMetrics.labels.status != undefined){
                    label = label + "_" + sampleMetrics.labels.status;
                }
                requestMetrics.push({
                    name: sampleName, 
                    label: label,
                    value: +sampleMetrics.value,
                    timestamp: +sampleMetrics.timestamp_ms                
                });
            });  
        } 
        else if(sample.name.endsWith('_response')){
            var sampleName = sample.name.slice(0,sample.name.lastIndexOf('_response'));
            if(!sampleNames.has(sampleName)){
                sampleNames.add(sampleName);
            }
            sample.metrics?.forEach((sampleMetrics) => {
                var label = sampleMetrics.labels.metrics;
                if(label == 'count'){
                    label = label + "_" + sampleMetrics.labels.status;
                }
                responseMetrics.push({
                    name: sampleName, 
                    label: label,
                    value: +sampleMetrics.value,
                    timestamp: +sampleMetrics.timestamp_ms                
                });
            });  
        } 
    });

    return {
        sampleNames : sampleNames,
        request : requestMetrics,
        response : responseMetrics
    }
}