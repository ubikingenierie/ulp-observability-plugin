import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { UlpObservabilityChartComponent } from './components/ulp-observability-chart/ulp-observability-chart.component';
import { UlpObservabilityDashboardComponent } from './components/ulp-observability-dashboard/ulp-observability-dashboard.component';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent,
    UlpObservabilityChartComponent,
    UlpObservabilityDashboardComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgChartsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
