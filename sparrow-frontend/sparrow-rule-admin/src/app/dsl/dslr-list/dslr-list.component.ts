import { Component } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { CommonApiService } from '@sparrowmini/common-api';
export const DslrClass='cn.sparrowmini.rules.model.Dslr'

@Component({
  selector: 'app-dslr-list',
  templateUrl: './dslr-list.component.html',
  styleUrls: ['./dslr-list.component.css']
})
export class DslrListComponent {
pageable: any = { page: 0, size: 10, sort: [], length: 0 }
  data: any;

  onDelete($event: any[]) {
    this.commonApi.delete(DslrClass, $event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    if ($event) {
      this.pageable.page = $event.pageIndex || this.pageable.page
      this.pageable.size = $event.pageSize || this.pageable.size
    }
    this.commonApi.filter(DslrClass, this.pageable, undefined).subscribe((res: any) => {
      this.data = res.content
      this.total = res.page.totalElements
    })
  }

  constructor(
    private commonApi: CommonApiService,
  ) { }
}
