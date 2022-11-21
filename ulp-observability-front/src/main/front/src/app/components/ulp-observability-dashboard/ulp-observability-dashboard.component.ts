import { Component, OnInit } from '@angular/core';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { seconds } from 'src/app/utility/time';
import parsePrometheusTextFormat from 'src/app/utility/parser/prometheus-parser';
import 'chartjs-adapter-moment';
import { Sample } from 'src/app/model/sample';
import { ChartData, DatasetGroup, Datasets } from 'src/app/model/chart-data';
import { map, Observable, startWith } from 'rxjs';
import { FormControl } from '@angular/forms';


interface NamePostfix {
  name: string,
  postfix: string
}

const MetricsStatus = Object.freeze({
  INFO:0,
  EMPTY:1,
  SUCCESS:2,
  ERROR:3
});

@Component({
  selector: 'app-ulp-observability-dashboard',
  templateUrl: './ulp-observability-dashboard.component.html',
  styleUrls: ['./ulp-observability-dashboard.component.css']
})
export class UlpObservabilityDashboardComponent implements OnInit{

  private postfix = ['total', 'avg', 'max', 'throughput', 'threads'];
  private updateFrequencyS = 60;  
  
  totalLabel = 'total_info';
  chartData : ChartData = {};
  datasets: Datasets = {};
  threads: DatasetGroup = {};
  status = MetricsStatus.INFO;
  reqSuccessful = true;

  listSamplers !: Array<string>
  visibleSamplers !: Array<string>
  control = new FormControl('');
  filteredCharts!: Observable<string[]>;
  
  constructor(private metricService: MetricsService) { }

  ngOnInit(): void {
    this.requestInfo();

    this.filteredCharts = this.control.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value || '')),
    );
  }

  private requestInfo() : void {
    this.metricService.getMetricsServerInfo().subscribe({
      next: (info) =>{
        this.updateFrequencyS = info.logFrequency;
        this.totalLabel = info.totalLabel;
        this.metricService.setMetricsURL(info.metricsRoute);
        this.status = MetricsStatus.EMPTY;
        this.requestMetrics();
      },
      error:() => {
        this.status = MetricsStatus.ERROR;
        setTimeout(() => this.requestInfo(), seconds(10));
      }
    });
  }

  private requestMetrics(): void {
    this.sendRequest(this.metricService.getAllMetrics());
    setInterval(()=>this.sendRequest(this.metricService.getLastMetrics()), seconds(this.updateFrequencyS));
  }
  

  private sendRequest(observable : Observable<String>) : void {
    observable.subscribe({
      next: (metricsText) => {
        const samples : Array<Sample> = parsePrometheusTextFormat(metricsText);
        this.reqSuccessful = true;
        if(samples.length > 0){
          this.status = MetricsStatus.SUCCESS;
          this.pushData(samples);
        }
      },
      error: () => {
        this.reqSuccessful = false;
      }
    })
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
    this.threads = {};
  }

  private fillSamplerList(sampleName: string) : void{

    if(this.listSamplers==null){
      this.listSamplers = [];
    }
    if (sampleName.startsWith('spl_')){
      sampleName = sampleName.slice(sampleName.indexOf('_')+1,sampleName.length)
    }
    if (!this.listSamplers.includes(sampleName) && sampleName!==this.totalLabel){
        this.listSamplers.push(sampleName)
    }
  }

  private pushData(samples: Array<Sample>) : void {
    this.clearData();

    samples.forEach(sample => {
      //for this sample sample.name has the correct name of the controllers in jMeter
      if (sample.help === "Response percentiles"){
        this.fillSamplerList(sample.name);
      }

      if(sample.metrics[0] !== undefined){
        const namePostfix : NamePostfix = this.getNamePostfix(sample.name);
        const timestamp = new Date(+(sample.metrics[0].timestamp_ms ?? sample.metrics[0].created ?? 0));

        switch(namePostfix.postfix){
          case('avg'):
          case('throughput'):
          case('max'):
            this.addChart(namePostfix.postfix,sample.help);
            this.pushMetric(namePostfix.postfix, namePostfix.name, timestamp, sample.metrics[0].value);
            break;

          case('total'):
            let error = 0;
            let count = 0;
            sample.metrics.forEach(metric =>{
              if(metric.labels !== undefined){
                switch(metric.labels['count']){
                  case('all'):
                    this.pushMetric('total', namePostfix.name, timestamp, sample.metrics[0].value);
                    break;
                  case('error'):
                    error = metric.value ?? 0;
                    this.pushMetric('error', namePostfix.name, new Date(timestamp), error);
                    break;
                  case('period'):
                    count = metric.value ?? 0;
                    this.pushMetric('period', namePostfix.name, new Date(timestamp), count);
                    break;
                  default:
                }
              }
            });

            this.addChart('errorP','Error %');
            this.pushMetric(
              'errorP', 
              namePostfix.name, 
              new Date(timestamp), 
              error == 0 ? 0 : error / count * 100
            );
            break;

          case('pc'):
            if(sample.metrics[0].quantiles !== undefined){
              Object.entries(sample.metrics[0].quantiles).forEach(quantile => {
                this.addChart('pc'+quantile[0], sample.help+' ('+quantile[0]+'th)');
                this.pushMetric('pc'+quantile[0], namePostfix.name, timestamp, quantile[1]);
              });
            }
            break;

          case('threads'):
            if(this.threads[namePostfix.name + '_threads'] === undefined){
              this.threads[namePostfix.name + '_threads'] = [];
            }
            this.threads[namePostfix.name + '_threads'].push({
              x: timestamp,
              y: sample.metrics[0].value
            });
            break;

          default:
        }
      } 
    });
  }

private _filter(value: string): string[] {
  const filterValue = this._normalizeValue(value);
  this.visibleSamplers = Object.values(this.listSamplers).filter(sample => this._normalizeValue(sample).includes(filterValue));
  return this.visibleSamplers;
}

private _normalizeValue(value: string): string {
  return value.toLowerCase().replace(/\s/g, '');
}

}


