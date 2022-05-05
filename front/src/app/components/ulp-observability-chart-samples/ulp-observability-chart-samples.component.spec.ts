import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityChartSamplesComponent } from './ulp-observability-chart-samples.component';

describe('UlpObservabilityChartSamplesComponent', () => {
  let component: UlpObservabilityChartSamplesComponent;
  let fixture: ComponentFixture<UlpObservabilityChartSamplesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityChartSamplesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityChartSamplesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
