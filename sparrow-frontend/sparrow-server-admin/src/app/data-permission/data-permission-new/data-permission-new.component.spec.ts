import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataPermissionNewComponent } from './data-permission-new.component';

describe('DataPermissionNewComponent', () => {
  let component: DataPermissionNewComponent;
  let fixture: ComponentFixture<DataPermissionNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataPermissionNewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DataPermissionNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
