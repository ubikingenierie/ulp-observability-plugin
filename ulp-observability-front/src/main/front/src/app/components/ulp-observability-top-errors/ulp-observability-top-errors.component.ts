import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Datasets, DatasetGroup } from 'src/app/model/chart-data';


export interface RaisedError {
  code?: string,
  count: string
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

  topErrors!: Array<RaisedError>;
  
  constructor(private translate: TranslateService) { }

  ngOnInit(): void {
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
      for (const [key, value] of Object.entries({...this.threads})) {
        // The top errors are passed to the total label sampler
        if (key.startsWith(this.totalLabel)) {
          const lastIndex = value.length - 1;
          var samplerName = key.slice(0, key.lastIndexOf('_'))

          Object.keys(this.datasets).filter(type => type.startsWith('errorEveryPeriods_')).forEach(errorType => {
            var raisedError: RaisedError = {
              code: errorType.slice(errorType.lastIndexOf("_"), errorType.length),
              count: this.datasets[errorType][samplerName][lastIndex].y
            }
            this.topErrors.push(raisedError);
          })
      }
    }
  }}

}
