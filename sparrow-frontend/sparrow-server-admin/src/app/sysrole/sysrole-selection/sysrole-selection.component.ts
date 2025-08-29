import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatLegacyChipInputEvent as MatChipInputEvent } from '@angular/material/legacy-chips';
import { Observable, startWith, map } from 'rxjs';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { MatLegacyAutocompleteSelectedEvent as MatAutocompleteSelectedEvent } from '@angular/material/legacy-autocomplete';
import { SysroleService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-sysrole-selection',
  templateUrl: './sysrole-selection.component.html',
  styleUrls: ['./sysrole-selection.component.scss'],
})
export class SysroleSelectionComponent implements OnInit {
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

  @ViewChild('fruitInput') fruitInput!: ElementRef<HTMLInputElement>;

  constructor(private sysroleService: SysroleService) {}
  ngOnInit(): void {
    this.onPageChange(this.pageable);
  }

  remove(fruit: any): void {
    const index = this.selectedSysroles.indexOf(fruit);

    if (index >= 0) {
      this.selectedSysroles.splice(index, 1);
    }
  }

  select(seletedItem: any) {
    if (!this.multiple) {
      this.selectedSysroles.length = 0;
    }

    if (this.selectedSysroles.indexOf(seletedItem) === -1) {
      this.selectedSysroles.push(seletedItem);
      this.onSelected.emit(seletedItem.id);
    }
  }

  onPageChange(event: any) {
    // console.log(event);
    this.pageable.pageIndex = event.pageIndex;
    this.pageable.pageSize = event.pageSize;
    this.sysroleService
      .sysroles([], this.pageable.pageIndex, this.pageable.pageSize)
      .subscribe((res) => {
        this.sysroles = res.content!;
        this.pageable.length = res.totalElements!;
      });
  }
}
