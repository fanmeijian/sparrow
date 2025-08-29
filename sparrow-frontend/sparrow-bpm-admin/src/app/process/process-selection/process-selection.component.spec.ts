import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessSelectionComponent } from './process-selection.component';

describe('ProcessSelectionComponent', () => {
  let component: ProcessSelectionComponent;
  let fixture: ComponentFixture<ProcessSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
