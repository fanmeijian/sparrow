import { ComponentFixture, TestBed } from '@angular/core/testing';

import { L2routeComponent } from './l2route.component';

describe('L2routeComponent', () => {
  let component: L2routeComponent;
  let fixture: ComponentFixture<L2routeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ L2routeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(L2routeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
