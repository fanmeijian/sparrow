import { SelectionModel } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, Injectable, OnInit } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import {
  MatTreeFlattener,
  MatTreeFlatDataSource,
} from '@angular/material/tree';
import { DictService, OrganizationService } from '@sparrowmini/org-api';
import { BehaviorSubject, switchMap, zip, of } from 'rxjs';
import { DictCatalogCreateComponent } from '../dict-catalog-create/dict-catalog-create.component';
import { DictCatalog } from '@sparrowmini/org-api/model/dictCatalog';
import {
  DynamicDataSource,
  DynamicDatabase,
  DynamicFlatNode,
} from './dict-tree-datasource';
import { DictCreateComponent } from '../dict-create/dict-create.component';
import { TreeNode } from '../../common/dynamic-tree-view/dynamic-tree-constant';
import { DictTreeService } from './dict-tree.service';

@Component({
  selector: 'app-dicts',
  templateUrl: './dicts.component.html',
  styleUrls: ['./dicts.component.scss', '../../org.css'],
})
export class DictsComponent implements OnInit {
  selectedMenu: any;
  catalogs?: DictCatalog[];
  constructor(
    private dialog: MatDialog,
    private snack: MatSnackBar,
    private dictService: DictService,
    private database: DynamicDatabase,
    public dictTreeService: DictTreeService,
  ) {
    this.treeControl = new FlatTreeControl<DynamicFlatNode>(
      this.getLevel,
      this.isExpandable
    );
    this.dataSource = new DynamicDataSource(this.treeControl, database);


  }
  ngOnInit(): void {
    this.database.initialData().subscribe(res => {
      // console.log(res)
      this.dataSource.data = res
    });
  }

  newCatalog() {
    this.dialog.open(DictCatalogCreateComponent, { width: '80%' }).afterClosed().subscribe(res => {
      if (res) {
        this.ngOnInit()
      }
    });
  }

  newDict() {
    this.dialog.open(DictCreateComponent, { width: '80%' }).afterClosed().subscribe(res => {
      if (res) {
        this.ngOnInit()
      }
    });
  }

  edit(selectedMenu: any) {

  }
  deleteMenu(selectedMenu: any) {
    console.log(selectedMenu)
  }

  // tree config
  treeControl!: FlatTreeControl<DynamicFlatNode>;

  dataSource!: DynamicDataSource;

  getLevel = (node: DynamicFlatNode) => node.level;

  isExpandable = (node: DynamicFlatNode) => node.childCount > 0;

  hasChild = (_: number, _nodeData: DynamicFlatNode) => _nodeData.me.childCount > 0;


  onNodeClick($event: TreeNode) {
  }
}
