import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityChartComponent } from './ulp-observability-chart.component';

describe('UlpObservabilityChartComponent', () => {
  let component: UlpObservabilityChartComponent;
  let fixture: ComponentFixture<UlpObservabilityChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
