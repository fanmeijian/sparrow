import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DslrFormComponent } from './dslr-form.component';

describe('DslrFormComponent', () => {
  let component: DslrFormComponent;
  let fixture: ComponentFixture<DslrFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DslrFormComponent]
    });
    fixture = TestBed.createComponent(DslrFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
