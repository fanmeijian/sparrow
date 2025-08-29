import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Inject, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatTreeFlattener, MatTreeFlatDataSource } from '@angular/material/tree';
import { TreeNode, FlatTreeNode, TreeService } from './dynamic-tree-constant';
import { DynamicDataSource, DynamicFlatNode } from './dynamic-tree-datasource';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { SelectionModel } from '@angular/cdk/collections';
import { MatLegacyCheckboxChange as MatCheckboxChange } from '@angular/material/legacy-checkbox';
import { BASE_PATH } from '@sparrowmini/org-api';

@Component({
  selector: 'app-dynamic-tree-view',
  templateUrl: './dynamic-tree-view.component.html',
  styleUrls: ['./dynamic-tree-view.component.scss']
})
export class DynamicTreeViewComponent implements OnInit {

  moveNode(_t17: any) {
    throw new Error('Method not implemented.');
  }
  onDrop(event: any) {
    // Implement drop logic here (e.g., reordering items)
    console.log('Dropped node:', event);
    const prevIndex = this.dataSource.data.findIndex((d) => d === event.item.data);
    moveItemInArray(this.dataSource.data, prevIndex, event.currentIndex);
    this.treeControl.dataNodes = this.dataSource.data;
  }
  dragAndDropEnabled = false;
  toggleDragAndDrop() {
    this.dragAndDropEnabled = !this.dragAndDropEnabled;
  }


  @Input() treeService!: TreeService;
  @Input() parentId: string = ''
  @Input() link?: string;
  @Output() onNodeClick: EventEmitter<TreeNode> = new EventEmitter();

  pageable: Map<string, any> = new Map()

  ngOnInit(): void {

  }
  dataSource!: DynamicDataSource;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject(BASE_PATH) private apiBase: string,
  ) {


  }
  ngOnChanges(changes: SimpleChanges): void {
    this.dataSource = new DynamicDataSource(this.treeControl, this.treeService);
    this.treeService.getChildren(this.parentId)
      .subscribe((res: any) => {
        this.pageable.set(this.parentId, res.pageable)
        let nestedNodes: any = res.content.map((value: any, index: number) => Object.assign(value, { index: index, parentId: this.parentId }))
        this.dataSource.data = nestedNodes
      })

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
  hasChild = (_: number, node: FlatTreeNode) => node.childCount > 0;





  // expansion model tracks expansion state
  expansionModel = new SelectionModel<string>(true);
  dragging = false;
  expandTimeout: any;
  expandDelay = 1000;
  validateDrop = false;

  // DRAG AND DROP METHODS

  shouldValidate(event: MatCheckboxChange): void {
    this.validateDrop = event.checked;
  }

  /**
   * This constructs an array of nodes that matches the DOM
   */
  visibleNodes(): any[] {
    const result: any[] = [];

    function addExpandedChildren(node: any, expanded: string[]) {
      result.push(node);
      if (expanded.includes(node.id)) {
        node.children.map((child: any) => addExpandedChildren(child, expanded));
      }
    }
    this.dataSource.data.forEach((node) => {
      addExpandedChildren(node, this.expansionModel.selected);
    });
    return result;
  }

  /**
   * Handle the drop - here we rearrange the data based on the drop event,
   * then rebuild the tree.
   * */
  drop(event: CdkDragDrop<string[]>) {
    console.log('origin/destination', event.previousIndex, event.currentIndex);
    this.dataSource.data.forEach((v, i) => {
      console.log(i, v.name, v.level, v.parentId)
    })
    let d: any = this.dataSource.data
    let currentNode = d[event.previousIndex]
    let currentId = currentNode.id

    let params: any = {}
    if (event.currentIndex == 0) {
      params = {
        nextId: d[0].id
      }
    } else if (event.currentIndex == this.dataSource.data.length - 1) {
      params = {
        previousId: d[d.length - 1].id
      }
    } else {

      if (event.currentIndex < event.previousIndex) {
        //向前排序
        let preNode = d[event.currentIndex - 1]
        let nextNode = d[event.currentIndex]
        params =
          preNode.level == currentNode.level ?
            {
              previousId: preNode.id,
              nextId: nextNode.id
            }
            : {
              nextId: nextNode.id
            }
      } else {
        let preNode = d[event.currentIndex]
        let nextNode = d[event.currentIndex + 1]
        params =
          nextNode.level == currentNode.level ?
            {
              previousId: preNode.id,
              nextId: nextNode.id
            } : {
              previousId: preNode.id,
            }
      }

    }

    this.http.post(`${this.apiBase}/dicts/${currentId}/move`, {}, {
      params: new HttpParams({
        fromObject: params
      })
    }).subscribe(() => {
      moveItemInArray(d, event.previousIndex, event.currentIndex);
      this.dataSource.dataChange.next(d)
    })

    return
    // ignore drops outside of the tree
    if (!event.isPointerOverContainer) return;

    // construct a list of visible nodes, this will match the DOM.
    // the cdkDragDrop event.currentIndex jives with visible nodes.
    // it calls rememberExpandedTreeNodes to persist expand state
    const visibleNodes = this.visibleNodes();

    // deep clone the data source so we can mutate it
    const changedData = JSON.parse(JSON.stringify(this.dataSource.data));

    // recursive find function to find siblings of node
    function findNodeSiblings(arr: Array<any>, id: string): Array<any> {
      let result, subResult;
      arr.forEach((item, i) => {
        if (item.id === id) {
          result = arr;
        } else if (item.children) {
          subResult = findNodeSiblings(item.children, id);
          if (subResult) result = subResult;
        }
      });
      return result!;

    }

    // determine where to insert the node
    const nodeAtDest = visibleNodes[event.currentIndex];
    const newSiblings = findNodeSiblings(changedData, nodeAtDest.id);
    if (!newSiblings) return;
    const insertIndex = newSiblings.findIndex(s => s.id === nodeAtDest.id);

    // remove the node from its old place
    const node = event.item.data;
    const siblings = findNodeSiblings(changedData, node.id);
    const siblingIndex = siblings.findIndex(n => n.id === node.id);
    const nodeToInsert: any = siblings.splice(siblingIndex, 1)[0];
    if (nodeAtDest.id === nodeToInsert.id) return;

    // ensure validity of drop - must be same level
    const nodeAtDestFlatNode = this.treeControl.dataNodes.find((n) => nodeAtDest.id === n.id);
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
  dragHover(node: any) {
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
  private expandNodesById(flatNodes: any[], ids: string[]) {
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

  private getParentNode(node: any): any | null {
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
