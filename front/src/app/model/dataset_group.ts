import { Dataset } from "./dataset";

export interface DatasetGroup {
  [name:string]: Array<Dataset>
}