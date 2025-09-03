import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DslPreviewComponent } from './dsl-preview.component';

describe('DslPreviewComponent', () => {
  let component: DslPreviewComponent;
  let fixture: ComponentFixture<DslPreviewComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DslPreviewComponent]
    });
    fixture = TestBed.createComponent(DslPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
