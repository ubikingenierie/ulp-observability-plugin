import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { TranslateService } from '@ngx-translate/core';
import { Datasets, DatasetGroup } from 'src/app/model/chart-data';


interface ErrorTypeInfo {
  type: string,
  occurrence: number,
  errorRate: number,
  errorFrequency: number
}

@Component({
  selector: 'app-ulp-observability-top-errors',
  templateUrl: './ulp-observability-top-errors.component.html',
  styleUrls: ['./ulp-observability-top-errors.component.css']
})
export class UlpObservabilityTopErrorsComponent implements OnChanges, OnInit {
  @Input() datasets : Datasets = {};
  @Input() totalLabel : string = "total_info";
  @Input() numberTopErrors = 10;
  numberTopErrorsI18n = {value: this.numberTopErrors};

  topErrors: ErrorTypeInfo[] = [];
  displayedColumns: string[] = ['type', 'occurrence', 'errorRate', 'errorFreq'];
  errorsData!: MatTableDataSource<ErrorTypeInfo>;
  
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
    
    if (this.datasets !== undefined) {
      // The top errors are passed to the total label sampler
      let currentErrorType = "";
      let occurrence = 0;
      let errorFreq = 0;
      let errorRate = 0;

      Object.keys(this.datasets).filter(metricType => metricType.startsWith('errorEveryPeriods_')).forEach(metric => {
        let indexLastValue =  this.datasets[metric][this.totalLabel].length - 1;
        let errorType = metric.slice(metric.indexOf("_")+1, metric.lastIndexOf("_"));

        var errorTypeInfo: ErrorTypeInfo = {
          type: currentErrorType,
          occurrence: occurrence,
          errorRate: errorRate, 
          errorFrequency: errorFreq
        }

        if (currentErrorType !== errorType) {
          if (currentErrorType !== "") {
            this.topErrors.push(errorTypeInfo);
          }

          if (metric.endsWith("occurrence")) {
            occurrence = this.datasets[metric][this.totalLabel][indexLastValue].y;
          } else if (metric.endsWith("errorRate")) {
            errorRate = this.datasets[metric][this.totalLabel][indexLastValue].y;
          } else if (metric.endsWith("errorFreq")) {
            errorFreq = this.datasets[metric][this.totalLabel][indexLastValue].y;
          }

          currentErrorType = errorType;
        } else {
          if (metric.endsWith("occurrence")) {
            occurrence = this.datasets[metric][this.totalLabel][indexLastValue].y;
          } else if (metric.endsWith("errorRate")) {
            errorRate = this.datasets[metric][this.totalLabel][indexLastValue].y;
          } else if (metric.endsWith("errorFreq")) {
            errorFreq = this.datasets[metric][this.totalLabel][indexLastValue].y;
          }
        }   
      })
    }
    this.errorsData.data = this.topErrors;
  }
}

