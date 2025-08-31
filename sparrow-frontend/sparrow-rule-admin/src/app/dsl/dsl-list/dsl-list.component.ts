import { Component } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { CommonApiService } from '@sparrowmini/common-api';
import { RuleTemplateClass } from 'src/app/rule-template/rule-template-form/rule-template-form.component';

export const DslClass='cn.sparrowmini.rules.model.Dsl'

@Component({
  selector: 'app-dsl-list',
  templateUrl: './dsl-list.component.html',
  styleUrls: ['./dsl-list.component.css']
})
export class DslListComponent {
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
    this.commonApi.filter(DslClass, this.pageable, undefined).subscribe((res: any) => {
      this.data = res.content
      this.total = res.page.totalElements
    })
  }

  constructor(
    private commonApi: CommonApiService,
  ) { }
}
