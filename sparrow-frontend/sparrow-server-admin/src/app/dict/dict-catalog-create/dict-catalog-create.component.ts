import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { DictService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-dict-catalog-create',
  templateUrl: './dict-catalog-create.component.html',
  styleUrls: ['./dict-catalog-create.component.scss'],
})
export class DictCatalogCreateComponent implements OnInit {
  formGroup: UntypedFormGroup = this.fb.group({
    code: [null, Validators.required],
    name: [null, Validators.required],
  });

  constructor(private dictService: DictService, private fb: UntypedFormBuilder,
    private dialogRef:MatDialogRef<DictCatalogCreateComponent>
  ) {}

  ngOnInit(): void {}

  submit() {
    this.dictService.newCatalog([this.formGroup.value]).subscribe(()=>this.dialogRef.close(true));
  }
}
