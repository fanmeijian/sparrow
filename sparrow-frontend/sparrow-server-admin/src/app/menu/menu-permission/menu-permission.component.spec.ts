import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuPermissionComponent } from './menu-permission.component';

describe('MenuPermissionComponent', () => {
  let component: MenuPermissionComponent;
  let fixture: ComponentFixture<MenuPermissionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MenuPermissionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MenuPermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
