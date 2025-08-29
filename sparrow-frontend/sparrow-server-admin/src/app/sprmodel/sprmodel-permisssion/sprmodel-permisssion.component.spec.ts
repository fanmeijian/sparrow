import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SprmodelPermisssionComponent } from './sprmodel-permisssion.component';

describe('SprmodelPermisssionComponent', () => {
  let component: SprmodelPermisssionComponent;
  let fixture: ComponentFixture<SprmodelPermisssionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SprmodelPermisssionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SprmodelPermisssionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
