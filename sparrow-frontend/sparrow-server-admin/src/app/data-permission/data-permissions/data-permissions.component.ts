import { Component, OnInit } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { DataPermissionService, SysroleService } from '@sparrowmini/org-api';
import { map, switchMap, tap, zip } from 'rxjs';

@Component({
  selector: 'app-data-permissions',
  templateUrl: './data-permissions.component.html',
  styleUrls: ['./data-permissions.component.scss'],
})
export class DataPermissionsComponent implements OnInit {
  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0 , sort: []};

  displayedColumns = ['seq','name', 'remark','permission'];

  constructor(
    private dataPermissionService: DataPermissionService,
    private sysroleService: SysroleService,
    private dialog: MatDialog,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.onPageChange(this.pageable);
  }

  onPageChange(event: PageEvent) {
    this.pageable.pageIndex =event.pageIndex
    this.pageable.pageSize = event.pageSize
    this.dataPermissionService
      .dataPermissions(this.pageable.pageIndex, this.pageable.pageSize, this.pageable.sort)
      .subscribe((res) => {
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.pageable.length = res.totalElements;
      });
  }
}
