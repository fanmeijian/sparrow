import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysroleSelectionComponent } from './sysrole-selection.component';

describe('SysroleSelectionComponent', () => {
  let component: SysroleSelectionComponent;
  let fixture: ComponentFixture<SysroleSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysroleSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysroleSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
