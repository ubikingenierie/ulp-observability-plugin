import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UlpObservabilityTopErrorsComponent } from './ulp-observability-top-errors.component';

describe('TopErrorsComponent', () => {
  let component: UlpObservabilityTopErrorsComponent;
  let fixture: ComponentFixture<UlpObservabilityTopErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UlpObservabilityTopErrorsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UlpObservabilityTopErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
