import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup, Validators, UntypedFormBuilder } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { PageElementService, ScopeService } from '@sparrowmini/org-api';
import { SysroleCreateComponent } from '../../sysrole/sysrole-create/sysrole-create.component';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
export const pageElementClassName = 'cn.sparrowmini.permission.pageelement.model.PageElement'
@Component({
  selector: 'app-page-element-create',
  templateUrl: './page-element-create.component.html',
  styleUrls: ['./page-element-create.component.scss'],
})
export class PageElementCreateComponent implements OnInit {
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    id: [null, Validators.required],
    pageId: [null, Validators.required],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: UntypedFormBuilder,
    private pageElementService: PageElementService,
    private dialogRef: MatDialogRef<PageElementCreateComponent>,
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
    if (this.formGroup.valid) {
      if (this.data) {
        const body = [{ ...this.formGroup.value, id: this.data.id }]
        this.http.patch(`${environment.jpaBase}/${pageElementClassName}`, body).subscribe(() => {
          this.snack.open('保存成功！', '关闭');
          this.dialogRef.close(true);
        })

      } else {
        this.pageElementService.newPageElement([this.formGroup.value]).subscribe(() => {
          this.dialogRef.close(true);
          this.snack.open('保存成功！', '关闭');
        });
      }
    }
  }
}
