import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictListComponent } from './dict-list.component';

describe('DictListComponent', () => {
  let component: DictListComponent;
  let fixture: ComponentFixture<DictListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DictListComponent]
    });
    fixture = TestBed.createComponent(DictListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
