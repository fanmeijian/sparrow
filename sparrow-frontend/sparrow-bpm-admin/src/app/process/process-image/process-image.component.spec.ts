import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProcessImageComponent } from './process-image.component';

describe('ProcessImageComponent', () => {
  let component: ProcessImageComponent;
  let fixture: ComponentFixture<ProcessImageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessImageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessImageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
