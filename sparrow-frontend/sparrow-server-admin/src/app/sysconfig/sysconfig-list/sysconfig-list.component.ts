import { Component, OnInit } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { Sysconfig, SysconfigService } from '@sparrowmini/org-api';
import { SysconfigFormComponent } from '../sysconfig-form/sysconfig-form.component';

@Component({
  selector: 'app-sysconfig-list',
  templateUrl: './sysconfig-list.component.html',
  styleUrls: ['./sysconfig-list.component.scss']
})
export class SysconfigListComponent implements OnInit {
  viewConfig(e: any) {
    this.dialog.open(SysconfigFormComponent, { data: Object.assign(e, { action: 'view' }), width: '100%' })
  }
  fillConfig(e: any) {
    this.dialog.open(SysconfigFormComponent, { data: Object.assign(e, { action: 'edit' }), width: '100%' })
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
    private sysconfigService: SysconfigService,
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
    this.sysconfigService.getInitConfigs(this.pageable.pageIndex, this.pageable.pageSize, this.pageable.sort).subscribe(res => {
      this.dataSource = new MatTableDataSource<Sysconfig>(res.content);
      this.pageable.length = res.totalElements!
    })
  }

}
