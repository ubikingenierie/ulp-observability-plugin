import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { ChartType, ChartConfiguration } from 'chart.js';
import 'chartjs-adapter-moment';
import { BaseChartDirective } from 'ng2-charts';
import { DatasetGroup } from 'src/app/model/dataset_group';
import { MetricsRecord } from 'src/app/model/metrics_record';
import { seconds } from 'src/app/utility/time';

@Component({
  selector: 'app-ulp-observability-chart',
  templateUrl: './ulp-observability-chart.component.html',
  styleUrls: ['./ulp-observability-chart.component.css']
})
export class UlpObservabilityChartComponent implements OnChanges, OnInit {
  
  @Input() metricsData: MetricsRecord[] = [];
  @Input() updateFrequencyS = 5;
  @Input() metricsBuffer = 120;
  @Input() stepSizeM = 1;

  private currentDate: Date = new Date();

  labels = [
    new Date(this.currentDate.getTime()-seconds(this.metricsBuffer * this.updateFrequencyS)),
    this.currentDate
  ];

  datasetGroups: DatasetGroup = {};

  sampleList : Array<string> = [];
  selectedSample = '';

  chartType: ChartType = 'line';

  chartOptions: ChartConfiguration['options'];

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
      
  constructor() { }

  ngOnInit(): void {
    this.currentDate = new Date();
    this.labels = [
      new Date(this.currentDate.getTime()-seconds(this.metricsBuffer * this.updateFrequencyS)),
      this.currentDate
    ];

    this.chartOptions = {
      responsive: true,
      elements: {
        point:{
            radius: 1
        }
      },
      plugins: {
        legend: {
          align: 'center'
        }
      },
      scales: {
        x: {
            stacked: true,
            type: 'time',
            grid: {
              display: true,
              color: 'lightgrey',
              tickColor: 'grey'
            },
            ticks: {
              display: true,
              major: {
                enabled: true,
              }
            },
            //afterBuildTicks:
            time: {
              unit: 'minute',
              stepSize: this.stepSizeM,
              displayFormats: {
                minute: 'HH:mm'
              },
            },
        },
        y: {
          min: 0,
          suggestedMax: 100,
          position: 'left',
          ticks: {
            stepSize: 10
        }
        },
      }
    }

    this.chart?.update();

  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.metricsData.length > 0){
      this.pushNew(this.metricsData);
    }
    else{
      this.pushEmpty();
    }
    this.updateX();
  }

  protected updateX(){
    var currentDate = new Date();
    this.labels[0] = new Date(this.labels[1].getTime()-seconds(this.updateFrequencyS*this.metricsBuffer));
    this.labels[1] = new Date(currentDate.getTime());
    this.chart?.update();
  }

  protected pushEmpty(){
    Object.keys(this.datasetGroups).forEach((groupName)=>{
      this.datasetGroups[groupName].forEach((dataset) => {
        dataset.data.push({});
        dataset.data.shift();
      });
    });  
    
  }

  protected pushNew(records: Array<MetricsRecord>): void {
    this.pushEmpty();
  }

}
