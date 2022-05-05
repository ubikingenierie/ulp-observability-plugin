import { MetricsLabel } from "../model/metrics_label";
import { MetricsRecord } from "../model/metrics_record";
import { Sample } from "../model/sample";

export function getSampleNames(samples: Array<Sample>): Array<string> {
    var sampleNames = new Array<string>();
    samples.forEach((sample) => {
        if(sampleNames.indexOf(sample.name.split('_')[0]) < 0){
            sampleNames.push(sample.name.split('_')[0]);
        }
    });

    return sampleNames;
}

export function getMetricsData(samples: Array<Sample>): Array<MetricsRecord> {
    var metricsData = new Array<MetricsRecord>();
    samples.forEach((sample) => {
        var sampleSplit = sample.name.split('_');
        if(sampleSplit[sampleSplit.length-1] == 'metrics'){
            var metricsName = (sampleSplit.slice(0,sampleSplit.indexOf('latency'))).join('_');
            sample.metrics?.forEach((sampleMetrics) => {
                metricsData.push({
                    name: metricsName, 
                    label: sampleMetrics.labels.metrics,
                    value: +sampleMetrics.value,
                    timestamp: +sampleMetrics.timestamp_ms                
                });
            });        
        }
    });
    return metricsData;
}