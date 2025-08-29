import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessInstanceImageComponent } from './process-instance-image.component';

describe('ProcessInstanceImageComponent', () => {
  let component: ProcessInstanceImageComponent;
  let fixture: ComponentFixture<ProcessInstanceImageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessInstanceImageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessInstanceImageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
