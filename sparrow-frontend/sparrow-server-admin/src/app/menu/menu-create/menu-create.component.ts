import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MenuService } from '@sparrowmini/org-api';
import { environment } from 'src/environments/environment';
import { menuClassName } from '../menu/menu.component';

@Component({
  selector: 'app-menu-create',
  templateUrl: './menu-create.component.html',
  styleUrls: ['./menu-create.component.scss'],
})
export class MenuCreateComponent implements OnInit {
  parentId: any[] = []
  linkType: any;
  submit() {
    // console.log(this.formGroup.value)
    this.formGroup.markAllAsTouched();
    if (this.formGroup.valid) {
      if (this.formGroup.value.id) {
        const body = [this.formGroup.value]

        this.http.patch(`${environment.jpaBase}/${menuClassName}`, body).subscribe(() => {
          this.dialogRef.close()
          this.snack.open('保存成功！', '关闭')
        })

      } else {
        this.http.post(`${'http://localhost:8080'}/menu`, this.formGroup.value).subscribe(() => {
          this.dialogRef.close()
          this.snack.open('保存成功！', '关闭')
        })
      }

    }
  }
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    code: [null, Validators.required],
    icon: [null],
    url: [null, Validators.required],
    target: [null],
    type: [null, Validators.required],
    queryParams: [null],
    parentId: [null],
    id: [null],
  });

  constructor(
    private formBuilder: UntypedFormBuilder,
    private menuService: MenuService,
    private dialogRef: MatDialogRef<MenuCreateComponent>,
    private snack: MatSnackBar,
    private http: HttpClient,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) { }

  ngOnInit(): void {
    if (this.data) {
      this.formGroup.patchValue(this.data)
    }

  }

  onSelected($event: any) {
    console.log($event)
    this.formGroup.patchValue({ parentId: $event })
  }
}
