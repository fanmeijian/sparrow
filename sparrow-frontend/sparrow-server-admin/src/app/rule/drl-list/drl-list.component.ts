import { Component, OnInit } from '@angular/core';
import { RuleService } from '../services/rule.service';
import { LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';

@Component({
  selector: 'app-drl-list',
  templateUrl: './drl-list.component.html',
  styleUrls: ['./drl-list.component.css']
})
export class DrlListComponent implements OnInit {
  onPage($event: PageEvent) {
    console.log($event);
  }
  rules: any[];
  pageable = { page: 0, size: 10, length: 0, sort: [] }
  constructor(
    private ruleService: RuleService,
  ) { }

  ngOnInit(): void {
    this.ruleService.listDrl({}, undefined).subscribe((res: any) => {
      this.rules = res.content
      this.pageable = Object.assign(this.pageable, res.page)
    })
  }

}
