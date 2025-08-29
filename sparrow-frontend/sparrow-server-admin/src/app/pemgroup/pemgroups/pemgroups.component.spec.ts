import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PemgroupsComponent } from './pemgroups.component';

describe('PemgroupsComponent', () => {
  let component: PemgroupsComponent;
  let fixture: ComponentFixture<PemgroupsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PemgroupsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PemgroupsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
