import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import {
  Menu,
  MenuService,
  Sysrole,
  SysroleMenuId,
  User,
  UserMenuId,
} from '@sparrowmini/org-api';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-menu-permission',
  templateUrl: './menu-permission.component.html',
  styleUrls: ['./menu-permission.component.scss'],
})
export class MenuPermissionComponent implements OnInit {
  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private menuService: MenuService,
    private dialogRef: MatDialogRef<MenuPermissionComponent>,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    console.log(this.data);
  }

  submit() {
    let userMenus: UserMenuId[] = [];
    let sysroleMenus: SysroleMenuId[] = [];
    let menus = this.data.forEach((m: Menu) => {
      userMenus.push(
        ...this.selectedUsernames.map((user: User) =>
          Object.assign({}, { username: user.username, menuId: m.id })
        )
      );

      sysroleMenus.push(
        ...this.selectedSysroles.map((sysrole: Sysrole) =>
          Object.assign({}, { sysroleId: sysrole.id, menuId: m.id })
        )
      );
    });
    const body = {
      userMenuIds: userMenus,
      sysroleMenuIds: sysroleMenus,
    }
    this.http.post(`${environment.apiBase}/menus/permissions/add`, body).subscribe(() => {
      this.dialogRef.close(true)
    });
  }
}
