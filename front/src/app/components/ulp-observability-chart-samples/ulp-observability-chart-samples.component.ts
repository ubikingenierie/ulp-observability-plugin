import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Dataset } from 'src/app/model/dataset';
import { MetricsRecord } from 'src/app/model/metrics_record';
import { UlpObservabilityChartComponent } from '../ulp-observability-chart/ulp-observability-chart.component';

@Component({
  selector: 'app-ulp-observability-chart-samples',
  templateUrl: '../ulp-observability-chart/ulp-observability-chart.component.html',
  styleUrls: ['../ulp-observability-chart/ulp-observability-chart.component.css']
})
export class UlpObservabilityChartSamplesComponent extends UlpObservabilityChartComponent implements OnChanges, OnInit  {

  constructor() {
    super();
  }

  override ngOnInit(): void {
    super.ngOnInit();
  }

  override ngOnChanges(changes: SimpleChanges): void {
    super.ngOnChanges(changes);
  }
  
  protected override pushNew(records: Array<MetricsRecord>): void {
    records.forEach((record) => {
      if(record.name != '_total'){
      
      if(this.datasetGroups[record.name] == undefined){
        this.datasetGroups[record.name] = [];
        this.sampleList.push(record.name);
        if(this.sampleList.length == 1){
          this.selectedSample = this.sampleList[0];
        }
      } 

      var datasets: Array<Dataset> = this.datasetGroups[record.name];
      var dataset: Dataset | undefined = datasets.find(dataset => dataset.label == record.label);

      if(dataset == undefined){
        dataset = {
          data: new Array(this.metricsBuffer).fill({}),
          label: record.label
        }
        datasets.push(dataset);
      }

      dataset.data.push({x: new Date(record.timestamp), y: record.value});
      dataset.data.shift();
      }
      
    })
  }
}
