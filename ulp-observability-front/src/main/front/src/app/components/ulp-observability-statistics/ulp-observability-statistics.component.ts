import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { TranslateService } from '@ngx-translate/core';
import { DatasetGroup, Datasets } from 'src/app/model/chart-data';

interface StatInfo{
  label: string,
  data: {
    value: string,
    unit?: string,
  }
}

interface StatList {
  [name:string]: StatInfo
}

interface KeyAndLabel {
  key: string,
  label: string
}

@Component({
  selector: 'app-ulp-observability-statistics',
  templateUrl: './ulp-observability-statistics.component.html',
  styleUrls: ['./ulp-observability-statistics.component.css']
})

export class UlpObservabilityStatisticsComponent implements OnChanges,OnInit {

  @Input() datasets : Datasets = {};
  @Input() threads : DatasetGroup = {};
  
  dataSource!: MatTableDataSource<StatList>;
  statLine !: Array<StatList>;
  columnsToDisplay !: Array<KeyAndLabel>;
  columnsKeys !: Array<string>;
  
  constructor(private translate: TranslateService) { }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.statLine)
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshStats();
  }
  
  /**
 * Refresh mat-table content on changes
 *
 * Only take the samplers metrics that start with 'spl_' and iterate through them to fill the table
 */
  refreshStats(){
    this.statLine = [];
    this.columnsToDisplay = [];
    this.columnsKeys = [];
    if(this.threads !== undefined){
      for (const [key, value] of Object.entries({...this.threads})){
        
        if (key.startsWith('spl_')){
          const lastIndex = value.length - 1;
          var samplerName = key.slice(0, key.lastIndexOf('_'))

          var stats : StatList = {
            'sampler':{
              label:'metrics.samplerName',
              data:{
                unit:'',
                value:'0'
              }
            },
            'samplerCountEveryPeriods': {
              label: 'metrics.totalRequests',
              data: {
                unit: '',
                value: '0'
              }
            },
            'avg': {
              label: 'metrics.avg',
              data: {
                unit: 'ms',
                value: '0'
              }
            },
            'error': {
              label: 'metrics.error',
              data: {
                unit: '%',
                value: '0'
              }
            },
            'max': {
              label: 'metrics.max',
              data: {
                unit: 'ms',
                value: '0'
              }
            },
            'throughput': {
              label: 'metrics.throughput',
              data: {
                unit: 'req/s',
                value: '0'
              }
            },
          };

          ['avg','max','throughput'].forEach(type =>{
            stats[type].data.value = this.datasets[type + '_every_periods'][samplerName][lastIndex].y;
          });
          stats['samplerCountEveryPeriods'].data.value = this.datasets['samplerCountEveryPeriods'][samplerName][lastIndex].y;
          stats['sampler'].data.value = samplerName.slice(key.indexOf('_')+1,key.length);
          stats['error'].data.value = (this.datasets['errorEveryPeriods'][samplerName][lastIndex].y / this.datasets['samplerCountEveryPeriods'][samplerName][lastIndex].y * 100).toFixed(3);
    
          Object.keys(this.datasets).filter(type => type.startsWith('pctEveryPeriods')).forEach(pct => {
            let percentileNumber = (pct.match(/\d/g) ?? ["0"]).join(""); // regex that get every numbers of a string
            stats['Percentile '+ percentileNumber + 'th'] = {
              label: this.translate.instant('metrics.percentile', {value: percentileNumber}),
                  data: {
                    unit: 'ms',
                    value: this.datasets[pct][samplerName][lastIndex].y
                  }
            }
          });
          if(this.columnsToDisplay.length==0){
            for (const [key, value] of Object.entries({...stats})){
              this.columnsToDisplay.push({key: key, label: value.label})
              this.columnsKeys.push(key)
            }
          }
          this.statLine.push(stats)
          
        }
      }
      this.dataSource = new MatTableDataSource(this.statLine) 
    }
  }
}
