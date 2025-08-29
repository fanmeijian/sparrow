import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessGlobalComponent } from './process-global.component';

describe('ProcessGlobalComponent', () => {
  let component: ProcessGlobalComponent;
  let fixture: ComponentFixture<ProcessGlobalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ProcessGlobalComponent]
    });
    fixture = TestBed.createComponent(ProcessGlobalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
