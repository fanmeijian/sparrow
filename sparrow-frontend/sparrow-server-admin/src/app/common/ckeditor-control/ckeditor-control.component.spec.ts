import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CkeditorControlComponent } from './ckeditor-control.component';

describe('CkeditorControlComponent', () => {
  let component: CkeditorControlComponent;
  let fixture: ComponentFixture<CkeditorControlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CkeditorControlComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CkeditorControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
