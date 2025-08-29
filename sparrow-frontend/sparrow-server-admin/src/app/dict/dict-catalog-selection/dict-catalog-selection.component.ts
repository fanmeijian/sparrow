import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { DictService, SysroleService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-dict-catalog-selection',
  templateUrl: './dict-catalog-selection.component.html',
  styleUrls: ['./dict-catalog-selection.component.scss']
})
export class DictCatalogSelectionComponent implements OnInit {
  @Input() public selectedSysroles!: any[];
  @Input() public multiple: boolean = false;
  @Output() private onSelected = new EventEmitter<string>();
  @Output() private onRemoved = new EventEmitter<string>();

  sysroles: any[] = [];
  // selectedSysroles: any[] = []

  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  @ViewChild("fruitInput") fruitInput!: ElementRef<HTMLInputElement>;

  constructor(private sysroleService: DictService) {}
  ngOnInit(): void {
    this.onPageChange(this.pageable)
  }

  remove(fruit: any): void {
    const index = this.selectedSysroles.indexOf(fruit);

    if (index >= 0) {
      this.selectedSysroles.splice(index, 1);
    }
  }

  select(seletedItem: any) {
    // if (!this.multiple) {
    //   this.selectedSysroles = [];
    // }

    if (this.selectedSysroles.indexOf(seletedItem) === -1) {
      this.selectedSysroles.push(seletedItem);
      this.onSelected.emit(seletedItem.id);
    }
  }

  onPageChange(event: any) {
    // console.log(event);
    this.pageable.pageIndex = event.pageIndex;
    this.pageable.pageSize = event.pageSize;
    this.sysroleService.catalogs(this.pageable.pageIndex, this.pageable.pageSize).subscribe((res) => {
      this.sysroles = res.content!;
      this.pageable.length = res.totalElements!
    });
  }

}
