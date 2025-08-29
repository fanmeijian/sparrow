import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleTemplateFormComponent } from './rule-template-form.component';

describe('RuleTemplateFormComponent', () => {
  let component: RuleTemplateFormComponent;
  let fixture: ComponentFixture<RuleTemplateFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RuleTemplateFormComponent]
    });
    fixture = TestBed.createComponent(RuleTemplateFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
