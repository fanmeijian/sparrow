import { SelectionModel } from '@angular/cdk/collections';
import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonTreeService } from '@sparrowmini/common-api';
import { DictClass } from '../dict.service';

@Component({
  selector: 'app-dict-list',
  templateUrl: './dict-list.component.html',
  styleUrls: ['./dict-list.component.css']
})
export class DictListComponent {
  treeClass = DictClass
  onNodeClick($event: any) {
    this.router.navigate([$event.id], { relativeTo: this.route });
  }
  onTreeSelect($event: any[]) {
    console.log($event);
    this.checklistSelection.clear()
    if ($event.length > 0) {
      this.checklistSelection.select($event)
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
  ) { }
}
