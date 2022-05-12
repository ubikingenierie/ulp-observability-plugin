import { MetricsRecord } from "./metrics_record";

export interface ULPObservabilityMetrics {
    sampleNames: Set<string>
    request: Array<MetricsRecord>,
    response: Array<MetricsRecord>
}