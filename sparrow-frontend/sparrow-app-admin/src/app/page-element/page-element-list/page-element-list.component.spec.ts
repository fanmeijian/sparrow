import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageElementListComponent } from './page-element-list.component';

describe('PageElementListComponent', () => {
  let component: PageElementListComponent;
  let fixture: ComponentFixture<PageElementListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PageElementListComponent]
    });
    fixture = TestBed.createComponent(PageElementListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
