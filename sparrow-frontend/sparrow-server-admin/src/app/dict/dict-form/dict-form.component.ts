import { AfterViewInit, Component, Inject, OnInit } from '@angular/core';
import { BASE_PATH, Dict, DictService } from '@sparrowmini/org-api';
import { DynamicFlatNode } from '../dicts/dict-tree-datasource';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { DictCreateComponent } from '../dict-create/dict-create.component';
import { SelectionModel } from '@angular/cdk/collections';
import { DictTreeService } from '../dicts/dict-tree.service';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TreeViewOption } from '../../common/tree-view/tree-view.component';
import { BehaviorSubject, Subject } from 'rxjs';

@Component({
  selector: 'app-dict-form',
  templateUrl: './dict-form.component.html',
  styleUrls: ['./dict-form.component.scss']
})
export class DictFormComponent implements OnInit {

  treeViewOption: TreeViewOption = {
    checklistSelection: new SelectionModel<any>(true /* multiple */),
    treeData: [],
    parentSelectable: false,
  }
  window = window

  selectedItems: DynamicFlatNode[] = [];

  // checklistSelection: SelectionModel<any> = new SelectionModel<any>(true /* multiple */);
  // checkedIds?: string[] = [];
  // treeData: any

  formGroup: UntypedFormGroup = this.fb.group({
    id: [null],
    code: [null, Validators.required],
    name: [null, Validators.required],
    parentId: [null],
  });


  constructor(private dictService: DictService, private fb: UntypedFormBuilder,
    private dictTreeService: DictTreeService,
    private route: ActivatedRoute,
    private http: HttpClient,
    @Inject(BASE_PATH) private apiBase: string,
  ) {
    this.dictTreeService.getChildren('root').subscribe((res: any) => {
      this.treeViewOption.treeData = res.content
      this.treeViewOption = Object.assign({},this.treeViewOption, { treeData: res.content })
    })
  }


  ngOnInit(): void {

    this.route.params.subscribe((params: any) => {
      if (params.id && params.id !== 'dict-form') {
        this.http.get(`${this.apiBase}/dicts/${params.id}`).subscribe((res: any) => {
          this.formGroup.patchValue(res)
          this.treeViewOption.checkedIds = [res.parentId]
          this.formGroup.disable()
          this.treeViewOption = Object.assign({},this.treeViewOption, { disabled: true })
        })
      } else {
        this.formGroup.enable()
      }
    })
  }

  submit() {

    let selectedIds = this.treeViewOption.checklistSelection.selected.map(m => m.id)
    this.formGroup.patchValue({
      parentId: selectedIds[0],
    });

    if (this.formGroup.valid) {
      let body = this.formGroup.getRawValue()
      if (this.formGroup.value.id) {
        this.http.patch(`${this.apiBase}/dicts/${this.formGroup.value.id}`, body).subscribe(() => {
          this.formGroup.disable()
        })
      } else {
        this.dictService.newDict([body]).subscribe((res) => {
          this.formGroup.patchValue({ id: res.id })
          this.formGroup.disable()
        });
      }

    }
  }
}
