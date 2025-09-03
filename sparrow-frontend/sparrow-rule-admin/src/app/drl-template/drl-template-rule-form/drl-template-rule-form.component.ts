import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { CommonApiService } from '@sparrowmini/common-api';
import { DrlTemplateRuleClass } from '../drl-template-rule-list/drl-template-rule-list.component';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
  selector: 'app-drl-template-rule-form',
  templateUrl: './drl-template-rule-form.component.html',
  styleUrls: ['./drl-template-rule-form.component.css']
})
export class DrlTemplateRuleFormComponent implements OnInit {
  constructor(
    private commonApiService: CommonApiService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }
  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const id = params.get("id")
      this.commonApiService.get(DrlTemplateRuleClass, id).subscribe(res => {
        this.form.patchValue(res)
      });

      const drlTemplateId = params.get('drlTemplateId')
      this.form.patchValue({ drlTemplateId: drlTemplateId })
    })

  }
  submit() {
    this.commonApiService.upsert(DrlTemplateRuleClass, [this.form.value]).subscribe(res => {
      this.router.navigate(['../drl-template-form'], {relativeTo: this.route, queryParams: {drlTemplateId: this.form.value.drlTemplateId,id: this.form.value.drlTemplateId}})
    });
  }
  form: FormGroup = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    enabled: new FormControl(true, Validators.required),
    description: new FormControl(null),
    drlTemplateId: new FormControl(null),
    content: new FormControl(
      `rule ""
  when

  then

end`, Validators.required),
  })
}
