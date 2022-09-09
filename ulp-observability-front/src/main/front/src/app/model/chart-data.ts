export interface ChartData {
    [id:string] : string;
}

export interface DatasetGroup {
  [name:string]: Array<any>
}

export interface Datasets{
  [type:string]: DatasetGroup
}