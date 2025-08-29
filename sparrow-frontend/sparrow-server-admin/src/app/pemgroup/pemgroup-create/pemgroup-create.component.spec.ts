import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PemgroupCreateComponent } from './pemgroup-create.component';

describe('PemgroupCreateComponent', () => {
  let component: PemgroupCreateComponent;
  let fixture: ComponentFixture<PemgroupCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PemgroupCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PemgroupCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
