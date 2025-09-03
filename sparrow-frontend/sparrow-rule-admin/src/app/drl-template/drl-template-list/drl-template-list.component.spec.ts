import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DrlTemplateListComponent } from './drl-template-list.component';

describe('DrlTemplateListComponent', () => {
  let component: DrlTemplateListComponent;
  let fixture: ComponentFixture<DrlTemplateListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DrlTemplateListComponent]
    });
    fixture = TestBed.createComponent(DrlTemplateListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
