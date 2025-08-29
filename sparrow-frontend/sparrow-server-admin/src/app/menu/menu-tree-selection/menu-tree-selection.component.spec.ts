import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuTreeSelectionComponent } from './menu-tree-selection.component';

describe('MenuTreeSelectionComponent', () => {
  let component: MenuTreeSelectionComponent;
  let fixture: ComponentFixture<MenuTreeSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MenuTreeSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MenuTreeSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
