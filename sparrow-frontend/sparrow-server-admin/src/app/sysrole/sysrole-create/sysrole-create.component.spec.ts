import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SysroleCreateComponent } from './sysrole-create.component';

describe('SysroleCreateComponent', () => {
  let component: SysroleCreateComponent;
  let fixture: ComponentFixture<SysroleCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SysroleCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SysroleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
