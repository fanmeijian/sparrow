import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelPermissionRulesComponent } from './model-permission-rules.component';

describe('ModelPermissionRulesComponent', () => {
  let component: ModelPermissionRulesComponent;
  let fixture: ComponentFixture<ModelPermissionRulesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModelPermissionRulesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelPermissionRulesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
