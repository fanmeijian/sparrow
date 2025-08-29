import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GristerFieldTypeComponent } from './grister-field-type.component';

describe('GristerFieldTypeComponent', () => {
  let component: GristerFieldTypeComponent;
  let fixture: ComponentFixture<GristerFieldTypeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GristerFieldTypeComponent]
    });
    fixture = TestBed.createComponent(GristerFieldTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
