import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { SysroleService } from '@sparrowmini/org-api';
import { environment } from 'src/environments/environment';

export const sysroleClassName = 'cn.sparrowmini.permission.sysrole.model.Sysrole'

@Component({
  selector: 'app-sysrole-create',
  templateUrl: './sysrole-create.component.html',
  styleUrls: ['./sysrole-create.component.scss'],
})
export class SysroleCreateComponent implements OnInit {
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    code: [null, Validators.required],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: UntypedFormBuilder,
    private sysroleService: SysroleService,
    private dialogRef: MatDialogRef<SysroleCreateComponent>,
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
        this.http.patch(`${environment.jpaBase}/${sysroleClassName}`, body).subscribe(() => {
          this.snack.open('保存成功！', '关闭');
          this.dialogRef.close(true);
        })
      } else {
        this.sysroleService.newSysrole(this.formGroup.value).subscribe(() => {
          this.dialogRef.close(true);
          this.snack.open('保存成功！', '关闭');
        });
      }
    }
  }
}
