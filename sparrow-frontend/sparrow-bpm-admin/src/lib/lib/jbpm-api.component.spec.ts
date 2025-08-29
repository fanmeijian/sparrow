import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JbpmApiComponent } from './jbpm-api.component';

describe('JbpmApiComponent', () => {
  let component: JbpmApiComponent;
  let fixture: ComponentFixture<JbpmApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JbpmApiComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JbpmApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
