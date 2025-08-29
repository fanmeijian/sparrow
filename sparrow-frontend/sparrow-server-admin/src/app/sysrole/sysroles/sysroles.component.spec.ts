import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysrolesComponent } from './sysroles.component';

describe('SysrolesComponent', () => {
  let component: SysrolesComponent;
  let fixture: ComponentFixture<SysrolesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysrolesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysrolesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
