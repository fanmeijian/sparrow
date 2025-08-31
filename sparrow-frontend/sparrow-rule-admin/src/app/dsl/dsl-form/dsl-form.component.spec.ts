import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DslFormComponent } from './dsl-form.component';

describe('DslFormComponent', () => {
  let component: DslFormComponent;
  let fixture: ComponentFixture<DslFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DslFormComponent]
    });
    fixture = TestBed.createComponent(DslFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
