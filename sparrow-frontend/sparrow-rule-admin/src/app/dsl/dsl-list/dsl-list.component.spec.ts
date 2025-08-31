import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DslListComponent } from './dsl-list.component';

describe('DslListComponent', () => {
  let component: DslListComponent;
  let fixture: ComponentFixture<DslListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DslListComponent]
    });
    fixture = TestBed.createComponent(DslListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
