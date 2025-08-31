import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScopePermissionComponent } from './scope-permission.component';

describe('ScopePermissionComponent', () => {
  let component: ScopePermissionComponent;
  let fixture: ComponentFixture<ScopePermissionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScopePermissionComponent]
    });
    fixture = TestBed.createComponent(ScopePermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
