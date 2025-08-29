import { Component, ElementRef, EventEmitter, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
// import { FormioRefreshValue } from '@formio/angular';
import { FormService, SysconfigService } from '@sparrowmini/org-api';
// import { Formio } from 'formiojs';
// import { CosFileService } from '../../services/cos-file.service';
// import Prism from 'prismjs';

@Component({
  selector: 'app-sysconfig-design',
  templateUrl: './sysconfig-design.component.html',
  styleUrls: ['./sysconfig-design.component.scss']
})
export class SysconfigDesignComponent implements OnInit {
  window = window;
  columns: any[] = []
  save() {
    this.formGroup.patchValue({ form: JSON.stringify(this.formJson) });
    this.formGroup.markAllAsTouched();
    if (this.formGroup.valid) {
      if (this.formGroup.getRawValue().code) {
        this.formService
          .updateConfig(this.formGroup.value, this.formGroup.getRawValue().code)
          .subscribe(() => window.history.back());

      } else {
        this.sysconfigService.createConfig([this.formGroup.value]).subscribe()
      }
    }
  }
  formJson: any;
  formOptions = {
    // fileService: this.formioFileService,
  }
  formGroup: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    code: [null, Validators.required],
    form: [{ components: [] }, Validators.required],
    displayColumns: [null],
  });

  @ViewChild('json', { static: true }) jsonElement?: ElementRef;
  @ViewChild('code', { static: true }) codeElement?: ElementRef;
  public form: any = { components: [] };
  // public refreshForm: EventEmitter<FormioRefreshValue> = new EventEmitter();

  constructor(
    private formService: SysconfigService,
    private activatedRoute: ActivatedRoute,
    private dialog: MatDialog,
    private formBuilder: UntypedFormBuilder,
    private snackBar: MatSnackBar,
    private router: Router,
    private sysconfigService: SysconfigService,
    // private formioFileService: CosFileService,

  ) {
    this.form = { components: [] };
    this.formJson = { components: [] };
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params: any) => {
      if (params.code) {
        this.formService.getConfig(params.code).subscribe((res) => {
          this.form = JSON.parse(res.form!);
          this.formJson = this.form;
          this.formGroup.patchValue({ code: res.code, name: res.name, remark: res.remark, form: res.form });
          let a: any[] = this.form.components.filter((f: any) => f.widget && f.widget.type == 'input').map((m: any) => Object.assign({}, { name: m.label, code: m.key }))
          this.columns = a
          this.formGroup.get('code')?.disable()
        });
      }
    });
  }

  onChange(event: any) {
    let a: any[] = this.formJson.components.filter((f: any) => f.widget && f.widget.type == 'input').map((m: any) => Object.assign({}, { name: m.label, code: m.key }))
    this.columns = a
  }

  @ViewChild('formbuilder') formbuilder: any
  ngAfterViewInit() {

  }


  @ViewChild('soruceCode') soruceCodeTemplate!: TemplateRef<any>;
  viewSource() {
    this.dialog.open(this.soruceCodeTemplate, { width: '80%', height: '80%' });
  }

  @ViewChild('preview') previewTemplate!: TemplateRef<any>;
  preview1() {
    this.dialog.open(this.previewTemplate, { width: '80%', height: '80%' });
  }
}
