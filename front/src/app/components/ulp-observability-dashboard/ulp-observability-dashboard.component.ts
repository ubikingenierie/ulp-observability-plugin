import { Component, OnInit } from '@angular/core';
import { MetricsRecord } from 'src/app/model/metrics_record';
import { MetricsService } from 'src/app/services/metrics/metrics.service';
import { getMetricsData } from 'src/app/utility/metrics-parser';
import { seconds } from 'src/app/utility/time';
import parsePrometheusTextFormat from 'parse-prometheus-text-format';
import { ULPObservabilityMetrics } from 'src/app/model/ulpobservability_metrics';

@Component({
  selector: 'app-ulp-observability-dashboard',
  templateUrl: './ulp-observability-dashboard.component.html',
  styleUrls: ['./ulp-observability-dashboard.component.css']
})
export class UlpObservabilityDashboardComponent implements OnInit{

  metricsData: ULPObservabilityMetrics = {
    sampleNames : new Set<string>(),
    request: [],
    response: []
  };

  sampleList = [];

  updateFrequencyS = 10;
  metricsBuffer = 60;
  stepSizeM = 1;

  constructor(private metricService: MetricsService) { }

  ngOnInit(): void {
    setInterval(()=>{
      this.metricService.getMetrics().subscribe({
        next: (metricsText) => {
          var data = parsePrometheusTextFormat(metricsText);
          this.metricsData = getMetricsData(data);
         },
        error: () => {
          this.metricsData.response = [];
        }
      });
   }, seconds(this.updateFrequencyS))
  }

}


