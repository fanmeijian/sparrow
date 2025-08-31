import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictFormComponent } from './dict-form.component';

describe('DictFormComponent', () => {
  let component: DictFormComponent;
  let fixture: ComponentFixture<DictFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DictFormComponent]
    });
    fixture = TestBed.createComponent(DictFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
