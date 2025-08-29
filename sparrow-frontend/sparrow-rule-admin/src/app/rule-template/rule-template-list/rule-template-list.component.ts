import { Component } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { RuleTemplateClass } from '../rule-template-form/rule-template-form.component';
import { CommonApiService } from '@sparrowmini/common-api';

@Component({
  selector: 'app-rule-template-list',
  templateUrl: './rule-template-list.component.html',
  styleUrls: ['./rule-template-list.component.css']
})
export class RuleTemplateListComponent {
  pageable: any = { page: 0, size: 10, sort: [], length: 0 }
  data: any;

  onDelete($event: any[]) {
    this.commonApi.delete(RuleTemplateClass, $event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    if ($event) {
      this.pageable.page = $event.pageIndex || this.pageable.page
      this.pageable.size = $event.pageSize || this.pageable.size
    }
    this.commonApi.filter(RuleTemplateClass, this.pageable, undefined).subscribe((res: any) => {
      this.data = res.content
      this.total = res.page.totalElements
    })
  }

  constructor(
    private commonApi: CommonApiService,
  ) { }
}
