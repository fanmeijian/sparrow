import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelPermissionViewComponent } from './model-permission-view.component';

describe('ModelPermissionViewComponent', () => {
  let component: ModelPermissionViewComponent;
  let fixture: ComponentFixture<ModelPermissionViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModelPermissionViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelPermissionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
