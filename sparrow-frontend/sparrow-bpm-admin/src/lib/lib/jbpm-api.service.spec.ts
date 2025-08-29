import { TestBed } from '@angular/core/testing';

import { JbpmApiService } from './jbpm-api.service';

describe('JbpmApiService', () => {
  let service: JbpmApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JbpmApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
