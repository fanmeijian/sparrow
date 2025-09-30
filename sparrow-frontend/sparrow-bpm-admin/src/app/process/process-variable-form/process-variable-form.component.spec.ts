import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessVariableFormComponent } from './process-variable-form.component';

describe('ProcessVariableFormComponent', () => {
  let component: ProcessVariableFormComponent;
  let fixture: ComponentFixture<ProcessVariableFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ProcessVariableFormComponent]
    });
    fixture = TestBed.createComponent(ProcessVariableFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
