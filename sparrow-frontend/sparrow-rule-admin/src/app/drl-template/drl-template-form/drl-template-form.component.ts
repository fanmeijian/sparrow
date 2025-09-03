import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonApiService } from '@sparrowmini/common-api';
import { DrlTemplateClass } from '../drl-template-list/drl-template-list.component';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-drl-template-form',
  templateUrl: './drl-template-form.component.html',
  styleUrls: ['./drl-template-form.component.css']
})
export class DrlTemplateFormComponent implements OnInit {

  @ViewChild('previewDlg') previewDlg!: TemplateRef<any>

  previewStr: any;
  preview() {
    const id = this.form.value.id
    this.http.get(`${environment.apiBase}/rules/drl-templates/${id}/preview`).subscribe(res => {
      console.log(res)
      this.dialog.open(this.previewDlg,{ width: '80%', height: '60%', data: res})
    });
  }
  constructor(
    private commonApiService: CommonApiService,
    private route: ActivatedRoute,
    private http: HttpClient,
    private dialog: MatDialog,
  ) { }
  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const id = params.get("id")
      this.commonApiService.get(DrlTemplateClass, id).subscribe(res => {
        this.form.patchValue(res)
      });
    })

  }
  submit() {
    this.commonApiService.upsert(DrlTemplateClass, [this.form.value]).subscribe(res => {

    });
  }
  form: FormGroup = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    remark: new FormControl(null),
    head: new FormControl(
      `package com.company;
import java.util.*;`, Validators.required),
  })

}
