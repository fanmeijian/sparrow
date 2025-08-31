import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup, Validators, UntypedFormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonApiService } from '@sparrowmini/common-api';
import { environment } from 'src/environments/environment';
import { PageElementClass } from '../page-element-list/page-element-list.component';

@Component({
  selector: 'app-page-element-form',
  templateUrl: './page-element-form.component.html',
  styleUrls: ['./page-element-form.component.css']
})
export class PageElementFormComponent implements OnInit {
  className = PageElementClass
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    id: [null, Validators.required],
    pageId: [null, Validators.required],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: UntypedFormBuilder,
    private commonApiService: CommonApiService,
    private dialogRef: MatDialogRef<any>,
    private snack: MatSnackBar,
    private http: HttpClient,
  ) { }

  ngOnInit(): void {
    if (this.data) {
      this.formGroup.patchValue(this.data);
    }
  }

  submit() {
    this.formGroup.markAllAsTouched();
    if (this.formGroup.invalid) return

    const body = [this.formGroup.value]
    this.commonApiService.upsert(this.className, body).subscribe(() => {
      this.snack.open('保存成功！', '关闭');
      this.dialogRef.close(true);
    })

  }
}
