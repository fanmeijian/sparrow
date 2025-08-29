import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { DictService } from '@sparrowmini/org-api';
import { DynamicDatabase, DynamicFlatNode, DynamicDataSource } from '../dicts/dict-tree-datasource';
import { DictCatalog } from '@sparrowmini/org-api/model/dictCatalog';
import { MatLegacyChipInputEvent as MatChipInputEvent } from '@angular/material/legacy-chips';
import { Dict } from '@sparrowmini/org-api/model/dict';
import { MatLegacySelectChange as MatSelectChange } from '@angular/material/legacy-select';

@Component({
  selector: 'app-dict-selection',
  templateUrl: './dict-selection.component.html',
  styleUrls: ['./dict-selection.component.scss']
})
export class DictSelectionComponent implements OnInit {

  @Input() public selectedItems: any[] = [];
  @Input() public multiple: boolean = false;
  @Input() public catalogId?: string;
  @Input() public showType: 'TREE'|'SELECT'|'RADIO' = 'TREE';
  @Output() private onSelected = new EventEmitter<string>();
  @Output() private onRemoved = new EventEmitter<string>();

  dicts: Dict[] = []
  catalogs?: DictCatalog[];
  selectedMenu: any;
  constructor(
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private dictService: DictService,
    database: DynamicDatabase
  ) {
    this.treeControl = new FlatTreeControl<DynamicFlatNode>(
      this.getLevel,
      this.isExpandable
    );
    this.dataSource = new DynamicDataSource(this.treeControl, database);

    database.initialData().subscribe(res=>{
      console.log(res)
      this.dataSource.data=res
    });
  }
  ngOnInit(): void {
    this.dictService.catalogs().subscribe((res) => {
      this.catalogs = res.content;
    });

    if(this.showType==='SELECT'){
      this.dictService.dictByCatalog(this.catalogId!).subscribe(res=>{
        this.dicts=res.content!
      })
    }
  }

  // tree config
  treeControl!: FlatTreeControl<DynamicFlatNode>;

  dataSource!: DynamicDataSource;

  getLevel = (node: DynamicFlatNode) => node.level;

  isExpandable = (node: DynamicFlatNode) => node.expandable;

  hasChild = (_: number, _nodeData: DynamicFlatNode) => _nodeData.expandable;

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our fruit
    if (value) {
      this.selectedItems.push({ name: value });
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(fruit: any): void {
    const index = this.selectedItems.indexOf(fruit);

    if (index >= 0) {
      this.selectedItems.splice(index, 1);
    }
  }

  select(menu: any) {

    if(this.multiple===false){
      this.selectedItems.length=0
    }

    if (this.selectedItems.indexOf(menu) === -1) {
      this.selectedItems.push(menu);
      this.onSelected.emit(menu.id);
    }
  }


  onSelectionChange(event: MatSelectChange){
    // console.log(event.value)
    // this.selectedItems.push()
    this.selectedItems.length=0
    this.selectedItems.push(event.value)
  }

}
