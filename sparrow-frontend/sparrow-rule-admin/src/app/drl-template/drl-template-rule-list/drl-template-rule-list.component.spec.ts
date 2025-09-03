import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlTemplateRuleListComponent } from './drl-template-rule-list.component';

describe('DrlTemplateRuleListComponent', () => {
  let component: DrlTemplateRuleListComponent;
  let fixture: ComponentFixture<DrlTemplateRuleListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DrlTemplateRuleListComponent]
    });
    fixture = TestBed.createComponent(DrlTemplateRuleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
