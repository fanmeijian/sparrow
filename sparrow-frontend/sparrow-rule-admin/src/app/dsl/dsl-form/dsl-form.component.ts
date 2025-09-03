import { Component } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService } from '@sparrowmini/common-api';
import { RuleClass } from 'src/app/rule-template/rule-form/rule-form.component';
import { DslClass } from '../dsl-list/dsl-list.component';
import { MatDialog } from '@angular/material/dialog';
import { DslrFormComponent } from '../dslr-form/dslr-form.component';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { DslPreviewComponent } from '../dsl-preview/dsl-preview.component';

@Component({
  selector: 'app-dsl-form',
  templateUrl: './dsl-form.component.html',
  styleUrls: ['./dsl-form.component.css']
})
export class DslFormComponent {
  getDrl() {
    this.dialog.open(DslPreviewComponent, {width: '80%', data: this.dsl, hasBackdrop: false})
  }
  dsl: any
  addDslr() {
    this.dialog.open(DslrFormComponent, { width: '80%', data: this.dsl });
  }

  form: FormGroup = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    remark: new FormControl(null),
    head: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    content: new FormControl(null, Validators.required),
  })

  submit() {
    const body = [this.form.value]
    this.commonApi.upsert(DslClass, body).subscribe()
  }
  onChange($event: Event) {
    console.log($event);
  }
  code: string = `rule "sample rule"\nwhen\nthen\nend`;
  editorOptions = { language: 'drools', theme: 'vs-dark' };

  constructor(
    private route: ActivatedRoute,
    private commonApi: CommonApiService,
    private dialog: MatDialog,
    private http: HttpClient,
  ) { }
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      if (params.id) {
        this.commonApi.get(DslClass, params.id).subscribe((res: any) => {
          this.dsl = res
          this.form.patchValue(res)
          this.code = this.form.value.content
        })
      } else {
        if (params.templateId) {
          this.form.patchValue({ templateId: params.templateId })
        }
      }
    });
  }
}
