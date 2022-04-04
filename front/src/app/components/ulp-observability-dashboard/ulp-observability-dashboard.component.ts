import { Component, OnInit, Output } from '@angular/core';
import { ChartData } from 'src/app/models/ChartData';
import Metric from 'src/app/models/Metric';
import { openMetricsToJson } from 'src/app/parsers/openMetrics/openMetricsToJson';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { metricsToChartData, updateChartData } from 'src/app/util/Util';

@Component({
  selector: 'app-ulp-observability-dashboard',
  templateUrl: './ulp-observability-dashboard.component.html',
  styleUrls: ['./ulp-observability-dashboard.component.css']
})
export class UlpObservabilityDashboardComponent implements OnInit {

  metrics: Array<Metric> ;
  chartData: ChartData ;

  

  constructor(private metricService: MetricsService ) {
       this.metrics = [];
   }

  ngOnInit(): void {

    setInterval(()=>{

      console.log("api called");
      let firstCall = true;

      this.metricService.getMetrics().subscribe(metricsText=>{
            
        
              firstCall = this.metrics.length===0;

              this.metrics = openMetricsToJson(metricsText);
              console.log(this.metrics)
              if(firstCall){
                  this.chartData = metricsToChartData(this.metrics);
              }
              
              else{
                
                  this.chartData =  updateChartData(this.chartData, this.metrics);
     

               }
         
          
       })
   }, 5000)

  }

 

}


