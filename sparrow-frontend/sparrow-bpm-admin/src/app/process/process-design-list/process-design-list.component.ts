import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { env } from 'process';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-design-list',
  templateUrl: './process-design-list.component.html',
  styleUrls: ['./process-design-list.component.css']
})
export class ProcessDesignListComponent implements OnInit {
  containerId: any
  authorize(_t32: any) {
    throw new Error('Method not implemented.');
  }
  unpublish(_t32: any) {
    throw new Error('Method not implemented.');
  }
  publish(_t32: any) {
    throw new Error('Method not implemented.');
  }
  dataSource: any = new MatTableDataSource([]);;
  displayedColumns: any = ['name','processId','version', 'action'];

  constructor(
    private http: HttpClient,
    public route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.containerId = this.route.snapshot.queryParams
    const httpParams = new HttpParams({fromObject: this.containerId})
    this.http.get(`${environment.bpmApi}/process-design`, {params: httpParams}).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content);
    })

  }

}
