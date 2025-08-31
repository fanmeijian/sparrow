import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup, FormGroup, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CommonTreeService } from '@sparrowmini/common-api';
import { DictClass } from '../dict.service';

@Component({
  selector: 'app-dict-form',
  templateUrl: './dict-form.component.html',
  styleUrls: ['./dict-form.component.css']
})
export class DictFormComponent implements OnInit {
  treeClass = DictClass
  onTreeSelect($event: any[]) {
    this.formGroup.patchValue({ parentId: $event[0] });
  }
  treeNode: any
  submit() {
    this.commonTreeService.upsert(this.treeClass, [this.formGroup.value]).subscribe();
  }
  formGroup: UntypedFormGroup = new FormGroup({
    name: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    parentId: new FormControl(),
    id: new FormControl(),
  });

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private commonTreeService: CommonTreeService,
  ) { }
  ngOnInit(): void {
    this.route.params.subscribe((params: any) => {
      const id = params.id
      if (id && id !== 'new') {
        this.formGroup.disable()
        this.commonTreeService.get(this.treeClass, id).subscribe(res => {
          this.formGroup.patchValue(res)
          this.treeNode = res
        });
      } else {
        this.formGroup.enable()
        this.formGroup.reset()
      }
    });

  }
}
