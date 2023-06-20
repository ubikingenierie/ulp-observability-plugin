import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
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
  @Input() totalLabel! : string;
  @Input() numberTopErrors!: number;
  numberTopErrorsI18n = {value: 5};

  topErrors: ErrorTypeInfo[] = [];
  displayedColumns: string[] = ['type', 'occurrence', 'errorRate', 'errorFreq'];
  errorsData!: MatTableDataSource<ErrorTypeInfo>;
  
  constructor() { }

  ngOnInit(): void {
    this.errorsData = new MatTableDataSource(this.topErrors);
    this.numberTopErrorsI18n = {value: this.numberTopErrors};
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
      let currentErrorTypeInfo: ErrorTypeInfo = {
        type: "",
        occurrence: 0,
        errorRate: 0,
        errorFrequency: 0
      };

      Object.keys(this.datasets).filter(metricType => metricType.startsWith('errorEveryPeriods_')).forEach(metric => {
        let indexLastValue = this.datasets[metric][this.totalLabel].length - 1;
        let errorType = metric.slice(metric.indexOf("_")+1, metric.lastIndexOf("_"));
        let metricProperty = metric.slice(metric.lastIndexOf("_")+1);

        if (currentErrorTypeInfo.type !== errorType) {
          if (currentErrorTypeInfo.type !== "") {
            this.topErrors.push(currentErrorTypeInfo);
          }
          // The ErrorTypeInfo object is only created once for each error type
          currentErrorTypeInfo = {
            type: errorType,
            occurrence: 0,
            errorRate: 0,
            errorFrequency: 0
          };
        }

        switch(metricProperty) {
          case "occurrence":
            currentErrorTypeInfo.occurrence = this.datasets[metric][this.totalLabel][indexLastValue].y;
            break;
          case "errorRate":
            currentErrorTypeInfo.errorRate = this.datasets[metric][this.totalLabel][indexLastValue].y;
            break;
          case "errorFreq":
            currentErrorTypeInfo.errorFrequency = this.datasets[metric][this.totalLabel][indexLastValue].y;
            break;
        }
      })

      // push the last ErrorTypeInfo object if it was not pushed before the loop ended
      if (currentErrorTypeInfo.type !== "") {
        this.topErrors.push(currentErrorTypeInfo);
      }
    }
    this.errorsData = new MatTableDataSource(this.topErrors);
  }

}

