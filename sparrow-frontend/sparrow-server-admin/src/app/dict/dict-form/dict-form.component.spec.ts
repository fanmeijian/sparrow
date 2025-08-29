import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictFormComponent } from './dict-form.component';

describe('DictFormComponent', () => {
  let component: DictFormComponent;
  let fixture: ComponentFixture<DictFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
