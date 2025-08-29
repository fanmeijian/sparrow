import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { MatLegacyChipInputEvent as MatChipInputEvent } from '@angular/material/legacy-chips';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import {
  DatamodelService,
  MenuService,
  RuleService,
  SysroleService,
} from '@sparrowmini/org-api';

import { PermissionEnum } from '../../common/ckeditor-control/constant';
import { UntypedFormGroup, Validators, UntypedFormBuilder, UntypedFormArray } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';

@Component({
  selector: 'app-sprmodel-permisssion',
  templateUrl: './sprmodel-permisssion.component.html',
  styleUrls: ['./sprmodel-permisssion.component.scss'],
  providers: [RuleService]
})
export class SprmodelPermisssionComponent implements OnInit {
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
    private ruleService: RuleService
  ) {}

  ngOnInit(): void {
    console.log(this.data);
    this.listOfOption = this.permissionKeys;
  }

  submit() {
    // console.log(this.selectedSysroles, this.selectedUsernames)
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

      // console.log(sysrolePermissions);
      // this.modelService
      //   .addModelPermissions({ sysroles: sysrolePermissions }, this.data.id)
      //   .subscribe();
      this.ruleService.newRule(this.rules).subscribe((res: any) => {
        let rulePermission: any[] = [];
        this.selectedPermissions.forEach((permission) => {
          res.forEach((f: string) => {
            rulePermission.push({
              permissionType: this.permissionType,
              permission: permission,
              ruleId: f,
            });
          });
        });

        this.modelService
          .addModelPermissions(
            {
              rules: rulePermission,
              sysroles: sysrolePermissions,
              usernames: userPermissions
            },
            this.data.id
          )
          .subscribe();
      });
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
