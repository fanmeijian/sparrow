import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';
import { FormService, Sysconfig, SysconfigService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-sysconfig-form',
  templateUrl: './sysconfig-form.component.html',
  styleUrls: ['./sysconfig-form.component.scss']
})
export class SysconfigFormComponent implements OnInit {
  form: any;
  formId: string='';
  formData: any = {}
  viewOnly:boolean = true

  formOptions = {
    // fileService: this.formioFileService,
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public config: any|Sysconfig,
    private activatedRoute: ActivatedRoute,
    private formService: FormService,
    private sysconfigService: SysconfigService,
    private dialogRef: MatDialogRef<SysconfigFormComponent>,
    // private formioFileService: CosFileService,
  ) { }

  ngOnInit(): void {
    this.form=JSON.parse(this.config.form)
    this.formData = {data:JSON.parse(this.config.configJson||'{}')}
    if(this.config.action!='view'){
      this.viewOnly=false
    }
  }

  onSubmit(e: any){
    this.sysconfigService.updateConfig({configJson: JSON.stringify(e.data)}, this.config.code).subscribe(()=>{
      this.dialogRef.close(true)
    })

  }
}
