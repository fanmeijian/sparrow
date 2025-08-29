import {
  Component,
  Input,
  OnInit,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import {
  DatamodelService,
  ModelPermissionResponseBody,
  ModelRule,
  SysroleModel,
  UserModel,
} from '@sparrowmini/org-api';

@Component({
  selector: 'app-model-permission-view',
  templateUrl: './model-permission-view.component.html',
  styleUrls: ['./model-permission-view.component.scss'],
})
export class ModelPermissionViewComponent implements OnInit {
  @Input() public modelId!: string;
  @Input() public attributeId?: string;

  @ViewChild('permissionDialog') permissionTemplate!: TemplateRef<any>;
  permission?: ModelPermissionResponseBody;
  constructor(
    private modelService: DatamodelService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    if (this.attributeId) {
      this.modelService.attrPermissions(this.modelId, this.attributeId).subscribe((res) => {
        this.permission = res;
      });
    } else {
      this.modelService.modelPermissions(this.modelId).subscribe((res) => {
        this.permission = res;
      });
    }

  }

  view() {
    this.dialog.open(this.permissionTemplate, { width: '100%' });
  }


  removeUser(user: UserModel) {
    let permissions = [{
      username: user?.id?.username,
      permission: user?.id?.permission,
      permissionType: user?.id?.permissionType
    }]
    if (this.attributeId) {
      this.modelService.removeAttrPermissions({
        usernames: permissions
      }, this.modelId, this.attributeId).subscribe((res) => {
        this.ngOnInit()
      })
    } else {
      this.modelService.removeModelPermissions({
        usernames: permissions
      }, user?.id?.modelId!).subscribe((res) => {
        this.ngOnInit()
      })
    }

  }
  remove(a: SysroleModel) {
    let permissions = [{
      sysroleId: a?.id?.sysroleId,
      permission: a?.id?.permission,
      permissionType: a?.id?.permissionType
    }]
    if (this.attributeId) {
      this.modelService.removeAttrPermissions({ sysroles: permissions }, this.modelId, this.attributeId).subscribe(() => {
        this.ngOnInit()
      })
    } else {
      this.modelService.removeModelPermissions({
        sysroles: permissions
      }, a?.id?.modelId!).subscribe((res) => {
        this.ngOnInit()
      })
    }

  }

  removeRule(a: ModelRule) {
    let permissions = {
      rules: [{
        ruleId: a?.id?.ruleId,
        permission: a?.id?.permission,
        permissionType: a?.id?.permissionType
      }]
    }
    if (this.attributeId) {
      this.modelService.removeAttrPermissions(permissions, this.modelId, this.attributeId).subscribe(() => {
        this.ngOnInit()
      })
    } else {
      this.modelService.removeModelPermissions(permissions, a?.id?.modelId!).subscribe((res) => {
        this.ngOnInit()
      })
    }

  }
}
