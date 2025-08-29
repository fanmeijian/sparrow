import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewTreeComponent } from './new-tree.component';

describe('NewTreeComponent', () => {
  let component: NewTreeComponent;
  let fixture: ComponentFixture<NewTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewTreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NewTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
