import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { CommonApiService } from '@sparrowmini/common-api';
export const ConfigClass = 'cn.sparrowmini.common.model.AppConfig'
@Component({
  selector: 'app-config-list',
  templateUrl: './config-list.component.html',
  styleUrls: ['./config-list.component.css']
})
export class ConfigListComponent implements OnInit {
  className=ConfigClass
  viewConfig(e: any) {
    // this.dialog.open(SysconfigFormComponent, { data: Object.assign(e, { action: 'view' }), width: '100%' })
  }
  fillConfig(e: any) {
    // this.dialog.open(SysconfigFormComponent, { data: Object.assign(e, { action: 'edit' }), width: '100%' })
  }

  dataSource = new MatTableDataSource<any>();
  displayedColumns = ['seq', 'name', 'code',  'actions'];
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  constructor(
    private commonApiService: CommonApiService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.onPage(this.pageable)
  }

  new() {

  }

  onPage(page: PageEvent) {
    this.dataSource = new MatTableDataSource<any>();
    this.pageable.pageIndex = page.pageIndex
    this.pageable.pageSize = page.pageSize
    this.commonApiService.filter(this.className,{
      page: 0,
      size: 10,
      sort: []
    }, undefined)
    .subscribe((res: any) => {
      this.dataSource = new MatTableDataSource<any>(res.content);
      this.pageable.length = res.totalElements!
    })
  }

}
