import { AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChildren, ViewEncapsulation } from '@angular/core';
import { FieldType, FormlyFieldConfig } from '@ngx-formly/core';
import { GridsterConfig, GridType, CompactType, DisplayGrid, GridsterItem } from 'angular-gridster2';
import { BehaviorSubject } from 'rxjs';

export const ON_ITEM_ADD = new BehaviorSubject<any>(undefined);

interface CombineItem {
  gridsterItem: GridsterItem,
  field: FormlyFieldConfig,
}

@Component({
  selector: 'app-grister-field-type',
  templateUrl: './grister-field-type.component.html',
  styleUrls: ['./grister-field-type.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class GristerFieldTypeComponent extends FieldType implements OnInit, AfterViewInit {

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.gridsterItems.forEach((itemRef, index) => {
        const rect = itemRef.nativeElement.getBoundingClientRect();

        const cols = Math.ceil(rect.width / this.myOptions.fixedColWidth);
        const rows = Math.ceil(rect.height / this.myOptions.fixedRowHeight);

        this.layout[index].cols = cols;
        this.layout[index].rows = rows;
      });

      this.myOptions.api?.optionsChanged?.();
    }, 0);
  }
  @ViewChildren('gridsterItems', { read: ElementRef }) gridsterItems!: QueryList<ElementRef>;

  layout: Array<GridsterItem & { id: string }> = [
    { id: 'input1', x: 0, y: 0, cols: 2, rows: 1 },
    { id: 'input2', x: 2, y: 0, cols: 3, rows: 1 },
    { id: 'button1', x: 0, y: 1, cols: 1, rows: 1 },
  ];
  myOptions: GridsterConfig | any = {

  };

  ngOnInit(): void {
    const myOptions = {
      gridType: GridType.Fixed,
      compactType: CompactType.None,
      margin: 10,
      outerMargin: true,
      outerMarginTop: null,
      outerMarginRight: null,
      outerMarginBottom: null,
      outerMarginLeft: null,
      useTransformPositioning: true,
      mobileBreakpoint: 640,
      useBodyForBreakpoint: false,
      minCols: 1,
      maxCols: 100,
      minRows: 1,
      maxRows: 100,
      maxItemCols: 100,
      minItemCols: 1,
      maxItemRows: 100,
      minItemRows: 1,
      maxItemArea: 2500,
      minItemArea: 1,
      defaultItemCols: 1,
      defaultItemRows: 1,
      fixedColWidth: 20,
      fixedRowHeight: 30,
      keepFixedHeightInMobile: false,
      keepFixedWidthInMobile: false,
      scrollSensitivity: 10,
      scrollSpeed: 20,

      emptyCellDragMaxCols: 50,
      emptyCellDragMaxRows: 50,
      ignoreMarginInRow: false,
      draggable: {
        enabled: true
      },
      resizable: {
        enabled: true
      },
      swap: false,
      pushItems: true,
      disablePushOnDrag: false,
      disablePushOnResize: false,
      pushDirections: { north: true, east: true, south: true, west: true },
      pushResizeItems: false,
      displayGrid: DisplayGrid.Always,
      disableWindowResize: false,
      disableWarnings: false,
      scrollToNewItems: false,
      enableEmptyCellClick: false,
      enableEmptyCellContextMenu: false,
      enableEmptyCellDrop: true,
      enableEmptyCellDrag: true,
      enableOccupiedCellDrop: false,
      emptyCellClickCallback: this.emptyCellClick.bind(this),
      emptyCellContextMenuCallback: this.emptyCellClick.bind(this),
      emptyCellDropCallback: this.emptyCellClick.bind(this),
      emptyCellDragCallback: this.emptyCellClick.bind(this),
    };
    this.myOptions = { ...this.myOptions, ...myOptions }
  }

  emptyCellClick(event: any, item: GridsterItem): void {
    console.info('empty cell click', event, item);
    console.info('empty cell click', event, item);
    // 取出数据
    const text = event.dataTransfer?.getData('text/plain');
    const type = event.dataTransfer?.getData('type');

    console.log('text:', text);
    console.log('type:', type);
    if (event instanceof DragEvent) {
      ON_ITEM_ADD.next({ type: type, item: item })
    }

  }
}
