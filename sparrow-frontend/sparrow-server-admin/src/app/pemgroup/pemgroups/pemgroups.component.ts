import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyPaginator as MatPaginator, LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import {
  GroupMemberBean,
  PemgroupService,
  ScopeService,
  SysroleService,
} from '@sparrowmini/org-api';
import { tap, map, switchMap, zip, of, combineLatest } from 'rxjs';
import { ScopeCreateComponent } from '../../scope/scope-create/scope-create.component';
import { ScopePermissionComponent } from '../../scope/scope-permission/scope-permission.component';
import { PemgroupCreateComponent } from '../pemgroup-create/pemgroup-create.component';
import { PemgroupMemberComponent } from '../pemgroup-member/pemgroup-member.component';

@Component({
  selector: 'app-pemgroups',
  templateUrl: './pemgroups.component.html',
  styleUrls: ['./pemgroups.component.scss'],
})
export class PemgroupsComponent implements OnInit {
  filters: any[] = [];

  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0 };

  total: number = 0;
  displayedColumns = ['seq', 'name', 'code', 'users', 'sysroles', 'actions'];

  constructor(
    private groupService: PemgroupService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private sysroleService: SysroleService
  ) {}

  ngOnInit(): void {
    this.onPage(this.pageable);
  }

  new() {
    this.dialog.open(PemgroupCreateComponent);
  }

  delete(sysrole: any) {
    this.groupService.deleteGroup(sysrole.id).subscribe(() => {
      this.onPage(this.pageable);
      this.snack.open('删除成功！', '关闭');
    });
  }

  edit(group: any) {
    this.dialog
      .open(PemgroupCreateComponent, { data: group })
      .afterClosed()
      .subscribe((res) => {
        if (res) {
          this.ngOnInit();
        }
      });
  }

  remove(members: GroupMemberBean) {
    this.groupService
      .removeGroupMembers(members)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(PemgroupMemberComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.snack.open('添加成功！', '关闭');
          this.onPage(this.pageable);
        }
      });
  }

  onPage(page: PageEvent) {
    this.pageable.pageIndex = page.pageIndex;
    this.pageable.pageSize = page.pageSize;
    this.dataSource = new MatTableDataSource<any>();
    this.groupService
      .groupFilter(
        this.filters,
        this.pageable.pageIndex,
        this.pageable.pageSize
      )
      .pipe(
        tap((t) => (this.pageable.length = t.totalElements!)),
        map((res: any) => res.content),
        switchMap((sysroles: any[]) =>
          zip(
            ...sysroles.map((group) => {
              const $users = this.groupService
                .groupMembers(group.id, 'USER')
                .pipe(
                  map((m) =>
                    m.content && m.content.length > 0
                      ? m.content?.map((a) => a.id.username)
                      : m.content
                  )
                );
              const $sysroles = this.groupService
                .groupMembers(group.id, 'SYSROLE')
                .pipe(
                  map((m) => m.content),
                  switchMap((res: any) =>
                    res.length > 0
                      ? zip(
                          ...res.map((m: any) =>
                            this.sysroleService.sysrole(m.id.sysroleId)
                          )
                        )
                      : of([])
                  )
                );

              return combineLatest($users, $sysroles).pipe(
                map((permissions: any) =>
                  Object.assign({}, group, {
                    users: permissions[0],
                    sysroles: permissions[1],
                  })
                )
              );
            })
          )
        )
      )
      .subscribe((res) => {
        this.dataSource = new MatTableDataSource<any>(res);
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
