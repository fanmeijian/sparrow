import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlTemplateRuleFormComponent } from './drl-template-rule-form.component';

describe('DrlTemplateRuleFormComponent', () => {
  let component: DrlTemplateRuleFormComponent;
  let fixture: ComponentFixture<DrlTemplateRuleFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DrlTemplateRuleFormComponent]
    });
    fixture = TestBed.createComponent(DrlTemplateRuleFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
