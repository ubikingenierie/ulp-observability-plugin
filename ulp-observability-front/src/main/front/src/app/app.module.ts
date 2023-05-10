import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { UlpObservabilityChartComponent } from './components/ulp-observability-chart/ulp-observability-chart.component';
import { UlpObservabilityDashboardComponent } from './components/ulp-observability-dashboard/ulp-observability-dashboard.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input'


import { FlexLayoutModule } from '@angular/flex-layout';
import { UlpObservabilityMetricsComponent } from './components/ulp-observability-metrics/ulp-observability-metrics.component';
import { UlpObservabilityStatisticsComponent } from './components/ulp-observability-statistics/ulp-observability-statistics.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

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
    UlpObservabilityStatisticsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgChartsModule,
    BrowserAnimationsModule,
    MaterialModule,
    FlexLayoutModule,
    TranslateModule.forRoot({
      defaultLanguage: 'fr',
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