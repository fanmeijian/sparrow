import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DslrListComponent } from './dslr-list.component';

describe('DslrListComponent', () => {
  let component: DslrListComponent;
  let fixture: ComponentFixture<DslrListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DslrListComponent]
    });
    fixture = TestBed.createComponent(DslrListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
