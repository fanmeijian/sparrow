import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysconfigListComponent } from './sysconfig-list.component';

describe('SysconfigListComponent', () => {
  let component: SysconfigListComponent;
  let fixture: ComponentFixture<SysconfigListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysconfigListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysconfigListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
