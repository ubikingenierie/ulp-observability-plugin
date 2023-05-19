import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { TranslateService } from '@ngx-translate/core';
import { Datasets, DatasetGroup } from 'src/app/model/chart-data';


interface RaisedError {
  code?: string,
  count: string,
  perThreads: number,
}

interface KeyAndLabel {
  key: string,
  label: string
}

@Component({
  selector: 'app-ulp-observability-top-errors',
  templateUrl: './ulp-observability-top-errors.component.html',
  styleUrls: ['./ulp-observability-top-errors.component.css']
})
export class UlpObservabilityTopErrorsComponent implements OnChanges, OnInit {
  @Input() datasets : Datasets = {};
  @Input() threads : DatasetGroup = {};
  @Input() totalLabel = 'total_info';
  @Input() numberTopErrors = 10;
  numberTopErrorsI18n = {value: this.numberTopErrors};

  topErrors: RaisedError[] = [];
  displayedColumns: string[] = ['code', 'count', 'perThreads'];
  errorsData!: MatTableDataSource<RaisedError>;
  
  constructor(private translate: TranslateService) { }

  ngOnInit(): void {
    this.errorsData = new MatTableDataSource(this.topErrors);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshTopErrors();
  }

  /**
   * Refresh the content of the table containing the top errors
   */
  refreshTopErrors() {
    this.topErrors = [];

    if(this.threads !== undefined) {
      const threadsNo = this.totalNumberThreads();
      for (const [key, value] of Object.entries({...this.threads})) {
        // The top errors are passed to the total label sampler
        if (key.startsWith(this.totalLabel)) {
          const lastIndex = value.length - 1;
          var samplerName = key.slice(0, key.lastIndexOf('_'))

          Object.keys(this.datasets).filter(type => type.startsWith('errorEveryPeriods_')).forEach(errorType => {
            var raisedError: RaisedError = {
              code: errorType.slice(errorType.lastIndexOf("_")+1, errorType.length),
              count: this.datasets[errorType][samplerName][lastIndex].y,
              perThreads: Number.parseInt(this.datasets[errorType][samplerName][lastIndex].y) / threadsNo * 100
            }
            this.topErrors.push(raisedError);
          })
        }
      }
    }

    this.errorsData.data = this.topErrors;
  }

  totalNumberThreads() {
    let totalThreads = 0;
    for (const [_, values] of Object.entries({...this.threads})) {
      const lastIndex = values.length - 1;
      totalThreads += Number.parseInt(values[lastIndex].y);
    }
    return totalThreads
  }
}

