import { Component } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { CommonApiService } from '@sparrowmini/common-api';
import { DslrClass } from 'src/app/dsl/dslr-list/dslr-list.component';
export const DrlTemplateClass= 'cn.sparrowmini.rules.model.DrlTemplate'

@Component({
  selector: 'app-drl-template-list',
  templateUrl: './drl-template-list.component.html',
  styleUrls: ['./drl-template-list.component.css']
})
export class DrlTemplateListComponent {
  pageable: any = { page: 0, size: 10, sort: [], length: 0 }
  data: any;

  onDelete($event: any[]) {
    this.commonApi.delete(DrlTemplateClass, $event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    if ($event) {
      this.pageable.page = $event.pageIndex || this.pageable.page
      this.pageable.size = $event.pageSize || this.pageable.size
    }
    this.commonApi.filter(DrlTemplateClass, this.pageable, undefined).subscribe((res: any) => {
      this.data = res.content
      this.total = res.page.totalElements
    })
  }

  constructor(
    private commonApi: CommonApiService,
  ) { }
}
