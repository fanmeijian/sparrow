import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTreeFlattener, MatTreeFlatDataSource } from '@angular/material/tree';
import { TreeNode, FlatTreeNode } from '../dynamic-tree-view/dynamic-tree-constant';


@Component({
  selector: 'app-dynamic-menu',
  templateUrl: './dynamic-menu.component.html',
  styleUrls: ['./dynamic-menu.component.scss']
})
export class DynamicMenuComponent implements OnInit, OnChanges {

  menuEntered = false;

  onMouseLeave($event: Event) {
    console.log('onMouseLeave', $event);
  }

  @Input() menu: TreeNode | any;

  isOpen = false;
  ngOnInit(): void {
  }
  private _transformer = (node: TreeNode, level: number) => {
    return {
      expandable: node.childCount > 0,
      name: node.name,
      id: node.id,
      childCount: node.childCount,
      level: level,
      index: node.index,
      parentId: node.parentId
    };
  };

  treeControl = new FlatTreeControl<FlatTreeNode>(
    node => node.level,
    node => node.expandable,
  );

  treeFlattener = new MatTreeFlattener(
    this._transformer,
    node => node.level,
    node => node.expandable,
    node => node.children,
  );

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

  constructor() {
  }
  ngOnChanges(changes: SimpleChanges): void {
    this.dataSource.data = this.menu?.children ? this.menu.children : [];

  }

  hasChild = (_: number, node: FlatTreeNode) => node.expandable;
}
