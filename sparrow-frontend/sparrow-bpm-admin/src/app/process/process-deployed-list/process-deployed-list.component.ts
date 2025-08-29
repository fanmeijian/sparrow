import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { KIEServerAndKIEContainersService, ProcessAndTaskDefinitionsService, ProcessQueriesService } from '@sparrowmini/jbpm-api';
import { ProcessImageComponent } from '../process-image/process-image.component';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-deployed-list',
  templateUrl: './process-deployed-list.component.html',
  styleUrls: ['./process-deployed-list.component.css'],
  providers: [ProcessAndTaskDefinitionsService, KIEServerAndKIEContainersService]
})
export class ProcessDeployedListComponent implements OnInit {
  containers: any[] = []
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);
  displayedColumns: any = ['containerId', 'processName', 'processId', 'processVersion', 'action'];

  showImage(process: any) {
    this.dialog.open(ProcessImageComponent, { data: { containerId: process['container-id'], processId: process['process-id'] } })
  }

  disposeContainer(containerId: string) {
    this.http.delete(`${environment.bpmApi}/process-design/containers/${containerId}/un-deploy`).subscribe();
  }
  constructor(
    private processQueriesService: ProcessQueriesService,
    private dialog: MatDialog,
    private kieServerAndKIEContainersService: KIEServerAndKIEContainersService,
    private http: HttpClient,
  ) { }

  getProcesses(containerId: any) {
    this.processQueriesService.getProcessesByDeploymentId1(containerId).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.processes)
    })
  }
  ngOnInit(): void {
    this.kieServerAndKIEContainersService.listContainers().subscribe((res: any) => {
      this.containers = res.result['kie-containers']['kie-container']
    })

  }

}
