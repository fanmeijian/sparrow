import { SelectionModel } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { FlatNode, TreeNode } from '../../../model/TreeConstant';
import { BehaviorSubject, Subject } from 'rxjs';

export interface TreeViewOption {
  checklistSelection: SelectionModel<FlatNode>;
  treeData: any;
  checkedIds?: string[];
  disabled?: boolean;
  parentSelectable: boolean;
}

@Component({
  selector: 'app-tree-view',
  templateUrl: './tree-view.component.html',
  styleUrls: ['./tree-view.component.scss'],
})
export class TreeViewComponent implements OnInit, OnChanges {

  @Input() treeViewOption!: TreeViewOption

  onNodeClick(_t22: any) {
    this.treeViewOption.checklistSelection.select(_t22);
  }
  remove(_t5: FlatNode) {
    this.treeViewOption.checklistSelection.deselect(_t5);
  }

  dataSource: MatTreeFlatDataSource<TreeNode, FlatNode>;


  constructor(
  ) {
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

  }
  ngOnChanges(changes: SimpleChanges): void {
    if (this.treeViewOption.treeData) {
      this.dataSource.data = this.treeViewOption.treeData;
    }

    if (this.treeViewOption.checkedIds && this.treeControl.dataNodes) {
      this.treeViewOption.checklistSelection.clear()
      this.treeViewOption.checkedIds.forEach((id: any) => {
        const node = this.treeControl.dataNodes.find(m => m.id === id)
        this.treeViewOption.checklistSelection.select(node)
      })
    }
  }
  ngOnInit(): void {

  }

  private _transformer = (node: TreeNode, level: number) => {
    return {
      expandable: node.children!.length > 0,
      name: node.name,
      id: node.id,
      childCount: node.children!.length,
      level: level,
      index: node.index,
      parentId: node.parentId
    };
  };

  treeControl = new FlatTreeControl<any>(
    node => node.level,
    node => node.expandable,
  );

  treeFlattener = new MatTreeFlattener(
    this._transformer,
    node => node.level,
    node => node.expandable,
    node => node.children,
  );
  hasChild = (_: number, node: FlatNode) => node.expandable;
  getLevel = (node: FlatNode) => node.level;

}
