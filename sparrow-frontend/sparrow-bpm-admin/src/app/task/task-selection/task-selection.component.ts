import { SelectionModel } from '@angular/cdk/collections';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ProcessSelectionComponent } from 'src/app/process/process-selection/process-selection.component';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-task-selection',
  templateUrl: './task-selection.component.html',
  styleUrls: ['./task-selection.component.css']
})
export class TaskSelectionComponent implements OnInit {
  confirm() {
    console.log(this.selection.selected);
    this.dialogRef.close(this.selection.selected.map(m => Object.assign({}, m.processId, { taskName: m.taskName })))
  }
  checkboxLabel(arg0: any) {

  }
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);
  displayedColumns = ['#', 'task', 'process'];
  selection = new SelectionModel<any>(true, []);

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<ProcessSelectionComponent>,
  ) { }

  ngOnInit(): void {
    this.http.get(`${environment.bpmApi}/process-design`).subscribe((res: any) => {
      const tasks: any[] = []
      res.content.forEach((p: any) => {
        p.tasks.forEach((task: any) => {
          tasks.push({
            processId: p.id,
            processName: p.processName,
            ...task,
          })
        })
      })
      this.dataSource = new MatTableDataSource(tasks);
    })
  }

}
