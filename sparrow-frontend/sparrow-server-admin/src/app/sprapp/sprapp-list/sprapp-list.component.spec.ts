import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SprappListComponent } from './sprapp-list.component';

describe('SprappListComponent', () => {
  let component: SprappListComponent;
  let fixture: ComponentFixture<SprappListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SprappListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SprappListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
