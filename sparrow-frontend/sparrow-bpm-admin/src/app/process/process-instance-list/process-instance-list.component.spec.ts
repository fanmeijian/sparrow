import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { ProcessInstanceAdministrationService, ProcessInstancesService, ProcessQueriesService } from '../../../lib';
import { environment } from 'src/environments/environment';

import { ProcessInstanceListComponent } from './process-instance-list.component';

describe('ProcessInstanceListComponent', () => {
  let component: ProcessInstanceListComponent;
  let fixture: ComponentFixture<ProcessInstanceListComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProcessInstanceListComponent ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { firstChild: null } },
        { provide: ProcessQueriesService, useValue: {} },
        { provide: ProcessInstancesService, useValue: {} },
        { provide: ProcessInstanceAdministrationService, useValue: {} },
      ],
    })
    .overrideComponent(ProcessInstanceListComponent, {
      set: {
        template: `
          @for (row of dataSource.data; track row.processInstanceId) {
            <div class="process">{{ row.processName }}</div>
          }
          <span class="total">{{ pageable.length }}</span>
        `,
      },
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProcessInstanceListComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.autoDetectChanges();
  });

  afterEach(() => http.verify());

  it('renders the asynchronous response and total without another user event', async () => {
    http.expectOne(req => req.url === `${environment.bpmApi}/process-instances`).flush({
      content: [{ processInstanceId: 1, processName: '审批流程' }],
      totalElements: 12,
    });

    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.process').textContent).toContain('审批流程');
    expect(fixture.nativeElement.querySelector('.total').textContent).toBe('12');

    component.onPage({ pageIndex: 1, pageSize: 10 });
    http.expectOne(req => req.url === `${environment.bpmApi}/process-instances`
      && req.params.get('page') === '1').flush({
      content: [{ processInstanceId: 2, processName: '下一页流程' }],
      totalElements: 12,
    });

    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.process').textContent).toContain('下一页流程');
  });
});
