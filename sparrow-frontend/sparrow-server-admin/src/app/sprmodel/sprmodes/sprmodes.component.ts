import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import {
  DatamodelService,
  ModelAttributePermissionResponseBody,
  PermissionRequestBody,
  SysroleService,
} from '@sparrowmini/org-api';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { SprmodelPermisssionComponent } from '../sprmodel-permisssion/sprmodel-permisssion.component';
import { map, tap, switchMap, zip, of, combineLatest, first } from 'rxjs';
import { AttributePermisssionComponent } from '../attribute-permisssion/attribute-permisssion.component';
// import { MonacoEditorService } from '../../../service/monaco-editor.service';
// import * as monaco from 'monaco-editor';
import { HttpClient } from '@angular/common/http';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-sprmodes',
  templateUrl: './sprmodes.component.html',
  styleUrls: ['./sprmodes.component.scss'],
})
export class SprmodesComponent implements OnInit {
  synchronizeModel() {
    this.http.get(`${environment.apiBase}/models/synchronize`).subscribe();
  }
  panelOpenState = false;

  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0 };
  displayedColumns = ['seq', 'code', 'users'];

  constructor(
    private modelService: DatamodelService,
    private dialog: MatDialog,
    private sysroleService: SysroleService,
    private http: HttpClient, // private monacoEditorService: MonacoEditorService
    private snack: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.onPageChange(this.pageable);
    // this.monacoEditorService.load();
    // this.monacoEditorService.initMonaco(this._editorContainer);
    // this.initMonaco();
  }

  onPageChange(event: any) {
    this.pageable.pageIndex = event.pageIndex
    this.pageable.pageSize = event.pageSize
    this.modelService
      .models(this.pageable.pageIndex,this.pageable.pageSize,['id,asc'])
      .subscribe((res: any) => {
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.pageable.length = res.totalElements
      });
  }


  remove(user: any, sysrole: any) {
    // console.log(user, sysrole);
    this.modelService
      .removeModelPermissions({ sysroles: [user.id] }, user.id.modelId)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  removeUser(user: any, modelPermission: any) {
    // console.log(user, sysrole);
    this.modelService
      .removeModelPermissions({ usernames: [user.id] }, user.id.modelId)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  removeRule(user: any, sysrole: any) {
    // console.log(user, sysrole);
    this.modelService
      .removeModelPermissions({ rules: [user.id] }, user.id.modelId)
      .subscribe(() => {
        this.snack.open('移除成功！', '关闭');
        this.ngOnInit();
      });
  }

  removeAttrRule(user: any, sysrole: any) {
    // console.log(user, sysrole);
    this.modelService
      .removeAttrPermissions(
        { rules: [user.id] },
        user.id.modelAttributeId.modelId,
        user.id.modelAttributeId.attributeId
      )
      .subscribe(() => {
        this.ngOnInit();
      });
  }

  removeAttrPermission(attr: any, body: PermissionRequestBody) {
    console.log(attr, body);
    this.modelService
      .removeAttrPermissions(
        body,
        attr.id.modelId,
        attr.id.attributeId
      )
      .subscribe(() => {
        this.ngOnInit();
      });
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(SprmodelPermisssionComponent, {
        data: sysrole,
        width: '100%',
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.snack.open('授权成功！', '关闭');
          this.ngOnInit();
        }
      });
  }

  openAttrPermission(sysrole: any) {
    this.dialog
      .open(AttributePermisssionComponent, {
        width: '100%',
        data: sysrole,
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.snack.open('授权成功！', '关闭');
          this.ngOnInit();
        }
      });
  }

  public _editor: any;
  @ViewChild('editorContainer', { static: true })
  _editorContainer!: ElementRef<any>;
}
