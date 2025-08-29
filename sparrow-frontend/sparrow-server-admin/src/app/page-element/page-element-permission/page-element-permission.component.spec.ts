import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageElementPermissionComponent } from './page-element-permission.component';

describe('PageElementPermissionComponent', () => {
  let component: PageElementPermissionComponent;
  let fixture: ComponentFixture<PageElementPermissionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PageElementPermissionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PageElementPermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
