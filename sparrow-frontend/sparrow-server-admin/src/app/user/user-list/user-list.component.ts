import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { SysroleService, UserService } from '@sparrowmini/org-api';
import { tap, map, switchMap, zip } from 'rxjs';
import { DataPermissionGrantComponent } from '../../data-permission/data-permission-grant/data-permission-grant.component';
import { SysroleCreateComponent } from '../../sysrole/sysrole-create/sysrole-create.component';
import { SysrolePermissionComponent } from '../../sysrole/sysrole-permission/sysrole-permission.component';
import { UserCreateComponent } from '../user-create/user-create.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0,sort: ['createdDate,desc'] };

  displayedColumns = ['seq', 'name', 'code', 'users', 'actions'];

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private http: HttpClient
  ) {}

  filters: any[] = [];
  ngOnInit(): void {
    this.onPageChange(this.pageable);
  }

  applyFilter() {
    this.onPageChange({ pageIndex: 0, pageSize: this.pageable.pageSize });
  }

  onPageChange(event: any) {
    this.dataSource = new MatTableDataSource<any>();
    this.pageable.pageIndex = event.pageIndex;
    this.pageable.pageSize = event.pageSize;
    this.userService
      .users(this.filters, this.pageable.pageIndex, this.pageable.pageSize)
      .subscribe((res) => {
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.pageable.length = res.totalElements!
      });
  }

  new() {
    this.dialog.open(UserCreateComponent).afterClosed()
    .subscribe((result) => {
      if (result) this.ngOnInit();
    });;
  }

  delete(sysrole: any) {
    this.userService.deleteUser([sysrole.id]).subscribe(() => {
      this.snack.open('删除成功！', '关闭');
    });
  }

  edit(sysrole: any) {
    this.dialog
      .open(UserCreateComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) this.onPageChange(this.pageable);
      });
  }

  remove(user: any, sysrole: any) {
    this.userService.deleteUser([user], sysrole.id).subscribe(() => {
      this.snack.open('移除成功！', '关闭');
      this.ngOnInit();
    });
  }

  openPermission(sysrole: any) {
    this.dialog.open(SysrolePermissionComponent, { data: sysrole , width:'80%'});
  }

  openDataPermission(sysrole: any) {
    this.dialog.open(DataPermissionGrantComponent, {
      data: sysrole,
      width: '80%',
    });
  }
  enableUser(u:string,a:boolean){
    this.userService.enableUser(u,a).subscribe(()=>{
      this.ngOnInit()
      this.snack.open('启用/禁用成功','关闭')

    })
  }
}
