import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
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


@Component({
  selector: 'app-ulp-observability-statistics',
  templateUrl: './ulp-observability-statistics.component.html',
  styleUrls: ['./ulp-observability-statistics.component.css']
})
export class UlpObservabilityStatisticsComponent implements OnChanges,OnInit {
  @Input() datasets : Datasets = {};
  @Input() threads : DatasetGroup = {};
  @Input() totalLabel = 'total_info';
  
  dataSource!: MatTableDataSource<StatList>;
  tableList!: String[];
  array !: Array<StatList>;

  columnsToDisplay !: Array<string>;
  
  constructor() { }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.array)

  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshStats();
  }
  
  refreshStats(){
    this.array = [];
    this.columnsToDisplay = [];
    if(this.threads !== undefined && this.datasets !== {}){
      for (const [key, value] of Object.entries({...this.threads})){
        
        if (key.startsWith('spl_')){
          const lastIndex = value.length - 1;
          var samplerName = key.slice(0, key.lastIndexOf('_'));

          var stats : StatList = {
            'sampler':{
              label:'Sampler Name',
              data:{
                unit:'',
                value:'0'
              }
            },
            'total': {
              label: 'Total Requests',
              data: {
                unit: '',
                value: '0'
              }
            },
            'avg': {
              label: 'Avg Response Time',
              data: {
                unit: 'ms',
                value: '0'
              }
            },
            'error': {
              label: 'Error Percentage',
              data: {
                unit: '%',
                value: '0'
              }
            },
            'max': {
              label: 'Max Response Time',
              data: {
                unit: 'ms',
                value: '0'
              }
            },
            'throughput': {
              label: 'Throughput',
              data: {
                unit: 'req/s',
                value: '0'
              }
            },
          };

          ['avg','max','total','throughput'].forEach(type =>{
            stats[type].data.value = this.datasets[type][samplerName][lastIndex].y;
          });
          stats['sampler'].data.value = samplerName;
          stats['error'].data.value = (this.datasets['error'][samplerName][lastIndex].y / this.datasets['period'][samplerName][lastIndex].y * 100).toFixed(3);
    
          Object.keys(this.datasets).filter(type => type.startsWith('pc')).forEach(pct => {
            stats['Percentile '+pct.substring(2) + 'th'] = {
              label: 'Percentile '+pct.substring(2) + 'th',
                  data: {
                    unit: 'ms',
                    value: this.datasets[pct][samplerName][lastIndex].y
                  }
            }
          });
          if(this.columnsToDisplay.length==0){
            for (const [key, value] of Object.entries({...stats})){
              this.columnsToDisplay.push(key)
            }
          }
          this.array.push(stats)
        }
      }     
    }
  }
}
