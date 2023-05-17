import { Component, OnInit } from '@angular/core';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { seconds } from 'src/app/utility/time';
import parsePrometheusTextFormat from 'src/app/utility/parser/prometheus-parser';
import 'chartjs-adapter-moment';
import { Sample } from 'src/app/model/sample';
import { ChartData, DatasetGroup, Datasets } from 'src/app/model/chart-data';
import { map, Observable, startWith } from 'rxjs';
import { FormControl } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';


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

  private acceptedPostfixs = ['total', 'avg', 'avg_every_periods', 'max', 'max_every_periods', 'throughput', 'throughput_every_periods', 'threads', 'threads_every_periods', 'pct'];
  private updateFrequencyS = 60;  
  
  totalLabel = 'total_info';
  chartData : ChartData = {};
  datasets: Datasets = {};
  threads: DatasetGroup = {};
  threadsEveryPeriods: DatasetGroup = {};
  status = MetricsStatus.INFO;
  showErrorMessage = false;

  listSamplers !: Array<string>
  visibleSamplers !: Array<string>
  control = new FormControl('');
  filteredCharts!: Observable<string[]>;
  
  constructor(private metricService: MetricsService, private translate: TranslateService) { }

  ngOnInit(): void {
    this.requestInfo();

    this.filteredCharts = this.control.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value || '')),
    );
  }

  closeError() : void {
    this.showErrorMessage = false;
  }

  private requestInfo() : void {
    this.metricService.getMetricsServerInfo().subscribe({
      next: (info) => {
        this.updateFrequencyS = info.logFrequency;
        this.totalLabel = info.totalLabel;
        this.metricService.setMetricsURL(info.metricsRoute);
        this.translate.setDefaultLang(info.localeLang);
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
        this.showErrorMessage = false;
        if(samples.length > 0){
          this.status = MetricsStatus.SUCCESS;
          this.pushData(samples);
        }
      },
      error: () => {
        this.showErrorMessage = true;
      }
    })
  }

  private getNamePostfix(name: string) {
    const split = name.split("_");
  
    let postfix = this.acceptedPostfixs.find(acceptedPostfix => {
      let acceptedPostfixSize = acceptedPostfix.length;
      let namePostfixSize = name.length;
  
      return namePostfixSize > acceptedPostfixSize && name.substring(namePostfixSize - acceptedPostfixSize, namePostfixSize) === acceptedPostfix;
    });
  
    return postfix ? 
    {
      name: name.substring(0, name.length - postfix.length - 1),
      postfix: postfix
    } :
    {
      name: name,
      postfix: ''
    };
  };

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

  private addChart(label: string, description: string | undefined, unit: string) : void {
    if(this.chartData[label] === undefined) {
      this.chartData[label] = {
        description: description ?? '',
        unit: unit
      };
    }
  }

  private clearData(){
    Object.keys(this.datasets).forEach(key => {
      this.datasets[key] = {};
    });
    this.threads = {};
    this.threadsEveryPeriods = {};
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
        this.fillSamplerList(sample.name.substring(0, sample.name.length - 4));
      }

      if(sample.metrics[0] !== undefined) {
        const nameAndPostfix : NamePostfix = this.getNamePostfix(sample.name);
        const timestamp = new Date(+(sample.metrics[0].timestamp_ms ?? sample.metrics[0].created ?? 0));
        const sampleHelp = "charts." + sample.help?.toLowerCase() ?? ''; // The title of a chart is retrieved from charts.<chartName> in assets/i18n 

        switch(nameAndPostfix.postfix){
          case('avg'):
          case('max'):
            this.addChart(nameAndPostfix.postfix, this.translate.instant(sampleHelp), 'ms');
            this.pushMetric(nameAndPostfix.postfix, nameAndPostfix.name, timestamp, sample.metrics[0].value);
            break;
          case('throughput'):
            this.addChart(nameAndPostfix.postfix, this.translate.instant(sampleHelp), 'req/s');
            this.pushMetric(nameAndPostfix.postfix, nameAndPostfix.name, timestamp, sample.metrics[0].value);
            break;

          case('avg_every_periods'):
          case('throughput_every_periods'):
          case('max_every_periods'):
            this.pushMetric(nameAndPostfix.postfix, nameAndPostfix.name, timestamp, sample.metrics[0].value);
            break;

          case('total'):
            let error = 0;
            let count = 0;
            let errorEveryPeriods = 0;
            sample.metrics.forEach(metric =>{
              if(metric.labels !== undefined){
                switch(metric.labels['count']){
                  case('sampler_count_every_periods'):
                    this.pushMetric('samplerCountEveryPeriods', nameAndPostfix.name, timestamp, sample.metrics[0].value);
                    break;
                  case('error'):
                    error = metric.value ?? 0;
                    this.pushMetric('error', nameAndPostfix.name, new Date(timestamp), error);
                    break;
                  case('error_every_periods'):
                    errorEveryPeriods = metric.value ?? 0;
                    if (metric.labels['type']) {
                      this.pushMetric('errorEveryPeriods_' + metric.labels['type'], nameAndPostfix.name, new Date(timestamp), errorEveryPeriods);
                    } else {
                      this.pushMetric('errorEveryPeriods', nameAndPostfix.name, new Date(timestamp), errorEveryPeriods);
                    }
                    break;
                  case('sampler_count'):
                    count = metric.value ?? 0;
                    this.pushMetric('samplerCount', nameAndPostfix.name, new Date(timestamp), count);
                    break;
                  default:
                }
              }
            });

            this.addChart('errorP', this.translate.instant(sampleHelp) + " %", '%');
            this.pushMetric(
              'errorP', 
              nameAndPostfix.name, 
              new Date(timestamp), 
              error == 0 ? 0 : (error / count * 100).toFixed(3)
            );
            break;

          case('pct'):
            if(sample.metrics[0].quantiles !== undefined){
              Object.entries(sample.metrics[0].quantiles).forEach(quantile => {
                this.addChart('pct'+quantile[0], this.translate.instant(sampleHelp, {value: quantile[0]}), 'ms');
                this.pushMetric('pct'+quantile[0], nameAndPostfix.name, timestamp, quantile[1]);
              });
            }
            if(sample.metrics[0].quantilesEveryPeriods !== undefined){
              Object.entries(sample.metrics[0].quantilesEveryPeriods).forEach(quantile => {
                this.pushMetric('pctEveryPeriods'+quantile[0], nameAndPostfix.name, timestamp, quantile[1]);
              });
            }
            break;

          case('threads'):
            if(this.threads[nameAndPostfix.name + '_threads'] === undefined){
              this.threads[nameAndPostfix.name + '_threads'] = [];
            }
            this.threads[nameAndPostfix.name + '_threads'].push({
              x: timestamp,
              y: sample.metrics[0].value
            });
            break;
          case('threads_every_periods'):
            if(this.threadsEveryPeriods[nameAndPostfix.name + '_threads_every_periods'] === undefined){
              this.threadsEveryPeriods[nameAndPostfix.name + '_threads_every_periods'] = [];
            }
            this.threadsEveryPeriods[nameAndPostfix.name +'_threads_every_periods'].push({
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


