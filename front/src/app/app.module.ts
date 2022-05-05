import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { HttpClientModule } from '@angular/common/http';
import { UlpObservabilityChartComponent } from './components/ulp-observability-chart/ulp-observability-chart.component';
import { UlpObservabilityDashboardComponent } from './components/ulp-observability-dashboard/ulp-observability-dashboard.component';
import { UlpObservabilityChartSamplesComponent } from './components/ulp-observability-chart-samples/ulp-observability-chart-samples.component';
import { UlpObservabilityChartTotalComponent } from './components/ulp-observability-chart-total/ulp-observability-chart-total.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import {MatSelectModule} from '@angular/material/select';

@NgModule({
  declarations: [
    AppComponent,
    UlpObservabilityChartComponent,
    UlpObservabilityDashboardComponent,
    UlpObservabilityChartSamplesComponent,
    UlpObservabilityChartTotalComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgChartsModule,
    BrowserAnimationsModule,
    MatSelectModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
