import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlListComponent } from './drl-list.component';

describe('DrlListComponent', () => {
  let component: DrlListComponent;
  let fixture: ComponentFixture<DrlListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DrlListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DrlListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
