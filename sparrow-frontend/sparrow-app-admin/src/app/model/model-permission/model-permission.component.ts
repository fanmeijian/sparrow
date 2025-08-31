import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup, UntypedFormArray, UntypedFormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

export enum PermissionEnum {
  READER = "读者",
  AUTHOR = "作者",
  EDITOR = "编辑者",
  DELETER = "删除者",
  DOWNLOAD = "下载",
  FORWARD = "转发",
  SHARE_READER = "授权读者",
  SHARE_AUTHOR = "授权作者",
  SHARE_EDITOR = "授权编辑者",
  SHARE_DELETER = "授权删除者",
  SHARE_FORWARD = "授权转发",
  SHARE_DOWNLOAD = "授权下载",
  ALL_CRUD = "所有操作",
  SHARE_ALL = "所有授权",
  ALL = "所有",
}

@Component({
  selector: 'app-model-permission',
  templateUrl: './model-permission.component.html',
  styleUrls: ['./model-permission.component.css']
})
export class ModelPermissionComponent implements OnInit {
  formGroup: UntypedFormGroup = this.fb.group({
    sysroles: this.fb.array([]),
    usernames: this.fb.array([]),
  });

  onSysroleSelect($event: any) {
    this.selectedSysroles = $event.map((m: any) => m.code);
  }


  onUserSelect($event: any) {
    console.log($event)
    this.selectedUsernames = $event.map((m: any) => m.username)
  }

  close() {
    this.dialogRef.close(true)
  }

  get sysroles() {
    return this.formGroup.get('sysroles') as UntypedFormArray;
  }

  // get usernames() {
  //   return this.formGroup.get('usernames') as UntypedFormArray;
  // }

  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];
  users: string = '';
  selectedPermissions: any[] = [];
  permissionType: string = '';
  rules: any[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: UntypedFormBuilder,
    private snack: MatSnackBar,
    private dialogRef: MatDialogRef<ModelPermissionComponent>,
  ) { }

  ngOnInit(): void {
    console.log(this.data);
    this.listOfOption = this.permissionKeys;
  }

  submit() {
    if (this.selectedPermissions.length > 0 && this.permissionType) {
      let sysrolePermissions: any[] = [];
      let userPermissions: any[] = [];
      this.selectedPermissions.forEach((permission) => {
        this.selectedSysroles.forEach((sysrole) => {
          sysrolePermissions.push({
            id: {
              modelId: this.data.id,
              sysroleId: sysrole,
              permissionType: this.permissionType,
              permission: permission,
            }
          });
        });

        this.selectedUsernames.forEach((o) => {
          userPermissions.push({
            id: {
              modelId: this.data.id,
              username: o,
              permissionType: this.permissionType,
              permission: permission,
            }
          });
        });
      });
      this.dialogRef.close({ sysrolePermissions: sysrolePermissions, userPermissions: userPermissions })
    } else {
      this.snack.open('请选择授予权限和权限类型！', '关闭');
    }
  }

  permissionKeys: any[] = Object.keys(PermissionEnum).map((label) => ({
    label: PermissionEnum[label as keyof typeof PermissionEnum],
    value: label,
  }));
  listOfSelectedPermissionValue: any[] = [];
  listOfOption?: any[];
  radioValue?: string;

  isNotSelected(value: string): boolean {
    // console.log("selected permission", this.listOfSelectedValue)
    return this.listOfSelectedPermissionValue.indexOf(value) === -1;
  }
}
