import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysrolePermissionComponent } from './sysrole-permission.component';

describe('SysrolePermissionComponent', () => {
  let component: SysrolePermissionComponent;
  let fixture: ComponentFixture<SysrolePermissionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysrolePermissionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysrolePermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
