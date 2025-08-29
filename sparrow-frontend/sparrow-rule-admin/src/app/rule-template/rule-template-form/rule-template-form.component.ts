import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { RuleFormComponent } from '../rule-form/rule-form.component';
import { CommonApiService } from '@sparrowmini/common-api';
import { ActivatedRoute } from '@angular/router';
export const RuleTemplateClass = 'cn.sparrowmini.rules.model.RuleTemplate'

@Component({
  selector: 'app-rule-template-form',
  templateUrl: './rule-template-form.component.html',
  styleUrls: ['./rule-template-form.component.css']
})
export class RuleTemplateFormComponent implements OnInit {
  onSubmit() {
    if (this.form.invalid) return;
    const body = [this.form.value]
    this.commonApi.upsert(RuleTemplateClass, body).subscribe()
  }
  newRule() {
    this.dialog.open(RuleFormComponent, { width: '100%', hasBackdrop: false, data: this.form.value });
  }

  form: FormGroup<any> = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    remark: new FormControl(null),
    head: new FormGroup({
      packageName: new FormControl(null, Validators.required),
      imports: new FormControl(null)
    })
  });

  constructor(
    private dialog: MatDialog,
    private commonApi: CommonApiService,
    private route: ActivatedRoute,
  ) { }
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      if (params.id) {
        this.commonApi.get(RuleTemplateClass, params.id).subscribe(res => {
          this.form.patchValue(res)
        })
      }
    });
  }


}
