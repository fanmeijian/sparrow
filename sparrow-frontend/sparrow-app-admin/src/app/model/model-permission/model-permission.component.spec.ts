import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelPermissionComponent } from './model-permission.component';

describe('ModelPermissionComponent', () => {
  let component: ModelPermissionComponent;
  let fixture: ComponentFixture<ModelPermissionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ModelPermissionComponent]
    });
    fixture = TestBed.createComponent(ModelPermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
