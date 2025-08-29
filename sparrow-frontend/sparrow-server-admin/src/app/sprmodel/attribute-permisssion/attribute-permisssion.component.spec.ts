import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributePermisssionComponent } from './attribute-permisssion.component';

describe('AttributePermisssionComponent', () => {
  let component: AttributePermisssionComponent;
  let fixture: ComponentFixture<AttributePermisssionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AttributePermisssionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributePermisssionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
