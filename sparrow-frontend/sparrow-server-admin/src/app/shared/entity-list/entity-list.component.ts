import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { LegacyPageEvent as PageEvent } from '@angular/material/legacy-paginator';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';

@Component({
  selector: 'app-entity-list',
  templateUrl: './entity-list.component.html',
  styleUrls: ['./entity-list.component.css']
})
export class EntityListComponent implements OnInit, OnChanges {

  @Input() entityList: any[] = []
  @Input() pageable: any

  @Output() onPage: EventEmitter<PageEvent> = new EventEmitter()
  @Output() onDelete: EventEmitter<any[]> = new EventEmitter()

  columns: any;
  displayedColumns: any;

  pagealbe: any;
  delete(element: any) {
    this.onDelete.next(element);
  }

  onPageChange($event: PageEvent) {
    this.onPage.next($event)
  }

  dataSource: any;
  new() {
    throw new Error('Method not implemented.');
  }

  constructor(

  ) { }
  ngOnChanges(changes: SimpleChanges): void {
    const list: any[] = changes['entityList'].currentValue
    console.log(list)
    if (list && list.length > 0) {
      this.dataSource = new MatTableDataSource(list)
      this.columns = Object.keys(list[0]).map(m => { return { name: m, field: m } })
      this.displayedColumns = Object.keys(list[0])
    }


  }

  ngOnInit(): void {


  }

}
