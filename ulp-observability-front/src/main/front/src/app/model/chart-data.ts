export interface ChartData {
    [id:string] : { description: string; unit: string; };
}

export interface DatasetGroup {
  [name:string]: Array<any>
}

export interface Datasets{
  [type:string]: DatasetGroup
}