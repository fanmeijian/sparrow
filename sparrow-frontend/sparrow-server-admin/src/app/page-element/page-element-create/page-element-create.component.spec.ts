import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageElementCreateComponent } from './page-element-create.component';

describe('PageElementCreateComponent', () => {
  let component: PageElementCreateComponent;
  let fixture: ComponentFixture<PageElementCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PageElementCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PageElementCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
