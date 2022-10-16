import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { HttpClientModule } from '@angular/common/http';
import { UlpObservabilityChartComponent } from './components/ulp-observability-chart/ulp-observability-chart.component';
import { UlpObservabilityDashboardComponent } from './components/ulp-observability-dashboard/ulp-observability-dashboard.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';

import { FlexLayoutModule } from '@angular/flex-layout';
import { UlpObservabilityMetricsComponent } from './components/ulp-observability-metrics/ulp-observability-metrics.component';
import { UlpObservabilityStatisticsComponent } from './components/ulp-observability-statistics/ulp-observability-statistics.component';

const materialModules = [
  MatSelectModule,
  MatIconModule,
  MatToolbarModule,
  MatCardModule,
  MatTableModule
];

@NgModule({
  imports: [...materialModules],
  exports: [...materialModules],
})
export class MaterialModule {};

@NgModule({
  declarations: [
    AppComponent,
    UlpObservabilityChartComponent,
    UlpObservabilityDashboardComponent,
    UlpObservabilityMetricsComponent,
    UlpObservabilityStatisticsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgChartsModule,
    BrowserAnimationsModule,
    MaterialModule,
    FlexLayoutModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
