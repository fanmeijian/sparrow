import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysconfigDesignComponent } from './sysconfig-design.component';

describe('SysconfigDesignComponent', () => {
  let component: SysconfigDesignComponent;
  let fixture: ComponentFixture<SysconfigDesignComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysconfigDesignComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysconfigDesignComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
