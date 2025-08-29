import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SprmodesComponent } from './sprmodes.component';

describe('SprmodesComponent', () => {
  let component: SprmodesComponent;
  let fixture: ComponentFixture<SprmodesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SprmodesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SprmodesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
