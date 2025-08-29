import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { SysroleService } from '@sparrowmini/org-api';
import { map, switchMap, tap, zip } from 'rxjs';
import { SysroleCreateComponent } from '../sysrole-create/sysrole-create.component';
import { SysrolePermissionComponent } from '../sysrole-permission/sysrole-permission.component';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { DataPermissionGrantComponent } from '../../data-permission/data-permission-grant/data-permission-grant.component';
import { HttpClient } from '@angular/common/http';
// import { ReportTemplateCreateComponent } from '../../report/report-template-create/report-template-create.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MatSort, SortDirection } from '@angular/material/sort';
import { environment } from 'src/environments/environment';
import { keyCodes } from 'ckeditor5';

@Component({
  selector: 'app-sysroles',
  templateUrl: './sysroles.component.html',
  styleUrls: ['./sysroles.component.scss'],
})
export class SysrolesComponent implements OnInit, AfterViewInit {
  resetFilter() {
    this.searchStr = '';
    this.applyFilter({ keyCode: 13 })
  }
  @ViewChild(MatSort) sort?: MatSort;
  users: any[] = [];
  dataSource = new MatTableDataSource<any>();
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  displayedColumns = ['seq', 'name', 'code', 'users', 'modified', 'actions'];
  searchStr: any;

  constructor(
    private sysroleService: SysroleService,
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) { }
  ngAfterViewInit(): void {
    this.onPageChange(this.pageable);
  }

  filters: any[] = [];
  ngOnInit(): void {

  }

  applyFilter($event) {
    const className = 'cn.sparrowmini.permission.model.Sysrole'
    this.searchStr = $event
    // this.onPageChange({ pageIndex: 0, pageSize: this.pageable.pageSize });
    const filter = [
      { name: 'code', operator: 'like', value: this.searchStr, type: 'OR' },
      { name: 'name', operator: 'like', value: this.searchStr, type: 'OR' }
    ]
    this.http.post(`${environment.apiBase}/sysroles/filter`, filter, { params: this.pageable }).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content)
      this.pageable.length = res.totalElements
    })
  }

  onPageChange(event: any) {
    // console.log(event);
    this.dataSource = new MatTableDataSource<any>();
    this.pageable.pageIndex = event.pageIndex;
    this.pageable.pageSize = event.pageSize;
    this.sysroleService
      .sysroles(this.filters, this.pageable.pageIndex, this.pageable.pageSize, this.pageable.sort)
      .subscribe((res) => {
        this.dataSource = new MatTableDataSource<any>(res.content);
        this.dataSource.sort = this.sort!;
        this.pageable.length = res.totalElements!
      });
  }

  new() {
    this.dialog
      .open(SysroleCreateComponent)
      .afterClosed()
      .subscribe((result) => {
        if (result) this.ngOnInit();
      });
  }

  delete(sysrole: any) {
    this.sysroleService.deleteSysroles([sysrole.id]).subscribe(() => {
      this.snack.open('删除成功！', '关闭');
    });
  }

  edit(sysrole: any) {
    this.dialog
      .open(SysroleCreateComponent, { data: sysrole })
      .afterClosed()
      .subscribe((result) => {
        if (result) this.onPageChange(this.pageable);
      });
  }

  remove(user: any, sysrole: any) {
    this.sysroleService.removeSysroleUsers([user.id.username], sysrole.id).subscribe(() => {
      this.snack.open('移除成功！', '关闭');
      this.onPageChange(this.pageable)
    });
  }

  openPermission(sysrole: any) {
    this.dialog
      .open(SysrolePermissionComponent, { data: sysrole, width: '80%' })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.onPageChange(this.pageable)
        }
      });
  }

  openDataPermission(sysrole: any) {
    this.dialog
      .open(DataPermissionGrantComponent, {
        data: sysrole,
        width: '80%',
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.ngOnInit();
        }
      });
  }

  newReport() {
    // this.dialog.open(ReportTemplateCreateComponent);
  }

}
