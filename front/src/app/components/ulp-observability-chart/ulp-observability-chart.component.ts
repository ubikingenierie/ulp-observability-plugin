import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { ChartLine } from 'src/app/models/ChartData';

@Component({
  selector: 'app-ulp-observability-chart',
  templateUrl: './ulp-observability-chart.component.html',
  styleUrls: ['./ulp-observability-chart.component.css']
})
export class UlpObservabilityChartComponent implements OnInit, OnChanges {

      

      @Input() barChartLabels: Array<String>;
      @Input() barChartData : Array<any>;

      barChartOptions = {
        scaleShowVerticalLines: false,
        responsive: true
      };

      barChartType = "line";
      barChartLegend = true;

      public lineChartOptions: ChartConfiguration['options'] = {
        elements: {
          line: {
            tension: 0.5
          }
        },
        scales: {
          // We use this empty structure as a placeholder for dynamic theming.
          x: {},
          'y-axis-0':
            {
              position: 'left',
              max:'5000',
              
            },
            
          'y-axis-1': {
            position: 'right',
            grid: {
              color: 'rgba(255,0,0,0.3)',
            },
            ticks: {
              color: 'red'
            }
          }
        },
    
        
      };
      

      constructor() { }

      


      ngOnInit(): void {
           console.log(this.barChartLabels)
      }

      ngOnChanges(changes: SimpleChanges): void {
    
            console.log('changed in metrics');
        
            if(changes['barChartData']){
                  console.log(changes['barChartData'].currentValue)
                  this.barChartData = changes['barChartData'].currentValue;
            }
            else if(changes['barChartLabels']){
                this.barChartData = changes['barChartLabels'].currentValue;     
              }
         
   }

}
