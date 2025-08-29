import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { LegacyPageEvent as PageEvent, MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import {
  PageElementService,
  SysrolePageElementId,
  SysroleService,
  UserPageElementId,
} from '@sparrowmini/org-api';
import { tap, map, switchMap, zip, of, combineLatest } from 'rxjs';
import { ScopeCreateComponent } from '../../scope/scope-create/scope-create.component';
import { ScopePermissionComponent } from '../../scope/scope-permission/scope-permission.component';
import { PageElementCreateComponent } from '../page-element-create/page-element-create.component';
import { PageElementPermissionComponent } from '../page-element-permission/page-element-permission.component';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-page-elements',
  templateUrl: './page-elements.component.html',
  styleUrls: ['./page-elements.component.scss'],
})
export class PageElementsComponent implements OnInit {
  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  // pageable = { page: 0, size: 10 };

  total: number = 0;
  displayedColumns = ['id', 'name', 'code', 'pageId', 'users', 'sysroles', 'modified', 'actions'];

  filters: any[] = [];
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  constructor(
    private scopeService: PageElementService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private sysroleService: SysroleService,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    this.onPage(this.pageable);
  }

  new() {
    this.dialog.open(PageElementCreateComponent);
  }

  delete(sysrole: any) {
    this.scopeService.deletePageElement([sysrole.id]).subscribe(() => {
      this.ngOnInit();
      this.snack.open('删除成功！', '关闭');
    });
  }

  edit(sysrole: any) {
    this.dialog
      .open(PageElementCreateComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) this.ngOnInit();
      });
  }

  remove(sysroles: SysrolePageElementId[], users: UserPageElementId[]) {
    this.scopeService
      .removePageElementPermission({
        sysrolePermissions: sysroles,
        userPermissions: users,
      })
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(PageElementPermissionComponent, { data: sysrole})
      .afterClosed()
      .subscribe((res) => {
        if (res) {
          this.snack.open('授权成功！', '关闭');
          // this.onPage(this.pageable);
        }
      });
  }

  onPage(page: PageEvent) {
    this.dataSource = new MatTableDataSource<any>();
    this.http.post(`${environment.apiBase}/page-elements/filter`, [])
      .subscribe((res: any) => {
        console.log(res);
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
