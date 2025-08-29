import { SelectionModel } from '@angular/cdk/collections';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-selection',
  templateUrl: './process-selection.component.html',
  styleUrls: ['./process-selection.component.css']
})
export class ProcessSelectionComponent implements OnInit {
  confirm() {
    console.log(this.selection.selected);
    this.dialogRef.close(this.selection.selected.map(m=>m.id))
  }
  checkboxLabel(arg0: any) {

  }
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);
  displayedColumns = ['#', 'name'];
  selection = new SelectionModel<any>(true, []);

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<ProcessSelectionComponent>,
  ) { }

  ngOnInit(): void {
    this.http.get(`${environment.bpmApi}/process-design`).subscribe((res: any) => {
      this.dataSource = new MatTableDataSource(res.content);
    })
  }

}
