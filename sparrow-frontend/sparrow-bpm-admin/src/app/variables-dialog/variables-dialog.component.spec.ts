import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VariablesDialogComponent } from './variables-dialog.component';

describe('VariablesDialogComponent', () => {
  let component: VariablesDialogComponent;
  let fixture: ComponentFixture<VariablesDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VariablesDialogComponent]
    });
    fixture = TestBed.createComponent(VariablesDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
