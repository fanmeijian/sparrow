import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { ScopeService, SysconfigService, SysroleService } from '@sparrowmini/org-api';
import { combineLatest, map, of, switchMap, tap, zip } from 'rxjs';
import { SysroleCreateComponent } from '../../sysrole/sysrole-create/sysrole-create.component';
import { SysrolePermissionComponent } from '../../sysrole/sysrole-permission/sysrole-permission.component';
import { MatLegacyPaginator as MatPaginator, LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { ScopeCreateComponent } from '../scope-create/scope-create.component';
import { ScopePermissionComponent } from '../scope-permission/scope-permission.component';
import { baseOpLogColumns } from '../../common/base-op-log-column/base-op-log-column.component';

@Component({
  selector: 'app-scopes',
  templateUrl: './scopes.component.html',
  styleUrls: ['./scopes.component.scss'],
})
export class ScopesComponent implements OnInit {
  sync() {
    this.sysconfigService.synchronizeScope().subscribe(() => {
      this.snack.open('同步成功！', '关闭');
      this.ngOnInit();
    })
  }
  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  // pageable = { page: 0, size: 10 };

  total: number = 0;
  displayedColumns = ['id', 'name', 'code', 'users', 'sysroles','modified', 'actions'];

  filters: any[] = [];
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  constructor(
    private scopeService: ScopeService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private sysroleService: SysroleService,
    private sysconfigService: SysconfigService,
  ) { }

  ngOnInit(): void {
    this.onPage(this.pageable);
  }

  new() {
    this.dialog.open(ScopeCreateComponent);
  }

  delete(sysrole: any) {
    this.scopeService.deleteScopes([sysrole.id]).subscribe(() => {
      this.ngOnInit();
      this.snack.open('删除成功！', '关闭');
    });
  }

  edit(sysrole: any) {
    this.dialog
      .open(ScopeCreateComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) this.ngOnInit();
      });
  }

  remove(sysrole: any, scope: any) {
    this.scopeService
      .removeScopePermissions({ sysroles: [sysrole.id.sysroleId] }, scope.id)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  removeUser(user: any, scope: any) {
    this.scopeService
      .removeScopePermissions({ users: [user] }, scope.id)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(ScopePermissionComponent, { data: sysrole })
      .afterClosed()
      .subscribe((res) => {
        if (res) {
          this.snack.open('授权成功！', '关闭');
          this.onPage(this.pageable);
        }
      });
  }

  onPage(page: PageEvent) {
    this.dataSource = new MatTableDataSource<any>();
    this.scopeService
      .scopeFilter(
        this.filters,
        page.pageIndex,
        page.pageSize,
        this.pageable.sort
      )
      .subscribe((res: any) => {
        // console.log(res);
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.pageable.length = res.totalElements
      });
  }

  @ViewChild('paginator', { static: true }) paginator!: MatPaginator;
  applyFilter(event: any) {
    this.filters = event;
    this.pageable.pageIndex = 0;
    this.paginator.pageIndex = this.pageable.pageIndex;
    this.onPage({
      pageIndex: this.pageable.pageIndex,
      pageSize: this.pageable.pageSize,
      length: this.pageable.length,
    });
  }
}
