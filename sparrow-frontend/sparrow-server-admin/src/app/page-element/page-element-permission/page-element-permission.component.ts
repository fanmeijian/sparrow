import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import {
  PageElementService,
  ScopeService,
  SysrolePageElementId,
} from '@sparrowmini/org-api';

@Component({
  selector: 'app-page-element-permission',
  templateUrl: './page-element-permission.component.html',
  styleUrls: ['./page-element-permission.component.scss'],
})
export class PageElementPermissionComponent implements OnInit {
  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];
  type: SysrolePageElementId.TypeEnum = 'ALLOW';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private sysroleService: PageElementService
  ) {}

  ngOnInit(): void {
    // console.log(this.data);
  }

  submit() {
    this.sysroleService
      .addPageElementPermission(
        {
          sysrolePermissions: this.selectedSysroles.map((m) =>
            Object.assign(
              {},
              { sysroleId: m.id, pageElementId: this.data.id, type: this.type }
            )
          ),
          userPermissions: this.selectedUsernames.map((m) =>
            Object.assign(
              {},
              {
                username: m.username,
                pageElementId: this.data.id,
                type: this.type,
              }
            )
          ),
        }
      )
      .subscribe();
  }
}
