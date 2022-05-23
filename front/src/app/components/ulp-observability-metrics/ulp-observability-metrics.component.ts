import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Datasets } from 'src/app/model/chart-data';

@Component({
  selector: 'app-ulp-observability-metrics',
  templateUrl: './ulp-observability-metrics.component.html',
  styleUrls: ['./ulp-observability-metrics.component.css']
})
export class UlpObservabilityMetricsComponent implements OnChanges, OnInit {

  @Input() datasets: Datasets = {};
  
  samples : Array<string> = [];
  selected : string = '';
  
  constructor() { }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {

  }

}
