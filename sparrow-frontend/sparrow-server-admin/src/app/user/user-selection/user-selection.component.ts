import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { UserService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-user-selection',
  templateUrl: './user-selection.component.html',
  styleUrls: ['./user-selection.component.scss'],
})
export class UserSelectionComponent implements OnInit {
  @Input() public selected!: any[];
  @Input() public multiple: boolean = false;
  @Output() private onSelected = new EventEmitter<string>();
  @Output() private onRemoved = new EventEmitter<string>();

  datas: any[] = [];
  // selectedSysroles: any[] = []
  pageable = {
    pageIndex: 0,
    pageSize: 10,
    length: 0,
    sort: ['createdDate,desc'],
  };

  @ViewChild('fruitInput') fruitInput!: ElementRef<HTMLInputElement>;
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>();
  filters: any[] = [];

  constructor(private userService: UserService) {}
  ngOnInit(): void {
    this.userService
      .users(this.filters, this.pageable.pageIndex, this.pageable.pageSize)
      .subscribe((res) => {
        this.datas = res.content!;
        this.pageable.length = res.totalElements!;
      });
  }

  remove(fruit: any): void {
    const index = this.selected.indexOf(fruit);

    if (index >= 0) {
      this.selected.splice(index, 1);
    }
  }

  select(seletedItem: any) {
    if (!this.multiple) {
      this.selected.length = 0;
    }

    if (this.selected.indexOf(seletedItem) === -1) {
      this.selected.push(seletedItem);
      this.onSelected.emit(seletedItem.id);
    }
  }
  onPageChange(event: any) {
    // console.log(event);
    // this.dataSource = new MatTableDataSource<any>();
    this.pageable.pageIndex = event.pageIndex;
    this.pageable.pageSize = event.pageSize;
    this.userService
      .users(this.filters, this.pageable.pageIndex, this.pageable.pageSize)
      .subscribe((res) => {
        // this.dataSource = new MatTableDataSource<any>(res.content);
        this.datas = res.content!;
        this.pageable.length = res.totalElements!;
      });
  }
}
