import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlTemplateFormComponent } from './drl-template-form.component';

describe('DrlTemplateFormComponent', () => {
  let component: DrlTemplateFormComponent;
  let fixture: ComponentFixture<DrlTemplateFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DrlTemplateFormComponent]
    });
    fixture = TestBed.createComponent(DrlTemplateFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
