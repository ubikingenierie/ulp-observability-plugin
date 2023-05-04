import { Title } from '@angular/platform-browser';
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
  @Input() unit : string = '';
  @Input() totalLabel = 'total_info';
  @Input() visibleSamplers !: Array<string>;

  private names : Array<string> = [];
  
  public data : ChartConfiguration['data'] = {
    datasets: []
  };

  public chartType: ChartType = 'line';
  public chartOptions: ChartConfiguration['options'];

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
      
  constructor() { }

  ngOnInit(): void {
    let chartUnit = this.unit;
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
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              let label = context.dataset.label || '';
              let unitToUse = (label.length > 9 && label.substring(label.length - 8, label.length) === '_threads') ? '' : chartUnit;

              if (label) {
                  label += ': ';
              }
              if (context.parsed.y !== null) {
                  label += context.parsed.y + ' ' + unitToUse;
              }
              return label;
            }
          }
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

      const isNotTotalLabel = curveLabel !== this.totalLabel;
      const isNotTotalThreadsLabel = curveLabel !== this.totalLabel + '_threads';
      const hasSplPrefix = curveLabel.startsWith('spl_');
      const hasThreadsSuffix = curveLabel.endsWith('_threads');
      const isSplWithoutThreads = hasSplPrefix && !hasThreadsSuffix;
      const isNotSplWithThreads = !hasSplPrefix && hasThreadsSuffix;

      if(isNotTotalLabel && isNotTotalThreadsLabel && (isSplWithoutThreads || isNotSplWithThreads)){

        if(hasSplPrefix){
          curveLabel = curveLabel.slice(curveLabel.indexOf('_')+1,curveLabel.length);
        }
        
        if(this.names.indexOf(curveLabel) < 0){
          this.names.push(curveLabel);
          this.data.datasets.push({
            label: curveLabel,
            data: [],
            yAxisID: hasThreadsSuffix ? 'y1' : 'y',
          });
        }
  
        this.data.datasets.forEach(dataset => {
          if(dataset.label === curveLabel){
            entry[1].forEach(metric =>{
              dataset.data.push(metric);
              dataset.hidden=this.visibleSamplers.includes(curveLabel) || hasThreadsSuffix?false:true
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
