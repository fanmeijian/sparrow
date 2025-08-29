import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormDesignListComponent } from './form-design-list.component';

describe('FormDesignListComponent', () => {
  let component: FormDesignListComponent;
  let fixture: ComponentFixture<FormDesignListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FormDesignListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FormDesignListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
