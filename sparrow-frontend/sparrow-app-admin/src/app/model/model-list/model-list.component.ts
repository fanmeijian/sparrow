import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { CommonApiService } from '@sparrowmini/common-api';
import { PermissionSelectionComponent } from '@sparrowmini/common-ui-nm';
import { forkJoin } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ModelPermissionComponent } from '../model-permission/model-permission.component';
export const ModelClass = 'cn.sparrowmini.common.model.Model'
export const SysroleModelClass='cn.sparrowmini.common.model.pem.SysroleModel'
export const UserModelClass='cn.sparrowmini.common.model.pem.UserModel'

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.css']
})
export class ModelListComponent implements OnInit {
  removeSysrole(sysroleModel: any) {
    this.commonApiService.delete(SysroleModelClass,[sysroleModel.id]).subscribe();
  }
  synchronizeModel() {
    this.http.post(`${environment.apiBase}/permissions/models/synchronize`, []).subscribe();
  }
  panelOpenState = false;

  dataSource = new MatTableDataSource<any>();
  pageable = { page: 0, size: 10, length: 0, sort: [] };
  displayedColumns = ['seq', 'code', 'users'];

  constructor(
    private commonApiService: CommonApiService,
    private dialog: MatDialog,
    private http: HttpClient, // private monacoEditorService: MonacoEditorService
    private snack: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.onPageChange({ pageIndex: this.pageable.page, pageSize: this.pageable.size });
    // this.monacoEditorService.load();
    // this.monacoEditorService.initMonaco(this._editorContainer);
    // this.initMonaco();
  }

  onPageChange(event: any) {
    this.pageable.page = event.pageIndex
    this.pageable.size = event.pageSize
    // this.modelService
    //   .models(this.pageable.pageIndex,this.pageable.pageSize,['id,asc'])
    this.commonApiService.filter(ModelClass, this.pageable, undefined)
      .subscribe((res: any) => {
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.pageable.length = res.totalElements
      });
  }


  remove(user: any, sysrole: any) {
    // console.log(user, sysrole);
    // this.modelService
    //   .removeModelPermissions({ sysroles: [user.id] }, user.id.modelId)
    //   .subscribe(() => {
    //     this.snack.open('移除成功！', '关闭');
    //     this.ngOnInit();
    //   });
  }

  removeUser(userModel: any) {
    this.commonApiService.delete(UserModelClass,[userModel.id]).subscribe()
  }

  removeRule(user: any, sysrole: any) {
    // console.log(user, sysrole);
    // this.modelService
    //   .removeModelPermissions({ rules: [user.id] }, user.id.modelId)
    //   .subscribe(() => {
    //     this.snack.open('移除成功！', '关闭');
    //     this.ngOnInit();
    //   });
  }

  removeAttrRule(user: any, sysrole: any) {
    // console.log(user, sysrole);
    // this.modelService
    //   .removeAttrPermissions(
    //     { rules: [user.id] },
    //     user.id.modelAttributeId.modelId,
    //     user.id.modelAttributeId.attributeId
    //   )
    //   .subscribe(() => {
    //     this.ngOnInit();
    //   });
  }

  removeAttrPermission(attr: any, body: any) {
    // console.log(attr, body);
    // this.modelService
    //   .removeAttrPermissions(
    //     body,
    //     attr.id.modelId,
    //     attr.id.attributeId
    //   )
    //   .subscribe(() => {
    //     this.ngOnInit();
    //   });
  }

  openPermission(model: any) {
    this.dialog
      .open(ModelPermissionComponent, { data: model, width: '80%', height: '600px' })
      .afterClosed()
      .subscribe((res) => {
        console.log(res)
        if (res) {
          const userBody = res.userPermissions //res.usernames?.map((m: any) => Object.assign({}, { id: { username: m, pageElementId: model.id, type: 'ALLOW' } }))
          const sysroleBody = res.sysrolePermissions//res.sysroleIds?.map((m: any) => Object.assign({}, { id: { sysroleId: m, pageElementId: model.id, type: 'ALLOW' } }))
          const $userPermission = this.commonApiService.upsert('cn.sparrowmini.common.model.pem.UserModel', userBody)
          const $rolePermssion = this.commonApiService.upsert('cn.sparrowmini.common.model.pem.SysroleModel', sysroleBody)
          forkJoin([$userPermission, $rolePermssion]).subscribe(() => {
            this.snack.open('授权成功！', '关闭');
            // this.onPage(this.pageable);
          })

        }
      });
  }

  openAttrPermission(sysrole: any) {
    // this.dialog
    //   .open(AttributePermisssionComponent, {
    //     width: '100%',
    //     data: sysrole,
    //   })
    //   .afterClosed()
    //   .subscribe((result) => {
    //     if (result) {
    //       this.snack.open('授权成功！', '关闭');
    //       this.ngOnInit();
    //     }
    //   });
  }

}
