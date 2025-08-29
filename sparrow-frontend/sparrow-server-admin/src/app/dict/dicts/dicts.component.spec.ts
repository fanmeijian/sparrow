import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictsComponent } from './dicts.component';

describe('DictsComponent', () => {
  let component: DictsComponent;
  let fixture: ComponentFixture<DictsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
