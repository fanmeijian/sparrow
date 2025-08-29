import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictCatalogSelectionComponent } from './dict-catalog-selection.component';

describe('DictCatalogSelectionComponent', () => {
  let component: DictCatalogSelectionComponent;
  let fixture: ComponentFixture<DictCatalogSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictCatalogSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictCatalogSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
