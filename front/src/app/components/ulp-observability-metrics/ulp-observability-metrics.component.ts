import { Component, OnInit } from '@angular/core';


interface CardInfo{
  label: string,
  icon: string,
  data: {
    value: number,
    unit: string,
    comment?: string 
  }
}
@Component({
  selector: 'app-ulp-observability-metrics',
  templateUrl: './ulp-observability-metrics.component.html',
  styleUrls: ['./ulp-observability-metrics.component.css']
})
export class UlpObservabilityMetricsComponent implements OnInit {

  cards : Array<CardInfo> = [
    {
      label: 'Total Requests',
      icon: 'rocket_launch',
      data: {
        value: 15,
        unit: ''
      }
    },
    {
      label: 'Error Percentage',
      icon: 'warning',
      data: {
        value: 15,
        unit: '%',
        comment: '(15/100)'
      }
    },
]

  constructor() { }

  ngOnInit(): void {
  }

}
