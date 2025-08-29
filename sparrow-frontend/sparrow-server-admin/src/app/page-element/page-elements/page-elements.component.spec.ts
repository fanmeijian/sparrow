import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageElementsComponent } from './page-elements.component';

describe('PageElementsComponent', () => {
  let component: PageElementsComponent;
  let fixture: ComponentFixture<PageElementsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PageElementsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PageElementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
