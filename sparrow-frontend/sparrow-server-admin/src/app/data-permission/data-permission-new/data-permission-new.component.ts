import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { DataPermissionService, Model } from '@sparrowmini/org-api';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { DataPermissionGrantComponent } from '../data-permission-grant/data-permission-grant.component';
import { showContent, showId } from '../../util/model-util';

@Component({
  selector: 'app-data-permission-new',
  templateUrl: './data-permission-new.component.html',
  styleUrls: ['./data-permission-new.component.scss'],
})
export class DataPermissionNewComponent implements OnInit {
  models: Model[] = [];
  dataSource = new MatTableDataSource<any>();
  pageable = { pageIndex: 0, pageSize: 10, length: 0, sort: [] };

  displayedColumns = ['seq', 'name', 'actions'];

  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
    private dataPermissionService: DataPermissionService,
  ) { }

  ngOnInit(): void {
    this.dataPermissionService.entityList(this.pageable.pageIndex,this.pageable.pageSize).subscribe((res) => {
      this.models = res.content
    })
  }

  selectionChange(e) {
    console.log(e)
    let id = e.value.id
    this.dataPermissionService.entityDataList(id, this.pageable.pageIndex, this.pageable.pageSize,this.pageable.sort).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content)
      this.pageable.length = res.totalElements
    })
  }
  onPageChange(e) {

  }

  grantPermission(element) {
    this.dialog.open(DataPermissionGrantComponent, { data: element })
  }

  showId = showId
  showContent = showContent

  // showId(id: any) {
  //   if (typeof id === 'object') {
  //     return JSON.stringify(id)
  //   } else {
  //     return id
  //   }

  // }

  // showContent(_content: any) {
  //   if (typeof _content === 'object') {
  //     let content = Object.assign({}, _content)
  //     delete content.id
  //     delete content.createdDate
  //     delete content.createdBy
  //     delete content.modifiedDate
  //     delete content.modifiedBy
  //     delete content.version
  //     delete content.stat
  //     delete content.modelName
  //     delete content.entityStat
  //     delete content.enabled
  //     delete content.dataPermissionTokenId
  //     delete content.dataPermissionId
  //     delete content.dataPermission
  //     delete content.errMsgs
  //     delete content.permissionTokenId

  //     return JSON.stringify(content)
  //   } else {
  //     return _content
  //   }
  // }
}
