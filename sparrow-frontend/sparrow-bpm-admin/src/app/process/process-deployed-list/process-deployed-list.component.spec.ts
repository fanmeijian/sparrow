import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessDeployedListComponent } from './process-deployed-list.component';

describe('ProcessDeployedListComponent', () => {
  let component: ProcessDeployedListComponent;
  let fixture: ComponentFixture<ProcessDeployedListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessDeployedListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessDeployedListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
