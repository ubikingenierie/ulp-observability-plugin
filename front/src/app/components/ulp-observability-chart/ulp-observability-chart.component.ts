import { Component, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChildren } from '@angular/core';
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
  @Input() selectedSample : string = '';

  private currentDate: Date = new Date();

  labels = [
    new Date(this.currentDate.getTime()-seconds(this.metricsBuffer * this.updateFrequencyS)),
    this.currentDate
  ];

  datasetGroups: DatasetGroup = {};

  sampleList : Array<string> = [];

  chartType: ChartType = 'line';

  chartOptions: ChartConfiguration['options'];

  @ViewChildren(BaseChartDirective) charts: QueryList<BaseChartDirective> | undefined;
      
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

    this.updateCharts();

  }

  private updateCharts() : void {
    if(this.charts != undefined){
      this.charts.forEach((child) => {
        if(child.chart != undefined){
          child.chart.update()
        }   
      });
    }
    
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.metricsData.length > 0){
      this.newRecord();
    }
    else{
      this.emptyRecord();
    }
    this.updateX();
  }

  protected updateX(){
    var currentDate = new Date();
    this.labels[0] = new Date(this.labels[1].getTime()-seconds(this.updateFrequencyS*this.metricsBuffer));
    this.labels[1] = new Date(currentDate.getTime());
    this.updateCharts();
  }

  protected emptyRecord(){
    Object.keys(this.datasetGroups).forEach((groupName)=>{
      this.datasetGroups[groupName].forEach((dataset) => {
        dataset.data.push({});
        dataset.data.shift();
      });
    });  
    
  }

  protected newRecord(): void {
    this.emptyRecord();
  }

}
