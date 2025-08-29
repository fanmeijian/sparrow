import {
  DataSource,
  CollectionViewer,
  SelectionChange,
} from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { Injectable } from '@angular/core';
import { DictService } from '@sparrowmini/org-api';
import { Dict } from '@sparrowmini/org-api/model/dict';
import { DictCatalog } from '@sparrowmini/org-api/model/dictCatalog';
import { BehaviorSubject, Observable, merge, map } from 'rxjs';

/** Flat node with expandable and level information */
export class DynamicFlatNode {
  constructor(
    public item: string,
    public id: string,
    public type: 'CATALOG' | 'DICT',
    public me: DictCatalog| Dict | any,
    public level = 1,
    public expandable = false,
    public isLoading = false,
    public childCount= 0
  ) {}
}

/**
 * Database for dynamic data. When expanding a node in the tree, the data source will need to fetch
 * the descendants data from the database.
 */
@Injectable({ providedIn: 'root' })
export class DynamicDatabase {
  constructor(private dictService: DictService) {}
  initialData(): Observable<DynamicFlatNode[]> {
    return this.dictService.catalogs().pipe(
      map((m) => m.content),
      map((m: any) =>
        m?.map(
          (c: any) => new DynamicFlatNode(c.name!, c.id, 'CATALOG', c, 0,true, true,m.childCount)
        )
      )
    );
  }

  getChildren(node: DynamicFlatNode): Observable<Dict[]> {
    if(node.type==='CATALOG'){
      return this.dictService.dictByCatalog(node.id).pipe(map((m) => m.content!));
    }else{
      return this.dictService.dictByParentId(node.id).pipe(map((m) => m.content!));
    }
  }

  isExpandable(node: string): boolean {
    return true;
  }
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
    private _database: DynamicDatabase
  ) {}

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

  disconnect(collectionViewer: CollectionViewer): void {}

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
    const children = this._database.getChildren(node);
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

      children.subscribe((res:any) => {
        const nodes = res.map(
          (name:any) =>
            new DynamicFlatNode(
              name.name!,
              name.id!,
              'DICT',
              name,
              node.level + 1,
              this._database.isExpandable(name.id!),
              false,
              name.childCount
            )
        );
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
      ) {}
      this.data.splice(index + 1, count);
      // notify the change
      this.dataChange.next(this.data);
      node.isLoading = false;
    }
  }
}
