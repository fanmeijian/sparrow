import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { ProcessSelectionComponent } from 'src/app/process/process-selection/process-selection.component';
import { TaskSelectionComponent } from 'src/app/task/task-selection/task-selection.component';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-form-design-list',
  templateUrl: './form-design-list.component.html',
  styleUrls: ['./form-design-list.component.css']
})
export class FormDesignListComponent implements OnInit {
  linkTask(element: any) {
    this.dialog.open(TaskSelectionComponent,{ width: '80%'}).afterClosed().subscribe(res => {
      const body = res.map((m: any) => {
        return { ...m, formId: element.id }
      })
      this.http.post(`${environment.bpmApi}/forms/task-forms`, body).subscribe();
    })
  }
  linkProcess(element: any) {
    this.dialog.open(ProcessSelectionComponent,{ width: '80%'}).afterClosed().subscribe(res => {
      const body = res.map((m: any) => {
        return { ...m, formId: element.id }
      })
      this.http.post(`${environment.bpmApi}/forms/process-forms`, body).subscribe();
    })
    // const body = {}
    // this.http.post(`${environment.bpmApi}/forms/process-forms`,body).subscribe();
  }
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);
  displayedColumns = ['name', 'process', 'task', 'action'];

  constructor(
    public route: ActivatedRoute,
    private http: HttpClient,
    private dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.http.get(`${environment.bpmApi}/forms`).subscribe((res: any) => {
      console.log(res.content)
      this.dataSource = new MatTableDataSource(res.content);
    })
  }

}
