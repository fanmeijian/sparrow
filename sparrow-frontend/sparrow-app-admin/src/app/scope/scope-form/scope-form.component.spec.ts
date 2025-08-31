import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScopeFormComponent } from './scope-form.component';

describe('ScopeFormComponent', () => {
  let component: ScopeFormComponent;
  let fixture: ComponentFixture<ScopeFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScopeFormComponent]
    });
    fixture = TestBed.createComponent(ScopeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
