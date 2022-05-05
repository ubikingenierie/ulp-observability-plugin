import { Metrics } from "./metrics";

export interface Sample {
    help?: string,
    metrics?: Array<Metrics>,
    name: string,
    type?: string
}