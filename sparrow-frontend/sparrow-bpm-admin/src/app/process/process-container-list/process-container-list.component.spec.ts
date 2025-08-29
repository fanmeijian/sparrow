import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessContainerListComponent } from './process-container-list.component';

describe('ProcessContainerListComponent', () => {
  let component: ProcessContainerListComponent;
  let fixture: ComponentFixture<ProcessContainerListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessContainerListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessContainerListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
