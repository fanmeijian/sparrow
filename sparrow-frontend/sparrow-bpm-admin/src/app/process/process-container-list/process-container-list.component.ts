import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-container-list',
  templateUrl: './process-container-list.component.html',
  styleUrls: ['./process-container-list.component.css']
})
export class ProcessContainerListComponent implements OnInit {
  delete(element: any) {
    const httpParams = new HttpParams({ fromObject: element.id })
    this.http.delete(`${environment.bpmApi}/process-design/containers`, { params: httpParams }).subscribe();
  }
  edit(element: any) {
    this.formGroup.patchValue(element)
    this.formGroup.get('id')?.disable()
    this.dialogRef = this.dialog.open(this.containerDlg, { data: element });
  }
  dataSource: any;
  dialogRef?: MatDialogRef<any>

  deploy(element: any) {
    this.http.post(`${environment.bpmApi}/process-design/deploy`, element.id).subscribe()
  }

  unDeploy(element: any) {
    this.http.post(`${environment.bpmApi}/process-design/un-deploy`, [element.id]).subscribe()
  }

  ngSubmit() {
    if(this.formGroup.invalid){
      return
    }
    const body = this.formGroup.getRawValue()
    this.http.post(`${environment.bpmApi}/process-design/containers`, body).subscribe(() => {
      this.dialogRef?.close()
    });
  }
  @ViewChild('containerDlg') containerDlg!: TemplateRef<any>

  pageable = { page: 0, size: 10, length: 0, sort: ['createdDate,desc'] }
  onPage($event: any) {
    this.pageable.page = $event.pageIndex
    this.pageable.size = $event.pageSize
    const httpParams = new HttpParams({ fromObject: this.pageable })
    this.http.get(`${environment.bpmApi}/process-design/containers`, { params: httpParams }).subscribe((res: any) => {
      this.dataSource = res.content
      this.pageable.length = res.totalElements
    });
  }

  add() {
    this.formGroup.get('id')?.enable()
    this.dialogRef = this.dialog.open(this.containerDlg);
    console.log(this.dialogRef)
  }
  formGroup: UntypedFormGroup = this.formBuilder.group({
    id: this.formBuilder.group({
      groupId: [null, Validators.required],
      artifactId: [null, Validators.required],
      version: [null, Validators.required],
    }),
    name: [null, Validators.required],
    remark: [null],
  });

  constructor(
    private formBuilder: UntypedFormBuilder,
    private dialog: MatDialog,
    private http: HttpClient,
    public route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.onPage({ pageIndex: this.pageable.page, pageSize: this.pageable.size })
  }

}
