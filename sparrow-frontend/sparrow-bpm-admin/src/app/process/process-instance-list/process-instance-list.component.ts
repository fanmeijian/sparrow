import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { ProcessInstanceAdministrationService, ProcessInstancesService, ProcessQueriesService } from '@sparrowmini/jbpm-api';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-instance-list',
  templateUrl: './process-instance-list.component.html',
  styleUrls: ['./process-instance-list.component.css']
})
export class ProcessInstanceListComponent implements OnInit {
  delete(processInstance: any) {
    this.http.delete(`${environment.bpmApi}/process-instances/delete`, { params: { processInstanceIds: [processInstance.processInstanceId,0] } }).subscribe();
  }
  onPage($event: any) {
    console.log($event)
    this.pageable.page = $event.pageIndex
    this.pageable.size = $event.pageSize
    const httpParams = new HttpParams({ fromObject: this.pageable })
    this.http.get(`${environment.bpmApi}/process-instances`, { params: httpParams }).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content)
      this.pageable.length = res.totalElements
    });
  }
  pageable = { page: 0, size: 10, length: 0, sort: ['start,desc'] }
  processNodes: Record<string, any> = {}
  initNodes(process: any) {
    const httpParams = new HttpParams({ fromObject: { page: 0, size: 1000, length: 0 } })
    const processInstanceId = process.processInstanceId
    this.http.get(`${environment.bpmApi}/process-instances/${processInstanceId}/node-instances`, { params: httpParams }).subscribe((res: any) => {
      this.processNodes[processInstanceId] = res.content
    })
    // this.processAdminService.getActiveNodeInstances(process['container-id'], process['process-instance-id']).subscribe((res: any) => {
    //   this.processNodes[process['process-instance-id']] = res['node-instance']
    // })

  }
  abort(element: any) {
    this.processInstanceService.abortProcessInstance(element.externalId, element.processInstanceId).subscribe()
  }
  displayedColumns = ['name', 'initiator', 'state', 'action'];
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);

  constructor(
    public route: ActivatedRoute,
    private processQueryService: ProcessQueriesService,
    private processInstanceService: ProcessInstancesService,
    private processAdminService: ProcessInstanceAdministrationService,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    if (!this.route.firstChild) {
      this.onPage({ pageIndex: this.pageable.page, pageSize: this.pageable.size })
    }
  }

}
