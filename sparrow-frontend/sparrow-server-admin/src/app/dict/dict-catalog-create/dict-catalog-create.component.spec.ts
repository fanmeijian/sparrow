import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DictCatalogCreateComponent } from './dict-catalog-create.component';

describe('DictCatalogCreateComponent', () => {
  let component: DictCatalogCreateComponent;
  let fixture: ComponentFixture<DictCatalogCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DictCatalogCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DictCatalogCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
