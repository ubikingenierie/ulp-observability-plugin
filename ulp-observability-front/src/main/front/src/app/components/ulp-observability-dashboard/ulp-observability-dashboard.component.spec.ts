import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityDashboardComponent } from './ulp-observability-dashboard.component';

describe('UlpObservabilityDashboardComponent', () => {
  let component: UlpObservabilityDashboardComponent;
  let fixture: ComponentFixture<UlpObservabilityDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
