import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { RuleClass } from '../rule-form/rule-form.component';
import { PageEvent } from '@angular/material/paginator';
import { CommonApiService } from '@sparrowmini/common-api';

@Component({
  selector: 'app-rule-list',
  templateUrl: './rule-list.component.html',
  styleUrls: ['./rule-list.component.css']
})
export class RuleListComponent implements OnChanges {

  @Input() templateId!: string

  pageable: any = { page: 0, size: 10, sort: [], length: 0 }
  data: any;

  onDelete($event: any[]) {
    this.commonApi.delete(RuleClass, $event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    if ($event) {
      this.pageable.page = $event.pageIndex || this.pageable.page
      this.pageable.size = $event.pageSize || this.pageable.size
    }
    this.commonApi.filter(RuleClass, this.pageable, `templateId='${this.templateId}'`).subscribe((res: any) => {
      this.data = res.content
      this.total = res.page.totalElements
    })
  }

  constructor(
    private commonApi: CommonApiService,
  ) { }
  ngOnChanges(changes: SimpleChanges): void {
    this.onPage({
      pageIndex: 0,
      pageSize: 10,
      length: 0
    });
  }
}
