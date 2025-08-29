import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PemgroupMemberComponent } from './pemgroup-member.component';

describe('PemgroupMemberComponent', () => {
  let component: PemgroupMemberComponent;
  let fixture: ComponentFixture<PemgroupMemberComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PemgroupMemberComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PemgroupMemberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
