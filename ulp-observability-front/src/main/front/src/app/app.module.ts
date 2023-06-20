import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { UlpObservabilityChartComponent } from './components/ulp-observability-chart/ulp-observability-chart.component';
import { UlpObservabilityDashboardComponent } from './components/ulp-observability-dashboard/ulp-observability-dashboard.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatLegacyCardModule as MatCardModule } from '@angular/material/legacy-card';
import { MatLegacyTableModule as MatTableModule } from '@angular/material/legacy-table';
import {MatLegacyAutocompleteModule as MatAutocompleteModule} from '@angular/material/legacy-autocomplete';
import {ReactiveFormsModule} from '@angular/forms';
import {MatLegacyInputModule as MatInputModule} from '@angular/material/legacy-input'


import { FlexLayoutModule } from '@angular/flex-layout';
import { UlpObservabilityMetricsComponent } from './components/ulp-observability-metrics/ulp-observability-metrics.component';
import { UlpObservabilityStatisticsComponent } from './components/ulp-observability-statistics/ulp-observability-statistics.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import { UlpObservabilityTopErrorsComponent } from './components/ulp-observability-top-errors/ulp-observability-top-errors.component';

const materialModules = [
  MatSelectModule,
  MatIconModule,
  MatToolbarModule,
  MatCardModule,
  MatTableModule,
  MatAutocompleteModule,
  ReactiveFormsModule,
  MatInputModule,
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
    UlpObservabilityStatisticsComponent,
    UlpObservabilityTopErrorsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgChartsModule,
    BrowserAnimationsModule,
    MaterialModule,
    FlexLayoutModule,
    TranslateModule.forRoot({
      defaultLanguage: 'en',
      useDefaultLang: true,
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}