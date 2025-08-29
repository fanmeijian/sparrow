import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RuleListComponent } from './rule-list.component';

describe('RuleListComponent', () => {
  let component: RuleListComponent;
  let fixture: ComponentFixture<RuleListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RuleListComponent]
    });
    fixture = TestBed.createComponent(RuleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
