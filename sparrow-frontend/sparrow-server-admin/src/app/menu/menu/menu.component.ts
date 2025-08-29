import { Component, Injectable, OnInit } from '@angular/core';
import { MenuService, SparrowTreeMenuString } from '@sparrowmini/org-api';
import {
  MatTreeFlattener,
  MatTreeFlatDataSource,
  MatTreeNestedDataSource,
} from '@angular/material/tree';
import { FlatTreeControl, NestedTreeControl } from '@angular/cdk/tree';
import { SelectionModel } from '@angular/cdk/collections';
import { BehaviorSubject } from 'rxjs';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MenuCreateComponent } from '../../menu/menu-create/menu-create.component';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MenuPermissionComponent } from '../../menu/menu-permission/menu-permission.component';

import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpClient } from '@angular/common/http';
import { SprappListComponent } from 'src/app/sprapp/sprapp-list/sprapp-list.component';
import { environment } from 'src/environments/environment';

export const menuClassName = 'cn.sparrowmini.permission.menu.model.Menu';


/**
 * Node for to-do item
 */
export class TodoItemNode {
  children!: TodoItemNode[];
  id!: string;
  code: string;
  name: string;
  me: any;
}

// interface ExampleFlatNode {
//   id:string;
//   expandable: boolean;
//   name: string;
//   level: number;
//   children: ExampleFlatNode[];
// }

/** Flat to-do item node with expandable and level information */
export class TodoItemFlatNode {
  id!: string;
  me: any;
  name: string;
  code: string;
  level!: number;
  expandable!: boolean;
  childCount: any;
  children!: TodoItemFlatNode[];
}

/**
 * Checklist database, it can build a tree structured Json object.
 * Each node in Json object represents a to-do item or a category.
 * If a node is a category, it has children items and new items can be added under the category.
 */
@Injectable()
export class ChecklistDatabase {
  dataChange = new BehaviorSubject<TodoItemNode[]>([]);

  get data(): TodoItemNode[] {
    return this.dataChange.value;
  }

  constructor(private menuService: MenuService) {
    this.initialize();
  }

  initialize() {
    // Build the tree nodes from Json object. The result is a list of `TodoItemNode` with nested
    //     file node as children.
    this.menuService.menuTree('MENU').subscribe((res: any) => {
      // const data = this.buildFileTree(res.children!, 0);
      const data: any = res.content!;
      // Notify the change.
      this.dataChange.next(data);
    });
  }

  /**
   * Build the file structure tree. The `value` is the Json object, or a sub-tree of a Json object.
   * The return value is the list of `TodoItemNode`.
   */
  buildFileTree(obj: { [key: string]: any }, level: number): TodoItemNode[] {
    return Object.keys(obj).reduce<TodoItemNode[]>((accumulator, key) => {
      const value = obj[key];
      const node = new TodoItemNode();
      node.id = key;

      if (value != null) {
        if (typeof value === 'object') {
          node.children = this.buildFileTree(value, level + 1);
        } else {
          node.id = value;
        }
      }

      return accumulator.concat(node);
    }, []);
  }

  /** Add an item to to-do list */
  insertItem(parent: TodoItemNode, name: string) {
    if (parent.children) {
      parent.children.push({ id: name } as TodoItemNode);
      this.dataChange.next(this.data);
    }
  }

  updateItem(node: TodoItemNode, name: string) {
    node.id = name;
    this.dataChange.next(this.data);
  }
}

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss', '../../org.css'],
  providers: [ChecklistDatabase],
})
export class MenuComponent implements OnInit {
  openAppDlg() {
    this.dialog.open(SprappListComponent).afterClosed().subscribe(res => {

      const body = this.checklistSelection.selected.map(m => {
        return {
          id: m.id,
          appId: res[0].id,
        }
      })

      this.http.patch(`${environment.apiBase}/menus`, body).subscribe()
    });
  }
  /** Map from flat node to nested node. This helps us finding the nested node to be modified */
  flatNodeMap = new Map<any, any>();

  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  nestedNodeMap = new Map<any, any>();

  /** A selected parent node to be inserted */
  selectedParent: TodoItemFlatNode | null = null;

  /** The new item's name */
  newItemName = '';

  treeFlattener: MatTreeFlattener<any, any>;

  treeControl: FlatTreeControl<any>;
  dataSource: MatTreeFlatDataSource<any, any>;

  // treeControl = new NestedTreeControl<any>(node => node.children);
  // dataSource = new MatTreeNestedDataSource<any>();

  /** The selection for checklist */
  checklistSelection = new SelectionModel<TodoItemFlatNode>(
    true /* multiple */
  );

  constructor(
    private _database: ChecklistDatabase,
    private dialog: MatDialog,
    private menuService: MenuService,
    private snack: MatSnackBar,
    private http: HttpClient
  ) {
    this.treeFlattener = new MatTreeFlattener(
      this.transformer,
      this.getLevel,
      this.isExpandable,
      this.getChildren
    );
    this.treeControl = new FlatTreeControl<TodoItemFlatNode>(
      this.getLevel,
      this.isExpandable
    );
    this.dataSource = new MatTreeFlatDataSource(
      this.treeControl,
      this.treeFlattener
    );


    // this.treeControl = new NestedTreeControl<any>(node => node.children);
    // this.dataSource = new MatTreeNestedDataSource<any>();

    _database.dataChange.subscribe((data) => {
      this.dataSource.data = data;
      //cache to storage for route guard permission check
      // let menus = data.flat().map(m=>Object.assign({},{id:m.id,path: m.me.url, parentId: m.me.parentId}))
      this.toSessionCache(data);
      sessionStorage.setItem('menus', JSON.stringify(this.menus));
    });
  }

  menus: any[] = [];
  toSessionCache(data: any[]) {
    if (data) {
      data.forEach((f) => {
        this.menus.push({ id: f.id, path: f.url, parentId: f.parentId });
        if (f.children.length > 0) {
          this.toSessionCache(f.children);
        }
      });
    }
  }

  getNestedNode(selectedMenu) {
    console.log(selectedMenu)
    console.log(this.nestedNodeMap.get(selectedMenu.id))
  }

  ngOnInit(): void {
    this._database.initialize();
    this.http.get(`${'http://localhost:8080'}/menu`).subscribe()
  }

  getLevel = (node: TodoItemFlatNode) => node.level;

  isExpandable = (node: TodoItemFlatNode) => node.expandable;

  getChildren = (node: TodoItemNode): TodoItemNode[] => node.children;

  hasChild = (_: number, _nodeData: TodoItemFlatNode) => _nodeData.expandable;

  hasNoContent = (_: number, _nodeData: TodoItemFlatNode) =>
    _nodeData.id === '';

  /**
   * Transformer to convert nested node to flat node. Record the nodes in maps for later use.
   */
  transformer = (node: any, level: number) => {
    const existingNode = this.nestedNodeMap.get(node);
    const flatNode: any =
      existingNode && existingNode.id === node.id
        ? existingNode
        : {};
    // flatNode = Object.assign({},node,{expandable: node.childCount>0})
    // Object.keys(node).forEach(key => {
    //   if (key !== 'children' && key != 'childCount') {
    //     flatNode[key] = node[key]

    //   }
    // })

    flatNode.expandable = node.childCount > 0
    flatNode.id = node.id;
    flatNode.name = node.name;
    flatNode.code = node.code
    flatNode.level = level;
    flatNode.url = node.url;
    flatNode.parentId = node.parentId
    flatNode.parentName = node.parentName
    flatNode.expandable = !!node.children?.length;
    flatNode.childCount = node.children?.length;

    this.flatNodeMap.set(flatNode, node);
    this.nestedNodeMap.set(node.id, flatNode);
    return flatNode;
  };

  /** Whether all the descendants of the node are selected. */
  descendantsAllSelected(node: TodoItemFlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node);
    const descAllSelected =
      descendants.length > 0 &&
      descendants.every((child) => {
        return this.checklistSelection.isSelected(child);
      });
    return descAllSelected;
  }

  /** Whether part of the descendants are selected */
  descendantsPartiallySelected(node: TodoItemFlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node);
    const result = descendants.some((child) =>
      this.checklistSelection.isSelected(child)
    );
    return result && !this.descendantsAllSelected(node);
  }

  /** Toggle the to-do item selection. Select/deselect all the descendants node */
  todoItemSelectionToggle(node: TodoItemFlatNode): void {
    this.checklistSelection.toggle(node);
    const descendants = this.treeControl.getDescendants(node);
    this.checklistSelection.isSelected(node)
      ? this.checklistSelection.select(...descendants)
      : this.checklistSelection.deselect(...descendants);

    // Force update for the parent
    descendants.forEach((child) => this.checklistSelection.isSelected(child));
    this.checkAllParentsSelection(node);
  }

  /** Toggle a leaf to-do item selection. Check all the parents to see if they changed */
  todoLeafItemSelectionToggle(node: TodoItemFlatNode): void {
    this.checklistSelection.toggle(node);
    this.checkAllParentsSelection(node);
  }

  /* Checks all the parents when a leaf node is selected/unselected */
  checkAllParentsSelection(node: TodoItemFlatNode): void {
    let parent: TodoItemFlatNode | null = this.getParentNode(node);
    while (parent) {
      this.checkRootNodeSelection(parent);
      parent = this.getParentNode(parent);
    }
  }

  /** Check root node checked state and change it accordingly */
  checkRootNodeSelection(node: TodoItemFlatNode): void {
    const nodeSelected = this.checklistSelection.isSelected(node);
    const descendants = this.treeControl.getDescendants(node);
    const descAllSelected =
      descendants.length > 0 &&
      descendants.every((child) => {
        return this.checklistSelection.isSelected(child);
      });
    if (nodeSelected && !descAllSelected) {
      this.checklistSelection.deselect(node);
    } else if (!nodeSelected && descAllSelected) {
      this.checklistSelection.select(node);
    }
  }

  /* Get the parent node of a node */
  getParentNode(node: TodoItemFlatNode): TodoItemFlatNode | null {
    const currentLevel = this.getLevel(node);

    if (currentLevel < 1) {
      return null;
    }

    const startIndex = this.treeControl.dataNodes.indexOf(node) - 1;

    for (let i = startIndex; i >= 0; i--) {
      const currentNode = this.treeControl.dataNodes[i];

      if (this.getLevel(currentNode) < currentLevel) {
        return currentNode;
      }
    }
    return null;
  }

  /** Select the category so we can insert the new item. */
  addNewItem(node: TodoItemFlatNode) {
    const parentNode = this.flatNodeMap.get(node);
    this._database.insertItem(parentNode!, '');
    this.treeControl.expand(node);
  }

  /** Save the node to database */
  saveNode(node: TodoItemFlatNode, itemValue: string) {
    const nestedNode = this.flatNodeMap.get(node);
    this._database.updateItem(nestedNode!, itemValue);
  }

  new() {
    this.dialog.open(MenuCreateComponent, { width: '90%' });
  }

  delete() {
    const body = this.checklistSelection.selected.map((m) => m.id)
    this.http.patch(`${environment.jpaBase}/${menuClassName}/delete`, body).subscribe(() => {
      this.snack.open('删除成功！', '关闭');
    })
  }

  deleteMenu(menu: any) {
    const body = [menu.id]
    this.http.patch(`${environment.jpaBase}/${menuClassName}/delete`, body).subscribe(() => {
      this.snack.open('删除成功！', '关闭');
    })
  }

  permission() {
    this.dialog.open(MenuPermissionComponent, {
      data: this.checklistSelection.selected.map((m) => this.flatNodeMap.get(m)),
      width: '90%',
    });
  }

  openPermissionDialog(menu: any) {
    this.dialog
      .open(MenuPermissionComponent, {
        data: [this.flatNodeMap.get(menu)],
        width: '740px',
        height: '60%'
      })
      .afterClosed()
      .subscribe((res) => {
        if (res) {
          this.snack.open('授权成功！', '关闭');
        }
      });
  }

  permissions: any = {};
  selectedMenu: any;
  onMenuClick(menu: any) {
    // console.log(menu);
    this.selectedMenu = menu;

    this.menuService.menuPermissions(menu.id, 'USER').subscribe((res: any) => {
      this.permissions.users = res.userMenus.map(m => m.id.username);
      this.permissions.sysroles = res.sysroleMenus.map(m => m.sysrole)

    });

    // this.menuService.menuPermissions(menu.id, 'SYSROLE').subscribe((res) => {
    //   this.permissions.sysroles = res;
    // });
  }

  removePermission(users: any[], sysroles: any[]) {
    const body = {
      sysroleMenuIds: sysroles.map((m) =>
        Object.assign({}, { sysroleId: m.id, menuId: this.selectedMenu.id })
      ),
      userMenuIds: users.map((m) =>
        Object.assign({}, { username: m, menuId: this.selectedMenu.id })
      ),
    }
    this.http.post(`${environment.apiBase}/menus/permissions/remove`, body)
      .subscribe(() => {
        sysroles.forEach((f) => {
          const index = this.permissions.sysroles.indexOf(f);
          if (index >= 0) {
            this.permissions.sysroles.splice(index, 1);
          }
        });

        users.forEach((f) => {
          const index = this.permissions.sysroles.indexOf(f);
          if (index >= 0) {
            this.permissions.users.splice(index, 1);
          }
        });

        this.snack.open('删除成功!', '关闭');
      });
  }

  edit(menu: any) {
    this.dialog.open(MenuCreateComponent, { width: '90%', data: this.flatNodeMap.get(menu) });
  }

  dragging = false;
  expandTimeout: any;
  expandDelay = 1000;
  validateDrop = false;
  expansionModel = new SelectionModel<string>(true);

  visibleNodes(): any[] {
    const result: any[] = [];
    console.log('this.expansionModel.selected', this.expansionModel.selected);
    function addExpandedChildren(node: any, expanded: string[]) {
      result.push(node);
      if (expanded.includes(node.id)) {
        node.children.map((child) => addExpandedChildren(child, expanded));
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
  drop(event: CdkDragDrop<any[]>) {
    const data = event.container.data
    const originIndex = event.previousIndex
    const destinationIndex = event.currentIndex
    console.log(event)
    console.log(
      'origin/destination',
      event.previousIndex,
      event.currentIndex,
      // event.container.data,
      // event.item.data
    );
    // ignore drops outside of the tree
    if (!event.isPointerOverContainer) return;

    // construct a list of visible nodes, this will match the DOM.
    // the cdkDragDrop event.currentIndex jives with visible nodes.
    // it calls rememberExpandedTreeNodes to persist expand state
    const visibleNodes = this.visibleNodes();
    console.log(visibleNodes);

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
    const nextNode = visibleNodes[originIndex < destinationIndex ? destinationIndex + 1 : destinationIndex];
    const nodeAtDest = visibleNodes[destinationIndex];
    const newSiblings = findNodeSiblings(changedData, nodeAtDest.id);
    if (!newSiblings) return;
    const insertIndex = newSiblings.findIndex((s) => s.id === nodeAtDest.id);

    // remove the node from its old place
    const node = event.item.data;
    const siblings = findNodeSiblings(changedData, node.id);
    const siblingIndex = siblings?.findIndex((n) => n.id === node.id);
    const nodeToInsert: TodoItemNode = siblings?.splice(siblingIndex!, 1)[0];
    if (nodeAtDest.id === nodeToInsert.id) return;

    // ensure validity of drop - must be same level
    const nodeAtDestFlatNode = this.treeControl.dataNodes.find(
      (n) => nodeAtDest.id === n.id
    );
    // if (this.validateDrop && nodeAtDestFlatNode?.level !== node.level) {
    //   alert('Items can only be moved within the same level.');
    //   return;
    // }

    if (nodeAtDestFlatNode?.level !== node.level || nodeAtDestFlatNode?.parentId != node.parentId) {
      alert('仅允许同一父层级排序');
      return;
    }



    //insert node
    let prevId: any = undefined;
    let nextId: any = undefined;
    const nextNode_ = originIndex < destinationIndex ? nextNode : nodeAtDestFlatNode

    console.log('node,nextNode', node, nextNode_)

    if (nextNode_.parentId === node.parentId) {
      // move to last
      nextId = nextNode_.id
    }


    let httpParams: any = { currentId: node.id }

    if (nextId) {
      httpParams = { currentId: node.id, nextId: nextId }
    }

    this.http.post(`${environment.apiBase}/menu/move`, null, { params: httpParams }).subscribe(() => {

    })

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
  dragHover(node: TodoItemFlatNode) {
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
      this.treeControl.expand(node!);
    });
  }

  /**
   * Not used but you might need this to programmatically expand nodes
   * to reveal a particular node
   */
  private expandNodesById(flatNodes: TodoItemFlatNode[], ids: string[]) {
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

  // private getParentNode(node: ExampleFlatNode): ExampleFlatNode | null {
  //   const currentLevel = node.level;
  //   if (currentLevel < 1) {
  //     return null;
  //   }
  //   const startIndex = this.treeControl.dataNodes.indexOf(node) - 1;
  //   for (let i = startIndex; i >= 0; i--) {
  //     const currentNode = this.treeControl.dataNodes[i];
  //     if (currentNode.level < currentLevel) {
  //       return currentNode;
  //     }
  //   }
  //   return null;
  // }
}
