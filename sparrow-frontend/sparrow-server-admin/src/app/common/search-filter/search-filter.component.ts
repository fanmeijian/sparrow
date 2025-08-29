import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { FilterTreeBean, SparrowJpaFilter } from '@sparrowmini/org-api';
import { BehaviorSubject } from 'rxjs';

// export interface SparrowJpaFilter {
//   filterTreeBean?: FilterTreeBean;
//   children?: SparrowJpaFilter[];
// }

// export interface FilterTreeBean {
//   type?: string;
//   name?: string;
//   op?: string;
//   value?: any;
// }

@Component({
  selector: 'app-search-filter',
  templateUrl: './search-filter.component.html',
  styleUrls: ['./search-filter.component.scss'],
})
export class SearchFilterComponent implements OnInit {
  searchStr: string;

  resetFilter() {
    this.searchStr = '';
    this.applyFilter1({ keyCode: 13 })
  }

  applyFilter1($event: any) {
    if ($event.keyCode === 13) {
      this.applyFilter.emit(this.searchStr)
    }
  }

  @Output() applyFilter = new EventEmitter<string>();

  constructor(private snack: MatSnackBar) {

  }

  ngOnInit(): void {
  }

}
