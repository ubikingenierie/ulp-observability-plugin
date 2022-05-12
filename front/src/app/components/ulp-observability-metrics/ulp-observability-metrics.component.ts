import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MetricsRecord } from 'src/app/model/metrics_record';
import { ULPObservabilityMetrics } from 'src/app/model/ulpobservability_metrics';


interface CardInfo{
  label: string,
  icon: string,
  data: {
    value: number,
    unit?: string,
    comment?: string 
  }
}

interface CardList {
  [name:string]: CardInfo
}

interface CardInfoGroupGroup {
  [name:string]: CardList
}



@Component({
  selector: 'app-ulp-observability-metrics',
  templateUrl: './ulp-observability-metrics.component.html',
  styleUrls: ['./ulp-observability-metrics.component.css']
})
export class UlpObservabilityMetricsComponent implements OnChanges, OnInit {

  @Input() metricsData: ULPObservabilityMetrics = {
    request: [],
    response: [],
    sampleNames: new Set<string>()
  };

  @Input() selectedSample : string = '';


  cardLabels : Array<string> = [];
  cards : CardInfoGroupGroup = {}


  emptyCard : CardList = {
    'total': {
      label: 'Total Requests',
      icon: 'summarize',
      data: {
        unit: '',
        value: 0
      }
    },
    'error': {
      label: 'Error Percentage',
      icon: 'warning',
      data: {
        unit: '%',
        value: 0
      }
    },
    'avg': {
      label: 'Avg Response Time',
      icon: 'schedule',
      data: {
        unit: 'ms',
        value: 0
      }
    },
    'max': {
      label: 'Max Response Time',
      icon: 'arrow_upward',
      data: {
        unit: 'ms',
        value: 0
      }
    },
    'throughput': {
      label: 'Throughput',
      icon: 'rocket_launch',
      data: {
        unit: 'ms',
        value: 0
      }
    }
  }

  

  constructor() { }

  ngOnInit(): void {
    Object.keys(this.cards);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshCards();
    if(this.cards[this.selectedSample] != undefined){
      this.cardLabels = Object.keys(this.cards[this.selectedSample]);
    }
  }

  refreshCards(){

    this.metricsData.request.forEach((record) => {
      if(this.cards[record.name] == undefined){
        this.cards[record.name] = {...this.emptyCard};
      } 

      var error = 0;

      switch(record.label){
        case 'count_total':
          this.cards[record.name]['total'].data.value = record.value;
          break;
        case 'count_error':
          error = record.value;
          break;
        case 'throughput':
          this.cards[record.name]['throughput'].data.value = record.value;
          break;
        default:
      }

      if(this.cards[record.name]['total'].data.value == 0){
        this.cards[record.name]['error'].data.value = 0;
      } else {
        this.cards[record.name]['error'].data.value = error / this.cards[record.name]['total'].data.value * 100;
        this.cards[record.name]['error'].data.comment = '('+error+'/'+this.cards[record.name]['total'].data.value+')';
      }      
      
    });

    this.metricsData.request.forEach((record) => {
      
      if(this.cards[record.name] == undefined){
        this.cards[record.name] = {...this.emptyCard};
      } 
      switch(record.label){
        case 'avg':
          this.cards[record.name]['avg'].data.value = record.value;
          break;
        case 'max':
          this.cards[record.name]['max'].data.value = record.value;
          break;
        default:
          if(record.label.startsWith('pc')){
            var pc = record.label.substring(2);
            this.cards[record.name]['pc'+pc] = {
              label: 'Percentiles '+pc,
              icon: 'percent',
              data: {
                unit: 'ms',
                value: record.value
              }
          }
        }
      }

    });
  }

}
