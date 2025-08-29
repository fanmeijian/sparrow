import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessInstanceListComponent } from './process-instance-list.component';

describe('ProcessInstanceListComponent', () => {
  let component: ProcessInstanceListComponent;
  let fixture: ComponentFixture<ProcessInstanceListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessInstanceListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessInstanceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
