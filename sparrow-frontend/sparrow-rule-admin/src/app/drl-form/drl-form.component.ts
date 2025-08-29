import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { RuleService } from '../services/rule.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-drl-form',
  templateUrl: './drl-form.component.html',
  styleUrls: ['./drl-form.component.css']
})
export class DrlFormComponent implements OnInit {
  onChange($event: Event) {
    console.log($event);
  }

  monaco: any
  code: string = `rule "sample rule"\nwhen\nthen\nend`;

  editorOptions = { language: 'drools', theme: 'vs-dark' };


  constructor(
    private fb: FormBuilder,
    private ruleService: RuleService,
    private route: ActivatedRoute,
  ) {

  }
  ngOnInit(): void {
    this.route.queryParams.subscribe((params: any) => {
      if (params.id) {
        this.ruleService.getDrl(params.id).subscribe((res: any) => {
          this.form.patchValue(res)
          this.code = res.content
        })
      }
    });
  }
  onSubmit() {
    console.log(this.code)
    this.form.patchValue({ content: this.code })
    if (this.form.value.id) {
      this.ruleService.updateDrl([this.form.value]).subscribe()
    } else {
      this.ruleService.saveDrl([this.form.value]).subscribe()
    }

  }
  form: FormGroup = new FormGroup({
    id: new FormControl(null),
    name: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    content: new FormControl(null, Validators.required),
  });



}
