import { Component, OnInit } from '@angular/core';
import { MetricsRecord } from 'src/app/model/metrics_record';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { getMetricsData } from 'src/app/utility/metrics-parser';
import { seconds } from 'src/app/utility/time';
import parsePrometheusTextFormat from 'parse-prometheus-text-format';

@Component({
  selector: 'app-ulp-observability-dashboard',
  templateUrl: './ulp-observability-dashboard.component.html',
  styleUrls: ['./ulp-observability-dashboard.component.css']
})
export class UlpObservabilityDashboardComponent implements OnInit{

  metricsData: MetricsRecord[] = [];
  updateFrequencyS = 5;
  metricsBuffer = 60;
  stepSizeM = 1;

  constructor(private metricService: MetricsService) { }

  ngOnInit(): void {
    setInterval(()=>{
      this.metricService.getMetrics().subscribe({
        next: (metricsText) => {
          this.metricsData = getMetricsData(parsePrometheusTextFormat(metricsText));
         },
        error: () => {
          this.metricsData = [];
        }
      });
   }, seconds(this.updateFrequencyS))
  }

}


