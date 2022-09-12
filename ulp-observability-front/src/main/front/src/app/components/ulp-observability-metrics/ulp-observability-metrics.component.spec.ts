import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityMetricsComponent } from './ulp-observability-metrics.component';

describe('UlpObservabilityMetricsComponent', () => {
  let component: UlpObservabilityMetricsComponent;
  let fixture: ComponentFixture<UlpObservabilityMetricsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityMetricsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityMetricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
