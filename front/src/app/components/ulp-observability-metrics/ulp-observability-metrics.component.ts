import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { DatasetGroup, Datasets } from 'src/app/model/chart-data';


interface CardInfo{
  label: string,
  icon: string,
  data: {
    value: string,
    unit?: string,
    comment?: string 
  }
}

interface CardList {
  [name:string]: CardInfo
}


@Component({
  selector: 'app-ulp-observability-metrics',
  templateUrl: './ulp-observability-metrics.component.html',
  styleUrls: ['./ulp-observability-metrics.component.css']
})
export class UlpObservabilityMetricsComponent implements OnChanges, OnInit {

  @Input() datasets : Datasets = {};
  @Input() threads : Array<any> = [];
  @Input() totalLabel = 'total_info';

  cards : CardList = {
    'total': {
      label: 'Total Requests',
      icon: 'summarize',
      data: {
        unit: '',
        value: '0'
      }
    },
    'avg': {
      label: 'Avg Response Time',
      icon: 'schedule',
      data: {
        unit: 'ms',
        value: '0'
      }
    },
    'error': {
      label: 'Error Percentage',
      icon: 'warning',
      data: {
        unit: '%',
        value: '0'
      }
    },
    'max': {
      label: 'Max Response Time',
      icon: 'arrow_upward',
      data: {
        unit: 'ms',
        value: '0'
      }
    },
    'throughput': {
      label: 'Throughput',
      icon: 'rocket_launch',
      data: {
        unit: 'req/s',
        value: '0'
      }
    },
    'threads' :{
      label: 'Threads',
      icon: 'memory',
      data: {
        unit: 'threads',
        value: '0'
      }
    }
  }

  constructor() { }

  ngOnInit(): void {
    
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshCards();
  }

  refreshCards(){
    if(this.threads !== undefined && this.datasets !== {}){
      const lastIndex = this.threads.length - 1;
      ['avg','max','total','throughput'].forEach(type =>{
        this.cards[type].data.value = this.datasets[type][this.totalLabel][lastIndex].y;
      });

      this.cards['error'].data.value = (this.datasets['error'][this.totalLabel][lastIndex].y / this.datasets['period'][this.totalLabel][lastIndex].y * 100).toFixed(3);
      this.cards['error'].data.comment = '('+ this.datasets['error'][this.totalLabel][lastIndex].y + '/'+ this.datasets['period'][this.totalLabel][lastIndex].y+')';

      this.cards['threads'].data.value = this.threads[lastIndex].y;

      Object.keys(this.datasets).filter(type => type.startsWith('pc')).forEach(pct => {
        this.cards[pct] = {
          label: 'Percentile '+pct.substring(2) + 'th',
          icon: 'percent',
              data: {
                unit: 'ms',
                value: this.datasets[pct][this.totalLabel][lastIndex].y
              }
        }
      });
    }
  }

}
