import { Component, OnInit } from '@angular/core';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { seconds } from 'src/app/utility/time';
import parsePrometheusTextFormat from 'src/app/utility/parser/prometheus-parser';
import 'chartjs-adapter-moment';
import { Sample } from 'src/app/model/sample';
import { ChartData, Datasets } from 'src/app/model/chart-data';
import { sample } from 'rxjs';


interface NamePostfix {
  name: string,
  postfix: string
}

const MetricsStatus = Object.freeze({
  INFO:0,
  EMPTY:1,
  SUCCESS:2,
  ERROR:3,
  INFO_ERROR:4
});

@Component({
  selector: 'app-ulp-observability-dashboard',
  templateUrl: './ulp-observability-dashboard.component.html',
  styleUrls: ['./ulp-observability-dashboard.component.css']
})
export class UlpObservabilityDashboardComponent implements OnInit{

  private postfix = ['total', 'avg', 'max', 'throughput', 'threads'];
  chartData : ChartData = {};
  datasets: Datasets = {};
  status = MetricsStatus.INFO;

  private updateFrequencyS = 60;

  constructor(private metricService: MetricsService) { }

  ngOnInit(): void {
    this.requestInfo();
  }

  private requestInfo() : void {
    this.metricService.getMetricsServerInfo().subscribe({
      next: (info) =>{
        this.updateFrequencyS = info.logFreq;
        this.metricService.setMetricsURL(info.metricsUrl);
        this.requestMetrics();
      },
      error:() => {
        this.status = MetricsStatus.INFO_ERROR;
        setTimeout(() => this.requestInfo(), seconds(10));
      }
    });
  }

  private requestMetrics(): void {

    this.metricService.getAllMetrics().subscribe({
      next: (metricsText) => {
        const samples : Array<Sample> = parsePrometheusTextFormat(metricsText);
        if(samples.length == 0){
          this.status = MetricsStatus.EMPTY;
        }
        else{
          this.status = MetricsStatus.SUCCESS;
          this.pushData(samples);
        }
      },
      error: () => {
        this.status = MetricsStatus.ERROR;
      }
    });

    setInterval(()=>{
      this.metricService.getLastMetrics().subscribe({
        next: (metricsText) => {
          this.status = MetricsStatus.SUCCESS;
          this.pushData(parsePrometheusTextFormat(metricsText));
        },
        error: () => {
          this.status = MetricsStatus.ERROR;
        }
      });
   }, seconds(this.updateFrequencyS));

  }

  private getNamePostfix(namePostfix: string): NamePostfix {
    const split = namePostfix.split("_");
    return this.postfix.indexOf(split[split.length - 1]) < 0 ?
    {
        name: namePostfix,
        postfix: 'pc'
    } :
    {
        name: split.slice(0,split.length-1).join("_"),
        postfix: split[split.length - 1]
    }
  }

  private prettyName(name: string) : string {
    return name.split("_").map(s => s.charAt(0).toUpperCase()+s.substring(1)).join(" ")
  }

  private pushMetric(type: string, name: string, timestamp: Date, value: any): void {

    if(this.datasets[type] === undefined){
      this.datasets[type] = {};
    }

    if(this.datasets[type][name] === undefined){
      this.datasets[type][name] = [];
    }

    this.datasets[type][name].push({
      x: timestamp,
      y: value
    });

  }

  private addChart(label: string, description: string | undefined) : void {
    if(this.chartData[label] === undefined){
      this.chartData[label] = description ?? '';
    }
  }

  private clearData(){
    Object.keys(this.datasets).forEach(key => {
      this.datasets[key] = {};
    });
  }

  private pushData(samples: Array<Sample>) : void {
    this.clearData();
    const threads: Array<{name: string, x: Date, y: any}> = [];

    samples.forEach(sample => {
      if(sample.metrics[0] !== undefined){
        const namePostfix : NamePostfix = this.getNamePostfix(sample.name);
        const name = this.prettyName(namePostfix.name);

        const timestamp = new Date(+(sample.metrics[0].timestamp_ms ?? sample.metrics[0].created ?? 0));

        switch(namePostfix.postfix){
          case('avg'):
          case('throughput'):
          case('max'):
            this.addChart(namePostfix.postfix,sample.help);
            this.pushMetric(namePostfix.postfix, name, timestamp, sample.metrics[0].value);
            break;
          case('total'):
            let error = 0;
            let count = 0;
            sample.metrics.forEach(metric =>{
              if(metric.labels !== undefined){
                switch(metric.labels['count']){
                  case('all'):
                    this.pushMetric('total', name, timestamp, sample.metrics[0].value);
                    break;
                  case('error'):
                    error = metric.value ?? 0;
                    this.pushMetric('error', name, new Date(timestamp), error);
                    break;
                  case('period'):
                    count = metric.value ?? 0;
                    this.pushMetric('period', name, new Date(timestamp), count);
                    break;
                  default:
                }
              }
            });

            this.addChart('errorP','Error %');
            this.pushMetric(
              'errorP', 
              name, 
              new Date(timestamp), 
              error == 0 ? 0 : error / count * 100
            );
            break;
          case('pc'):
            if(sample.metrics[0].quantiles !== undefined){
              Object.entries(sample.metrics[0].quantiles).forEach(quantile => {
                this.addChart('pc'+quantile[0], sample.help+' ('+quantile[0]+'th)');
                this.pushMetric('pc'+quantile[0], name, timestamp, quantile[1]);
              });
            }
            break;
            case('threads'):
              if(namePostfix.name !== 'total_info'){
                threads.push({
                  name: name,
                  x: timestamp,
                  y: sample.metrics[0].value
                });
              }
              break;
            default:
        }
      } 
    });

    threads.forEach(thread =>{
      Object.keys(this.datasets).forEach(key => {
        this.pushMetric(key, thread.name+" Threads", thread.x, thread.y);
      });
    });
  }
}


