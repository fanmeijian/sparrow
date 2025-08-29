import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleTemplateListComponent } from './rule-template-list.component';

describe('RuleTemplateListComponent', () => {
  let component: RuleTemplateListComponent;
  let fixture: ComponentFixture<RuleTemplateListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RuleTemplateListComponent]
    });
    fixture = TestBed.createComponent(RuleTemplateListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
