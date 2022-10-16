import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { ChartType, ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import 'chartjs-adapter-moment';
import { DatasetGroup } from 'src/app/model/chart-data';
import * as moment from 'moment';


@Component({
  selector: 'app-ulp-observability-chart',
  templateUrl: './ulp-observability-chart.component.html',
  styleUrls: ['./ulp-observability-chart.component.css']
})
export class UlpObservabilityChartComponent implements OnChanges, OnInit {
  
  @Input() datasets : DatasetGroup = {};
  @Input() threads : DatasetGroup = {};
  @Input() title : string = '';
  @Input() totalLabel = 'total_info';

  private names : Array<string> = [];
  
  public data : ChartConfiguration['data'] = {
    datasets: []
  };

  public chartType: ChartType = 'line';
  public chartOptions: ChartConfiguration['options'];

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
      
  constructor() { }

  ngOnInit(): void {
    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false,
      },
      elements: {
        point:{
            radius: 1
        }
      },
      plugins: {
        title: {
          display: true,
          text: this.title
        },
        legend: {
          align: 'center'
        }
      },
      scales: {
        x: {
            type: 'time',
            grid: {
              display: true,
              color: 'lightgrey',
              tickColor: 'grey'
            },
            ticks: {
              display: true,
            },
            time: {
              unit: 'minute',
              displayFormats: {
                minute: 'HH:mm'
              },
            },
        },
        y: {
          type: 'linear',
          display: true,
          min: 0,
          position: 'left',
          offset: true
        },
        y1: {
          type: 'linear',
          display: true,
          min: 0,
          suggestedMax: 1,
          position: 'right',
          ticks:{
            stepSize: 1
          },
          grid: {
            drawOnChartArea: false,
          },
          offset: true
        }
  
      },
    };
  }

  
  private updateChart(): void{

    Object.entries({...this.datasets, ...this.threads}).forEach(entry => {
      let curveLabel = entry[0]
      if(curveLabel !== this.totalLabel && curveLabel !== this.totalLabel+'_threads' && !(curveLabel.startsWith('spl_') && curveLabel.endsWith('_threads'))   ){

        if(curveLabel.startsWith('spl_')){
          curveLabel = curveLabel.slice(curveLabel.indexOf('_')+1,curveLabel.length);
        }
        
        if(this.names.indexOf(curveLabel) < 0){
          this.names.push(curveLabel);
          this.data.datasets.push({
            label: curveLabel,
            data: [],
            yAxisID: curveLabel.endsWith('_threads') ? 'y1' : 'y'
          });
        }
  
        this.data.datasets.forEach(dataset => {
          if(dataset.label === curveLabel){
            entry[1].forEach(metric =>{
              dataset.data.push(metric);
            })
            
          }
        });
      }
    });
    this.chart?.update();

  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateChart();
  }
}
