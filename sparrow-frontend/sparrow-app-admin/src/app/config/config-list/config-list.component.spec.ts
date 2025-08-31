import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigListComponent } from './config-list.component';

describe('ConfigListComponent', () => {
  let component: ConfigListComponent;
  let fixture: ComponentFixture<ConfigListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ConfigListComponent]
    });
    fixture = TestBed.createComponent(ConfigListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
