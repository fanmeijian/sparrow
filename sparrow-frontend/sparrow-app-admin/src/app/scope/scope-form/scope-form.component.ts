import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup, Validators, UntypedFormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonApiService } from '@sparrowmini/common-api';
import { ScopeClass } from '../scope-list/scope-list.component';

@Component({
  selector: 'app-scope-form',
  templateUrl: './scope-form.component.html',
  styleUrls: ['./scope-form.component.css']
})
export class ScopeFormComponent implements OnInit {

  className = ScopeClass

  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    code: [null, Validators.required],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: UntypedFormBuilder,
    private sysroleService: CommonApiService,
    private dialogRef: MatDialogRef<any>,
    private snack: MatSnackBar
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
    this.sysroleService.upsert(this.className, body).subscribe(() => {
      this.dialogRef.close(true);
      this.snack.open("保存成功！", "关闭");
    })

  }

}
