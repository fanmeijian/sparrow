import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataPermissionViewComponent } from './data-permission-view.component';

describe('DataPermissionViewComponent', () => {
  let component: DataPermissionViewComponent;
  let fixture: ComponentFixture<DataPermissionViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataPermissionViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DataPermissionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
