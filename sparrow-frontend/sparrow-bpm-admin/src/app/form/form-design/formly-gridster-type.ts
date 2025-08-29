import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { FieldType, FormlyFieldConfig } from '@ngx-formly/core';
import { CompactType, DisplayGrid, Draggable, GridsterConfig, GridsterItem, GridType, PushDirections, Resizable } from 'angular-gridster2';
import { BehaviorSubject } from 'rxjs';
interface Safe extends GridsterConfig {
  draggable: Draggable;
  resizable: Resizable;
  pushDirections: PushDirections;
}

export const ON_ITEM_ADD = new BehaviorSubject<any>(undefined);

interface CombineItem {
  gridsterItem: GridsterItem,
  field: FormlyFieldConfig,
}

@Component({
  selector: 'formly-gridster-type',
  template: `

<div style="height: 400px;">
     <div
    *ngIf="myOptions.enableEmptyCellDrop"
    draggable="true"
    (dragstart)="dragStartHandler($event)"
  >
    Drag me!
  </div>
      <gridster [options]="myOptions" style="height: 400px;">
      <gridster-item *ngFor="let item1 of field.fieldGroup" [item]="item1.props!['gristerItem']!">
        <div style="padding: 10px; display:flex; flex-directioin: row">
          <button
            mat-icon-button color="accent"
          >
            <mat-icon>delete</mat-icon>
          </button>
          <formly-field [field]="item1"></formly-field>
        </div>
      </gridster-item>
    </gridster>
</div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GridsterTypeComponent extends FieldType implements OnInit {

  bb = { x: 0, y: 0, cols: 1, rows: 1 }
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
      fixedColWidth: 105,
      fixedRowHeight: 105,
      keepFixedHeightInMobile: false,
      keepFixedWidthInMobile: false,
      scrollSensitivity: 10,
      scrollSpeed: 20,
      enableEmptyCellClick: false,
      enableEmptyCellContextMenu: false,
      enableEmptyCellDrop: true,
      enableEmptyCellDrag: false,
      enableOccupiedCellDrop: false,
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
      displayGrid: DisplayGrid.OnDragAndResize,
      disableWindowResize: false,
      disableWarnings: false,
      scrollToNewItems: false
    };
    this.myOptions = { ...this.myOptions, ...myOptions }
  }


  // get gridItems() {
  //   // console.log(this.field.fieldGroup)
  //   return this.field.fieldGroup?.map((m: any) =>{
  //     const c = {field: m, gridsterItem: m.props['gristerItem'] }
  //     console.log(c)
  //     return c
  //   }) as CombineItem[]
  // }

  dashboard: any = [
    { cols: 2, rows: 1, y: 0, x: 0 },
    { cols: 2, rows: 2, y: 0, x: 2 },
    { cols: 1, rows: 1, y: 0, x: 4 },
    { cols: 3, rows: 2, y: 1, x: 4 },
    { cols: 1, rows: 1, y: 4, x: 5 },
    { cols: 1, rows: 1, y: 2, x: 1 },
    { cols: 2, rows: 2, y: 5, x: 5 },
    { cols: 2, rows: 2, y: 3, x: 2 },
    { cols: 2, rows: 1, y: 2, x: 2 },
    { cols: 1, rows: 1, y: 3, x: 4 },
    { cols: 1, rows: 1, y: 0, x: 6 }
  ];

  myOptions: GridsterConfig | any = {
    gridType: GridType.Fixed,
    displayGrid: DisplayGrid.Always,
    enableEmptyCellClick: false,
    enableEmptyCellContextMenu: false,
    enableEmptyCellDrop: true,
    enableEmptyCellDrag: true,
    enableOccupiedCellDrop: false,
    emptyCellClickCallback: this.emptyCellClick.bind(this),
    emptyCellContextMenuCallback: this.emptyCellClick.bind(this),
    emptyCellDropCallback: this.emptyCellClick.bind(this),
    emptyCellDragCallback: this.emptyCellClick.bind(this),
    emptyCellDragMaxCols: 50,
    emptyCellDragMaxRows: 50,
    fixedColWidth: 105,
    fixedRowHeight: 105,
  };



  changedOptions(): void {
    if (this.myOptions.api && this.myOptions.api.optionsChanged) {
      this.myOptions.api.optionsChanged();
    }
  }

  removeItem($event: MouseEvent | TouchEvent, item: any): void {
    $event.preventDefault();
    $event.stopPropagation();
    this.dashboard.splice(this.dashboard.indexOf(item), 1);
  }

  addItem(): void {
    const key = 'input_' + Date.now();
    const newField: any = {
      key,
      type: 'input',
      props: {
        label: 'Email address1234',
        placeholder: 'Enter email',
        required: true,
        gristerItem: { x: 0, y: 0, cols: 3, rows: 1 }
      },

    };

    this.field.fieldGroup = [
      ...(this.field.fieldGroup || []),
      newField
    ];
  }

  emptyCellClick(event: any, item: GridsterItem): void {
    console.info('empty cell click', event, item);
    console.info('empty cell click', event, item);
    // 取出数据
    const text = event.dataTransfer?.getData('text/plain');
    const type = event.dataTransfer?.getData('type');

    console.log('text:', text);
    console.log('type:', type);
    ON_ITEM_ADD.next({ type: type, item: item })
  }

  dragStartHandler(ev: DragEvent): void {
    if (ev.dataTransfer) {
      ev.dataTransfer.setData('text/plain', 'Drag Me Button');
      ev.dataTransfer.dropEffect = 'copy';
    }
  }

}
