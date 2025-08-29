import { Component, OnInit } from '@angular/core';
import { RuleService } from '../services/rule.service';
import { LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { HttpClient } from '@angular/common/http';
import { CommonApiService } from '@sparrowmini/common-api';

@Component({
  selector: 'app-drl-list',
  templateUrl: './drl-list.component.html',
  styleUrls: ['./drl-list.component.css']
})
export class DrlListComponent implements OnInit {

  pageable: any = { page: 0, size: 10, sort: [], length: 0 }

  onDelete($event: any[]) {
    this.ruleService.deleteDrl($event.map(m => m.id)).subscribe(() => {
      this.onPage(this.pageable)
    });
  }
  total = 0
  onPage($event: PageEvent) {
    console.log($event)
    if ($event) {
      this.pageable.page = $event.pageIndex
      this.pageable.size = $event.pageSize
    }
    // this.commonService.filter("cn.sparrowmini.rules.model.Drl",this.pageable, undefined)
    this.ruleService.listDrl(this.pageable, undefined)
    .subscribe((res: any) => {
      this.rules = res.content
      this.total = res.page.totalElements
    })
  }
  rules: any[] = [];

  constructor(
    private ruleService: RuleService,
    private http: HttpClient,
    private commonService:CommonApiService,
  ) { }

  ngOnInit(): void {
    // this.http.get(`http://localhost:8999/access/check`).subscribe()
  }

}
