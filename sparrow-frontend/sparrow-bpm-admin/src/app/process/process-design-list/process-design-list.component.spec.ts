import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessDesignListComponent } from './process-design-list.component';

describe('ProcessDesignListComponent', () => {
  let component: ProcessDesignListComponent;
  let fixture: ComponentFixture<ProcessDesignListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessDesignListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessDesignListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
