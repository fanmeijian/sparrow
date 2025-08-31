import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent, MatPaginator } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { ScopeFormComponent } from '../scope-form/scope-form.component';
import { ScopePermissionComponent } from '../scope-permission/scope-permission.component';
import { CommonApiService } from '@sparrowmini/common-api';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { DialogService, PermissionSelectionComponent } from '@sparrowmini/common-ui-nm';
import { forkJoin } from 'rxjs';
export const ScopeClass = 'cn.sparrowmini.common.model.Scope'
export const UserScopeClass = 'cn.sparrowmini.common.model.pem.UserScope'
export const SysroleScopeClass = 'cn.sparrowmini.common.model.pem.SysroleScope'

@Component({
  selector: 'app-scope-list',
  templateUrl: './scope-list.component.html',
  styleUrls: ['./scope-list.component.css']
})
export class ScopeListComponent implements OnInit {
  className = ScopeClass
  sync() {
    const $request = this.http.post(`${environment.apiBase}/permissions/scopes/synchronize`, [])
    $request.subscribe(() => {
      this.snack.open('同步成功！', '关闭');
      this.ngOnInit();
    })
  }
  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  // pageable = { page: 0, size: 10 };

  total: number = 0;
  displayedColumns = ['id', 'name', 'code', 'users', 'sysroles', 'modified', 'actions'];

  filters: any[] = [];
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  constructor(
    private commonApiService: CommonApiService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private http: HttpClient,
    private dialogService: DialogService,
  ) { }

  ngOnInit(): void {
    this.onPage(this.pageable);
  }

  new() {
    this.dialog.open(ScopeFormComponent);
  }

  delete(element: any) {
    this.dialogService.confirm('是否删除？').subscribe(r => {
      if (r) {
        this.commonApiService.delete(this.className, element.id).subscribe(() => {
          this.ngOnInit();
          this.snack.open('删除成功！', '关闭');
        })
      }
    })

    // this.scopeService.deleteScopes([sysrole.id]).subscribe(() => {
    //   this.ngOnInit();
    //   this.snack.open('删除成功！', '关闭');
    // });

  }

  edit(sysrole: any) {
    this.dialog
      .open(ScopeFormComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) this.ngOnInit();
      });
  }

  removeSysrole(sysroleScope: any) {
    this.commonApiService.delete(SysroleScopeClass,[sysroleScope.id]).subscribe()
  }

  removeUser(userScope: any) {
    console.log(userScope.id)
    this.commonApiService.delete(UserScopeClass,[userScope.id]).subscribe()
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(PermissionSelectionComponent, { data: sysrole, width: '80%', height:'600px' })
      .afterClosed()
      .subscribe((res) => {
        if (res) {
          const userBody = res.usernames?.map((m: any) => Object.assign({}, { id: { username: m, scopeId: sysrole.id } }))
          const sysroleBody = res.sysroleIds?.map((m: any) => Object.assign({}, { id: { sysroleId: m, scopeId: sysrole.id } }))
          const $userPermission = this.commonApiService.upsert('cn.sparrowmini.common.model.pem.UserScope', userBody)
          const $rolePermssion = this.commonApiService.upsert('cn.sparrowmini.common.model.pem.SysroleScope', sysroleBody)
          forkJoin([$userPermission,$rolePermssion]).subscribe(() => {
            this.snack.open('授权成功！', '关闭');
            this.onPage(this.pageable);
          })

        }
      });
  }

  onPage(page: PageEvent) {
    this.dataSource = new MatTableDataSource<any>();
    // this.scopeService
    //   .scopeFilter(
    //     this.filters,
    //     page.pageIndex,
    //     page.pageSize,
    //     this.pageable.sort
    //   )
    //   .subscribe((res: any) => {
    //     // console.log(res);
    //     this.dataSource = new MatTableDataSource<any>(res.content);
    //     this.pageable.length = res.totalElements
    //   });
    this.commonApiService.filter(ScopeClass, {
      page: 0,
      size: 10,
      sort: []
    }, undefined).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource<any>(res.content);
      this.pageable.length = res.totalElements
    })
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
