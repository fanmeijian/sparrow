import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictSelectionComponent } from './dict-selection.component';

describe('DictSelectionComponent', () => {
  let component: DictSelectionComponent;
  let fixture: ComponentFixture<DictSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
