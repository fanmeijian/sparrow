import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { CommonApiService } from '@sparrowmini/common-api';

@Component({
  selector: 'app-scope-permission',
  templateUrl: './scope-permission.component.html',
  styleUrls: ['./scope-permission.component.css']
})
export class ScopePermissionComponent {
  onSysroleSelect($event: any) {
    this.sysrolesIds = $event.map((m: any) => m.code);
  }
  usernames?: []
  sysrolesIds?: []

  constructor(
    private commonApiService: CommonApiService,
    private dialogRef: MatDialogRef<ScopePermissionComponent>
  ) { }
  onUserSelect($event: any) {
    console.log($event)
    this.usernames = $event.map((m: any) => m.username)
  }

  close() {
    this.dialogRef.close({usernames: this.usernames, sysroleIds: this.sysrolesIds})
  }

  submit() {

  }
}
