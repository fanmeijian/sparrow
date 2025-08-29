import {
  DataSource,
  CollectionViewer,
  SelectionChange,
} from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, merge, map, tap } from 'rxjs';
import { TreeService } from './dynamic-tree-constant';

/** Flat node with expandable and level information */
export class DynamicFlatNode {
  constructor(
    public name: string,
    public id: string,
    public type: 'CATALOG' | 'DICT',
    public me: any,
    public level: number,
    public expandable: boolean,
    public isLoading: boolean,
    public childCount: number,
    public parentId: string,
  ) { }
}


export class DynamicDataSource implements DataSource<DynamicFlatNode> {
  dataChange = new BehaviorSubject<DynamicFlatNode[]>([]);

  get data(): DynamicFlatNode[] {
    return this.dataChange.value;
  }
  set data(value: DynamicFlatNode[]) {
    this._treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(
    private _treeControl: FlatTreeControl<DynamicFlatNode>,
    private treeService: TreeService,
  ) {
  }

  connect(collectionViewer: CollectionViewer): Observable<DynamicFlatNode[]> {
    this._treeControl.expansionModel.changed.subscribe((change) => {
      if (
        (change as SelectionChange<DynamicFlatNode>).added ||
        (change as SelectionChange<DynamicFlatNode>).removed
      ) {
        this.handleTreeControl(change as SelectionChange<DynamicFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(
      map(() => this.data)
    );
  }

  disconnect(collectionViewer: CollectionViewer): void { }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<DynamicFlatNode>) {
    if (change.added) {
      change.added.forEach((node) => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed
        .slice()
        .reverse()
        .forEach((node) => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: DynamicFlatNode, expand: boolean) {
    // const children = this._database.getChildren(node);
    const children = this.treeService.getChildren(node.id)
    const index = this.data.indexOf(node);
    if (!children || index < 0) {
      // If no children, or cannot find the node, no op
      return;
    }

    node.isLoading = true;
    if (expand) {
      // const nodes = children.map(
      //   name => new DynamicFlatNode(name, name , node.level + 1, this._database.isExpandable(name)),
      // );
      // this.data.splice(index + 1, 0, ...nodes);

      children.subscribe((res: any) => {
        const nodes = res.content.map((value: any, index: number) => Object.assign(value, { index: index, level: node.level+1, parentId: node.id }));
        this.data.splice(index + 1, 0, ...nodes);
        // notify the change
        this.dataChange.next(this.data);
        node.isLoading = false;
      });
    } else {
      let count = 0;
      for (
        let i = index + 1;
        i < this.data.length && this.data[i].level > node.level;
        i++, count++
      ) { }
      this.data.splice(index + 1, count);
      // notify the change
      this.dataChange.next(this.data);
      node.isLoading = false;
    }
  }
}
