import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlFormComponent } from './drl-form.component';

describe('DrlFormComponent', () => {
  let component: DrlFormComponent;
  let fixture: ComponentFixture<DrlFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DrlFormComponent]
    });
    fixture = TestBed.createComponent(DrlFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
