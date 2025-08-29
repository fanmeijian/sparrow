import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataPermissionGrantComponent } from './data-permission-grant.component';

describe('DataPermissionGrantComponent', () => {
  let component: DataPermissionGrantComponent;
  let fixture: ComponentFixture<DataPermissionGrantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DataPermissionGrantComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DataPermissionGrantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
