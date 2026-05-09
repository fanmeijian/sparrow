import { DictService } from '@sparrowmini/common-api';
import { SelectionModel } from '@angular/cdk/collections';
import { Component, ElementRef, TemplateRef, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonTreeService } from '@sparrowmini/common-api';
import { DictClass } from '../dict.service';
import { MatDialog } from '@angular/material/dialog';
import * as _ from 'lodash';

@Component({
  selector: 'app-dict-list',
  templateUrl: './dict-list.component.html',
  styleUrls: ['./dict-list.component.css']
})
export class DictListComponent {
  onTaggle($event: SelectionModel<any>) {
    this.selectedParent = $event
  }

  selectedParent?: SelectionModel<any>

  confirmParent() {
    console.log(this.checklistSelection.selected)
    const body = _.flatMap(this.checklistSelection.selected, (v1) =>
      _.map(this.selectedParent?.selected, (v2) => ({
        id: v1,
        parentId: v2.id
      }))
    );
    console.log(body)
    this.dictService.upsert(body).subscribe();
  }
  remove(_t3: { name: string; }) {
    console.log(_t3)
  }


  @ViewChild('parent') parentDlg!: TemplateRef<any>

  openParentDlg() {
    this.dialog.open(this.parentDlg, { width: '400px', height: '350px' })
  }

  // updateParent() {
  //   const body = this.checklistSelection.selected
  //   this.commonTreeService.delete(this.treeClass, body).subscribe();
  // }
  treeClass = DictClass
  onNodeClick($event: any) {
    this.router.navigate([$event.id], { relativeTo: this.route });
  }
  onTreeSelect($event: any[]) {
    console.log($event);
    this.checklistSelection.clear()
    if ($event.length > 0) {
      this.checklistSelection.select(...$event)
    }
  }
  new() {
    throw new Error('Method not implemented.');
  }
  delete() {
    const body = this.checklistSelection.selected
    this.commonTreeService.delete(this.treeClass, body).subscribe();
  }
  checklistSelection = new SelectionModel<any>(
    true /* multiple */
  );

  constructor(
    private commonTreeService: CommonTreeService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private dictService: DictService,
  ) { }
}
