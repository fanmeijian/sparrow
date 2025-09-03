import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { CommonApiService } from '@sparrowmini/common-api';
import { DrlTemplateClass } from '../drl-template-list/drl-template-list.component';
import { ActivatedRoute } from '@angular/router';
export const DrlTemplateRuleClass = 'cn.sparrowmini.rules.model.DrlTemplateRule'

@Component({
  selector: 'app-drl-template-rule-list',
  templateUrl: './drl-template-rule-list.component.html',
  styleUrls: ['./drl-template-rule-list.component.css']
})
export class DrlTemplateRuleListComponent implements OnChanges{
  @Input() drlTemplateId!: any
  pageable: any = { page: 0, size: 10, sort: [], length: 0 }
  data: any;
  // drlTemplateId?: any = ''
  onDelete($event: any[]) {
    this.commonApi.delete(DrlTemplateRuleClass, $event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    if ($event) {
      this.pageable.page = $event.pageIndex || this.pageable.page
      this.pageable.size = $event.pageSize || this.pageable.size
    }
    this.route.queryParamMap.subscribe(params => {
      const drlTemplateId = params.get('drlTemplateId')||this.drlTemplateId
      // this.drlTemplateId = drlTemplateId
      const filter = `drlTemplateId='${drlTemplateId}'`
      this.commonApi.filter(DrlTemplateRuleClass, this.pageable, filter).subscribe((res: any) => {
        this.data = res.content
        this.total = res.page.totalElements
      })
    })

  }

  constructor(
    private commonApi: CommonApiService,
    private route: ActivatedRoute,
  ) { }
  ngOnChanges(changes: SimpleChanges): void {
    this.onPage({
      pageIndex: 0,
      pageSize: 10,
      length: 0
    })
  }
}
