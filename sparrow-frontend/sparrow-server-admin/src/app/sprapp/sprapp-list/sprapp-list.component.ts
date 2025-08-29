import { SelectionModel } from '@angular/cdk/collections';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-sprapp-list',
  templateUrl: './sprapp-list.component.html',
  styleUrls: ['./sprapp-list.component.scss']
})
export class SprappListComponent implements OnInit {
  confirm() {
    console.log(this.selected.selected);
    this.dialogRef.close(this.selected.selected)

  }

  selected = new SelectionModel<any>(false);
  columns = []

  displayedColumns: any;
  dataSource: MatTableDataSource<any> = new MatTableDataSource([]);

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<SprappListComponent>,
  ) {
    this.columns = [
      { name: '名称', code: 'name' },
      { name: 'code', code: 'code' },
    ]

    this.displayedColumns = ['#'].concat(this.columns.map(m => m.code))
  }

  ngOnInit(): void {
    const className = 'cn.sparrowmini.permission.app.App'
    this.http.post(`${environment.apiBase}/common-jpa-controller/${className}/filter`, []).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content)
    })
  }

  getColumnValue(column: string) {

  }

}
