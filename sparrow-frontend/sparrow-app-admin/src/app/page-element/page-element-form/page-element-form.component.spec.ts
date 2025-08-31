import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageElementFormComponent } from './page-element-form.component';

describe('PageElementFormComponent', () => {
  let component: PageElementFormComponent;
  let fixture: ComponentFixture<PageElementFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PageElementFormComponent]
    });
    fixture = TestBed.createComponent(PageElementFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
