import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup, UntypedFormArray, UntypedFormBuilder } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import {
  DataPermissionService,
  DatamodelService,
  RuleService,
} from '@sparrowmini/org-api';
import { PermissionEnum } from '../../common/ckeditor-control/constant';
import { showId } from '../../util/model-util';

@Component({
  selector: 'app-data-permission-grant',
  templateUrl: './data-permission-grant.component.html',
  styleUrls: ['./data-permission-grant.component.scss'],
})
export class DataPermissionGrantComponent implements OnInit {
  showId = showId

  formGroup: UntypedFormGroup = this.fb.group({
    sysroles: this.fb.array([]),
    usernames: this.fb.array([]),
  });

  get sysroles() {
    return this.formGroup.get('sysroles') as UntypedFormArray;
  }

  get usernames() {
    return this.formGroup.get('usernames') as UntypedFormArray;
  }

  selectedSysroles: any[] = [];
  selectedUsernames: any[] = [];
  users: string = '';
  selectedPermissions: any[] = [];
  permissionType: string = '';
  rules: any[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private modelService: DatamodelService,
    private fb: UntypedFormBuilder,
    private snack: MatSnackBar,
    private dataPermissionService: DataPermissionService
  ) {}

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
            sysroleId: sysrole.id,
            permissionType: this.permissionType,
            permission: permission,
          });
        });

        this.selectedUsernames.forEach((o) => {
          userPermissions.push({
            username: o.username,
            permissionType: this.permissionType,
            permission: permission,
          });
        });
      });

      if (this.data.dataPermissionId) {
        this.dataPermissionService
          .updateDataPermission(
            { sysroleIds: sysrolePermissions, usernames: userPermissions },
            this.data.dataPermissionId
          )
          .subscribe((res) => this.snack.open('授权成功！', '关闭'));
      } else {
        this.dataPermissionService
          .newDataPermission(
            { sysroleIds: sysrolePermissions, usernames: userPermissions },
            this.data.modelName,
            typeof this.data.id === 'object'? JSON.stringify(this.data.id): this.data.id
          )
          .subscribe((res) => this.snack.open('授权成功！', '关闭'));
      }
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
