import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { TodoItemFlatNode } from '../filter-tree/filter-tree.component';
import { SelectionModel } from '@angular/cdk/collections';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-new-tree',
  templateUrl: './new-tree.component.html',
  styleUrls: ['./new-tree.component.scss']
})
export class NewTreeComponent implements OnInit {
  checklistSelection: SelectionModel<TodoItemFlatNode> = new SelectionModel<TodoItemFlatNode>(false /* multiple */);
  selectedNodes($event: import("../filter-tree/filter-tree.component").TodoItemFlatNode[]) {
    console.log($event);
  }


  fg: UntypedFormGroup = this.formBuilder.group({
    name: [null, Validators.required],
    code: [null, Validators.required],
    parentId: [null],
  });

  submit() {
    if(this.checklistSelection.selected[0]){
      this.fg.patchValue({parentId: this.checklistSelection.selected[0].id});
    }
    if(this.fg.valid){
      this.dialogRef.close(this.fg.value);
    }
  }

  constructor(
    private formBuilder: UntypedFormBuilder,
    @Inject(MAT_DIALOG_DATA) public treeData: any,
    private dialogRef: MatDialogRef<NewTreeComponent>,
  ) { }

  ngOnInit(): void {
  }

}
