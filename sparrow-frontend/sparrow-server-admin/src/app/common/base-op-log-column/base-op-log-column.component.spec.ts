import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseOpLogColumnComponent } from './base-op-log-column.component';

describe('BaseOpLogColumnComponent', () => {
  let component: BaseOpLogColumnComponent;
  let fixture: ComponentFixture<BaseOpLogColumnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BaseOpLogColumnComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BaseOpLogColumnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
