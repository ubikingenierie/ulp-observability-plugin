import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityChartTotalComponent } from './ulp-observability-chart-total.component';

describe('UlpObservabilityChartTotalComponent', () => {
  let component: UlpObservabilityChartTotalComponent;
  let fixture: ComponentFixture<UlpObservabilityChartTotalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityChartTotalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityChartTotalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
