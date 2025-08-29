import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GristerTestComponent } from './grister-test.component';

describe('GristerTestComponent', () => {
  let component: GristerTestComponent;
  let fixture: ComponentFixture<GristerTestComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GristerTestComponent]
    });
    fixture = TestBed.createComponent(GristerTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
