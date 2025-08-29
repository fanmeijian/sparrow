import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, OnInit } from '@angular/core';
import {
  MatTreeFlattener,
  MatTreeFlatDataSource,
} from '@angular/material/tree';
import { MenuService } from '@sparrowmini/org-api';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { SelectionModel } from '@angular/cdk/collections';
import { HttpClient } from '@angular/common/http';
import { SysconfigService } from '@sparrowmini/org-api';
import { DynamicDataSource } from 'src/app/service/dynamic-tree-datasource';
import { MenuTreeService } from './menu-tree.service';
import { KeycloakService } from 'keycloak-angular';

/** Flat node with expandable and level information */
interface ExampleFlatNode {
  id: string;
  expandable: boolean;
  name: string;
  level: number;
  children: ExampleFlatNode[];
}

@Component({
  selector: 'app-menu-tree',
  templateUrl: './menu-tree.component.html',
  styleUrls: ['./menu-tree.component.scss'],
})
export class MenuTreeComponent implements OnInit {
  logout() {
    this.keycloak.logout();
  }
  name
  isInit: boolean = true;
  userInfo: any = JSON.parse(localStorage.getItem('userInfo'));
  selectedNode = '';

  dragging = false;
  expandTimeout: any;
  expandDelay = 1000;
  validateDrop = false;
  expansionModel = new SelectionModel<string>(true);

  private _transformer = (node: any, level: number) => {
    return {
      expandable: node.childCount > 0,
      childCount: node.childCount,
      name: node.name,
      url: node.url,
      level: level,
      icon: node.icon
    };
  };

  treeControl = new FlatTreeControl<any>(
    (node) => node.level,
    (node) => node.expandable
  );

  treeFlattener = new MatTreeFlattener(
    this._transformer,
    (node) => node.level,
    (node) => node.expandable,
    (node) => node.childre,
  );

  dataSource!: DynamicDataSource;
  parentId: any;

  constructor(
    private menuService: MenuService,
    private http: HttpClient,
    private sysconfigService: SysconfigService,
    private treeService: MenuTreeService,
    private keycloak: KeycloakService,
  ) {
    this.dataSource = new DynamicDataSource(this.treeControl, treeService);
    this.name = sessionStorage.getItem('name') || sessionStorage.getItem('username')
  }
  ngOnInit(): void {


    this.sysconfigService.getInitConfigs().subscribe((res: any) => {
      if (res.totalElements <= 0) {
        this.isInit = false;
      } else {
        this.treeService.getChildren(null).subscribe((res: any) => {
          let nestedNodes: any = res.content.map((value: any, index: number) => Object.assign(value, { index: index, parentId: this.parentId }))
          this.dataSource.data = nestedNodes
          console.log(nestedNodes)
        });
      }
    });
  }

  init() {
    this.sysconfigService.initSystem({}).subscribe(() => {
      this.ngOnInit()
      this.isInit = true
    });
  }

  hasChild = (_: number, node: ExampleFlatNode) => node.expandable;

  // visibleNodes(): ExampleFlatNode[] {
  //   const result: ExampleFlatNode[] = [];

  //   function addExpandedChildren(node: ExampleFlatNode, expanded: string[]) {
  //     result.push(node);
  //     if (expanded.includes(node.id)) {
  //       node.children.map((child) => addExpandedChildren(child, expanded));
  //     }
  //   }
  //   this.dataSource.data.forEach((node) => {
  //     addExpandedChildren(node, this.expansionModel.selected);
  //   });
  //   return result;
  // }

  /**
   * Handle the drop - here we rearrange the data based on the drop event,
   * then rebuild the tree.
   * */
  drop(event: CdkDragDrop<string[]>) {
    console.log('origin/destination', event.previousIndex, event.currentIndex, event);

    // ignore drops outside of the tree
    if (!event.isPointerOverContainer) return;

    // construct a list of visible nodes, this will match the DOM.
    // the cdkDragDrop event.currentIndex jives with visible nodes.
    // it calls rememberExpandedTreeNodes to persist expand state
    // const visibleNodes = this.visibleNodes();

    // deep clone the data source so we can mutate it
    const changedData = JSON.parse(JSON.stringify(this.dataSource.data));

    // recursive find function to find siblings of node
    function findNodeSiblings(
      arr: Array<any>,
      id: string
    ): Array<any> | undefined {
      let result, subResult;
      arr.forEach((item, i) => {
        if (item.id === id) {
          result = arr;
        } else if (item.children) {
          subResult = findNodeSiblings(item.children, id);
          if (subResult) result = subResult;
        }
      });
      return result;
    }

    // determine where to insert the node
    const nodeAtDest = null//visibleNodes[event.currentIndex];
    const newSiblings = findNodeSiblings(changedData, nodeAtDest.id);
    if (!newSiblings) return;
    const insertIndex = newSiblings.findIndex((s) => s.id === nodeAtDest.id);

    // remove the node from its old place
    const node = event.item.data;
    const siblings = findNodeSiblings(changedData, node.id);
    const siblingIndex = siblings?.findIndex((n) => n.id === node.id);
    const nodeToInsert: ExampleFlatNode = siblings?.splice(siblingIndex!, 1)[0];
    if (nodeAtDest.id === nodeToInsert.id) return;

    // ensure validity of drop - must be same level
    const nodeAtDestFlatNode = this.treeControl.dataNodes.find(
      (n) => nodeAtDest.id === n.id
    );
    if (this.validateDrop && nodeAtDestFlatNode.level !== node.level) {
      alert('Items can only be moved within the same level.');
      return;
    }

    // insert node
    newSiblings.splice(insertIndex, 0, nodeToInsert);

    // rebuild tree with mutated data
    this.rebuildTreeForData(changedData);
  }

  /**
   * Experimental - opening tree nodes as you drag over them
   */
  dragStart() {
    this.dragging = true;
  }
  dragEnd() {
    this.dragging = false;
  }
  dragHover(node: ExampleFlatNode) {
    if (this.dragging) {
      clearTimeout(this.expandTimeout);
      this.expandTimeout = setTimeout(() => {
        this.treeControl.expand(node);
      }, this.expandDelay);
    }
  }
  dragHoverEnd() {
    if (this.dragging) {
      clearTimeout(this.expandTimeout);
    }
  }

  /**
   * The following methods are for persisting the tree expand state
   * after being rebuilt
   */

  rebuildTreeForData(data: any) {
    this.dataSource.data = data;
    this.expansionModel.selected.forEach((id) => {
      const node = this.treeControl.dataNodes.find((n) => n.id === id);
      this.treeControl.expand(node);
    });
  }

  /**
   * Not used but you might need this to programmatically expand nodes
   * to reveal a particular node
   */
  private expandNodesById(flatNodes: ExampleFlatNode[], ids: string[]) {
    if (!flatNodes || flatNodes.length === 0) return;
    const idSet = new Set(ids);
    return flatNodes.forEach((node) => {
      if (idSet.has(node.id)) {
        this.treeControl.expand(node);
        let parent = this.getParentNode(node);
        while (parent) {
          this.treeControl.expand(parent);
          parent = this.getParentNode(parent);
        }
      }
    });
  }

  private getParentNode(node: ExampleFlatNode): ExampleFlatNode | null {
    const currentLevel = node.level;
    if (currentLevel < 1) {
      return null;
    }
    const startIndex = this.treeControl.dataNodes.indexOf(node) - 1;
    for (let i = startIndex; i >= 0; i--) {
      const currentNode = this.treeControl.dataNodes[i];
      if (currentNode.level < currentLevel) {
        return currentNode;
      }
    }
    return null;
  }
}
