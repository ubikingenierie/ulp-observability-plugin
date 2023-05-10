import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
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
    'samplerCountEveryPeriods': {
      label: 'statistics.totalRequests',
      icon: 'summarize',
      data: {
        unit: '',
        value: '0'
      }
    },
    'avg': {
      label: 'statistics.avg',
      icon: 'schedule',
      data: {
        unit: 'ms',
        value: '0'
      }
    },
    'error': {
      label: 'statistics.error',
      icon: 'warning',
      data: {
        unit: '%',
        value: '0'
      }
    },
    'max': {
      label: 'statistics.max',
      icon: 'arrow_upward',
      data: {
        unit: 'ms',
        value: '0'
      }
    },
    'throughput': {
      label: 'statistics.throughput',
      icon: 'rocket_launch',
      data: {
        unit: 'req/s',
        value: '0'
      }
    },
    'threads' :{
      label: 'statistics.threads',
      icon: 'memory',
      data: {
        unit: 'threads',
        value: '0'
      }
    }
  }

  constructor(private translate: TranslateService) { }

  ngOnInit(): void {
    
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshCards();
  }

  refreshCards() {
    if(this.threads !== undefined){
      const lastIndex = this.threads.length - 1;
      ['avg','max','throughput'].forEach(type =>{
        this.cards[type].data.value = this.datasets[type + '_every_periods'][this.totalLabel][lastIndex].y;
      });
      this.cards['samplerCountEveryPeriods'].data.value = this.datasets['samplerCountEveryPeriods'][this.totalLabel][lastIndex].y;

      this.cards['error'].data.value = (this.datasets['errorEveryPeriods'][this.totalLabel][lastIndex].y / this.datasets['samplerCountEveryPeriods'][this.totalLabel][lastIndex].y * 100).toFixed(3);
      this.cards['error'].data.comment = '('+ this.datasets['errorEveryPeriods'][this.totalLabel][lastIndex].y + '/'+ this.datasets['samplerCountEveryPeriods'][this.totalLabel][lastIndex].y+')';

      this.cards['threads'].data.value = this.threads[lastIndex].y;

      Object.keys(this.datasets).filter(type => type.startsWith('pctEveryPeriods')).forEach(pct => {
        let percentileNumber = (pct.match(/\d/g) ?? ["0"]).join(""); // regex that get every numbers of a string
        this.cards[pct] = {
          label: 'Percentile '+ percentileNumber + 'th',
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
