import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityStatisticsComponent } from './ulp-observability-statistics.component';

describe('UlpObservabilityStatisticsComponent', () => {
  let component: UlpObservabilityStatisticsComponent;
  let fixture: ComponentFixture<UlpObservabilityStatisticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityStatisticsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
