import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { ScopeService, SysroleService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-scope-permission',
  templateUrl: './scope-permission.component.html',
  styleUrls: ['./scope-permission.component.scss'],
})
export class ScopePermissionComponent implements OnInit {
  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private sysroleService: ScopeService
  ) {}

  ngOnInit(): void {
    // console.log(this.data);
  }

  submit() {
    this.sysroleService
      .addScopePermissions(
        {
          sysroles: this.selectedSysroles.map((m) => m.id),
          users: this.selectedUsernames.map((m) => m.username),
        },
        this.data.id
      )
      .subscribe();
  }
}
