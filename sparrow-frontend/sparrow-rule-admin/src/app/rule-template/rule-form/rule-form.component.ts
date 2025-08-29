import { Component, Inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService } from '@sparrowmini/common-api';
export const RuleClass = 'cn.sparrowmini.rules.model.Rule'

@Component({
  selector: 'app-rule-form',
  templateUrl: './rule-form.component.html',
  styleUrls: ['./rule-form.component.css']
})
export class RuleFormComponent implements OnInit {

  form: FormGroup = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    remark: new FormControl(null),
    templateId: new FormControl(null, Validators.required),
    content: new FormControl(null, Validators.required),
  })

  submit() {
    const body = [Object.assign({}, this.form.value, { content: this.code })]
    this.commonApi.upsert(RuleClass, body).subscribe()
  }
  onChange($event: Event) {
    console.log($event);
  }
  code: string = `rule "sample rule"\nwhen\nthen\nend`;
  editorOptions = { language: 'drools', theme: 'vs-dark' };

  constructor(
    private route: ActivatedRoute,
    private commonApi: CommonApiService,
  ) { }
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      if (params.id) {
        this.commonApi.get(RuleClass, params.id).subscribe((res: any) => {
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
