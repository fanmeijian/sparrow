import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScopeCreateComponent } from './scope-create.component';

describe('ScopeCreateComponent', () => {
  let component: ScopeCreateComponent;
  let fixture: ComponentFixture<ScopeCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScopeCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScopeCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
