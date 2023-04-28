export interface Label {
    [key:string]: string
}

export interface Quantiles {
    [quantile:string] : number
}

export interface Metrics {
    value?: number,
    labels?: Label
    timestamp_ms?: Date,
    quantiles?: Quantiles,
    quantilesEveryPeriods?: Quantiles,
    count?: number,
    created?: number
}

export interface Sample {
    help?: string,
    metrics: Array<Metrics>,
    name: string,
    type: string
}