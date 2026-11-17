import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { ProcessInstanceImageComponent } from './process-instance-image.component';

describe('ProcessInstanceImageComponent', () => {
  let fixture: ComponentFixture<ProcessInstanceImageComponent>;
  let http: HttpTestingController;
  let createUrl: jasmine.Spy;
  let revokeUrl: jasmine.Spy;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProcessInstanceImageComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    createUrl = spyOn(URL, 'createObjectURL').and.returnValue('blob:process-image');
    revokeUrl = spyOn(URL, 'revokeObjectURL');
    fixture = TestBed.createComponent(ProcessInstanceImageComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.autoDetectChanges();
  });

  afterEach(() => http.verify());

  it('waits for the container and renders the SVG after the response', async () => {
    fixture.componentRef.setInput('processInstanceId', 4693);
    await fixture.whenStable();
    http.expectNone(req => req.url.includes('/images/'));
    expect(fixture.nativeElement.querySelector('img')).toBeNull();

    fixture.componentRef.setInput('containerId', 'group:artifact:1.0');
    await fixture.whenStable();
    const request = http.expectOne(`${environment.bpmApi}/rest/server/containers/group%3Aartifact%3A1.0/images/processes/instances/4693`);
    expect(request.request.responseType).toBe('text');
    expect(request.request.headers.get('Accept')).toBe('application/svg+xml');
    request.flush('<svg xmlns="http://www.w3.org/2000/svg"></svg>');
    await fixture.whenStable();

    expect(createUrl).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('img').getAttribute('src')).toBe('blob:process-image');
    fixture.destroy();
    expect(revokeUrl).toHaveBeenCalledWith('blob:process-image');
  });

  it('cancels the previous request when the instance changes', async () => {
    fixture.componentRef.setInput('containerId', 'deployment');
    fixture.componentRef.setInput('processInstanceId', 4693);
    await fixture.whenStable();
    const previous = http.expectOne(req => req.url.endsWith('/instances/4693'));

    fixture.componentRef.setInput('processInstanceId', 4694);
    await fixture.whenStable();
    expect(previous.cancelled).toBeTrue();
    http.expectOne(req => req.url.endsWith('/instances/4694')).flush('<svg/>');
    await fixture.whenStable();
  });
});
