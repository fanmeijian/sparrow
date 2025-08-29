import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysconfigFormComponent } from './sysconfig-form.component';

describe('SysconfigFormComponent', () => {
  let component: SysconfigFormComponent;
  let fixture: ComponentFixture<SysconfigFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysconfigFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysconfigFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
