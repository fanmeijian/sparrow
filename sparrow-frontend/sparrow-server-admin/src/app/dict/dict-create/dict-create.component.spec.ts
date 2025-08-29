import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictCreateComponent } from './dict-create.component';

describe('DictCreateComponent', () => {
  let component: DictCreateComponent;
  let fixture: ComponentFixture<DictCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
