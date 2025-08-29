import { Component, OnInit, Injectable, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { SelectionModel } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { BehaviorSubject } from 'rxjs';
/**
 * Node for to-do item
 */
export class TodoItemNode {
  children!: TodoItemNode[];
  id!: string;
  name!: string;
  code!: string;
}

/** Flat to-do item node with expandable and level information */
export class TodoItemFlatNode {
  id!: string;
  name!: string;
  code!: string;
  level!: number;
  expandable!: boolean;
}

/**
 * The Json object for to-do list data.
 */
const TREE_DATA = {
  Root: {
    Groceries: {
      'Almond Meal flour': null,
      'Organic eggs': null,
      'Protein Powder': null,
      Fruits: {
        Apple: null,
        Berries: ['Blueberry', 'Raspberry'],
        Orange: null
      }
    },
    Reminders: {
      'Cook dinner': null,
      'Read the Material Design spec': null,
      'Upgrade Application to Angular': null,
      Fruits: {
        Apple: null,
        Berries: ['Orange', 'Mango'],
        Orange: null
      }
    },
    Reminders1: {
      'Cook dinner 1': null,
      'Read the Material Design spec 1': null,
      'Upgrade Application to Angular 1': null,
      Fruits: {
        Apple: null,
        Berries: ['Grapes', 'Pomegranate'],
        Orange: null
      }
    },
    Reminders2: {
      'Cook dinner 2': null,
      'Read the Material Design spec 2': null,
      'Upgrade Application to Angular 2': null,
      Fruits: {
        Apple: null,
        Berries: ['Grapes1', 'Pomegranate1'],
        Orange: null
      }
    },
    Reminders3: [
      'Cook dinner 3',
      'Read the Material Design spec 3',
      'Upgrade Application to Angular 3',
    ]
  }
};

/**
 * Checklist database, it can build a tree structured Json object.
 * Each node in Json object represents a to-do item or a category.
 * If a node is a category, it has children items and new items can be added under the category.
 */
@Injectable({
  providedIn: 'root',
})
export class ChecklistDatabase {


  dataChange = new BehaviorSubject<TodoItemNode[]>([]);
  get data(): TodoItemNode[] { return this.dataChange.value; }

  constructor(
  ) {
    // this.initialize();
  }

  initialize(treeData: any) {
    // Build the tree nodes from Json object  . The result is a list of `TodoItemNode` with nested
    //     file node as children.
    // const data = this.buildFileTree(treeData, 0);
    // console.log(data)
    // Notify the change.
    this.dataChange.next(treeData);
  }

  /**
   * Build the file structure tree. The `value` is the Json object, or a sub-tree of a Json object.
   * The return value is the list of `TodoItemNode`.
   */
  // buildFileTree(obj: { [key: string]: any }, level: number): any[] {
  //   return Object.keys(obj).reduce<any[]>((accumulator, key) => {
  //     const value = obj[key];
  //     const node = new TodoItemNode();
  //     node.item = key;

  //     if (value != null) {
  //       if (typeof value === 'object') {
  //         node.children = this.buildFileTree(value, level + 1);
  //       } else {
  //         node.item = value;
  //       }
  //     }

  //     return accumulator.concat(
  //       {
  //         ...node,
  //         otherInfo: key
  //       }
  //     );
  //   }, []);
  // }

  /** Add an item to to-do list */
  // insertItem(parent: TodoItemNode, name: string) {
  //   if (parent.children) {
  //     parent.children.push({ item: name } as TodoItemNode);
  //     this.dataChange.next(this.data);
  //   }
  // }

  // updateItem(node: TodoItemNode, name: string) {
  //   node.item = name;
  //   this.dataChange.next(this.data);
  // }
}

@Component({
  selector: 'app-filter-tree',
  templateUrl: './filter-tree.component.html',
  styleUrls: ['./filter-tree.component.scss']
})
export class FilterTreeComponent implements OnInit, OnChanges {
  @Input() checklistSelection!: SelectionModel<TodoItemFlatNode>;
  @Input() treeData: any;
  @Input() checkedIds?: string[];
  @Input() disabled?: boolean;

  /** Map from flat node to nested node. This helps us finding the nested node to be modified */
  flatNodeMap = new Map<TodoItemFlatNode, TodoItemNode>();

  searchString = ''

  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  nestedNodeMap = new Map<TodoItemNode, TodoItemFlatNode>();

  /** A selected parent node to be inserted */
  selectedParent: TodoItemFlatNode | null = null;

  /** The new item's name */
  newItemName = '';

  treeControl: FlatTreeControl<TodoItemFlatNode>;

  treeFlattener: MatTreeFlattener<TodoItemNode, TodoItemFlatNode>;

  dataSource: MatTreeFlatDataSource<TodoItemNode, TodoItemFlatNode>;

  /** The selection for checklist */
  // checklistSelection = new SelectionModel<TodoItemFlatNode>(true /* multiple */);

  constructor(private _database: ChecklistDatabase) {
    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel,
      this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<TodoItemFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    _database.dataChange.subscribe(data => {
      this.dataSource.data = data;
      this.treeControl.expand(this.treeControl.dataNodes[0])
    });
  }
  ngOnChanges(changes: SimpleChanges): void {
    console.log(this.treeData)


    if (this.treeData) {
      this._database.initialize(this.treeData);;
      if (this.checkedIds) {
        this.checkedIds.forEach(id => {
          const node = this.treeControl.dataNodes.find(m => m.id === id)
          this.checklistSelection.select(node!)
        })
      }
    }
  }
  ngOnInit(): void {

  }

  getLevel = (node: TodoItemFlatNode) => node.level;

  isExpandable = (node: TodoItemFlatNode) => node.expandable;

  getChildren = (node: TodoItemNode): TodoItemNode[] => node.children;

  hasChild = (_: number, _nodeData: TodoItemFlatNode) => _nodeData.expandable;

  hasNoContent = (_: number, _nodeData: TodoItemFlatNode) => _nodeData.id === null;

  /**
   * Transformer to convert nested node to flat node. Record the nodes in maps for later use.
   */
  transformer = (node: TodoItemNode, level: number) => {
    const existingNode = this.nestedNodeMap.get(node);
    const flatNode = existingNode && existingNode.id === node.id
      ? existingNode
      : new TodoItemFlatNode();
    flatNode.id = node.id;
    flatNode.name = node.name;
    flatNode.code = node.code;
    flatNode.level = level;
    flatNode.expandable = !!node.children?.length;
    this.flatNodeMap.set(flatNode, node);
    this.nestedNodeMap.set(node, flatNode);
    return flatNode;
  }

  // search filter logic start
  filterLeafNode(node: TodoItemFlatNode): boolean {
    if (!this.searchString) {
      return false
    }
    return node.name.toLowerCase()
      .indexOf(this.searchString?.toLowerCase()) === -1

  }

  filterParentNode(node: TodoItemFlatNode): boolean {

    if (
      !this.searchString ||
      node.name.toLowerCase().indexOf(this.searchString?.toLowerCase()) !==
      -1
    ) {
      return false
    }
    const descendants = this.treeControl.getDescendants(node)

    if (
      descendants.some(
        (descendantNode) =>
          descendantNode.name
            .toLowerCase()
            .indexOf(this.searchString?.toLowerCase()) !== -1
      )
    ) {
      return false
    }

    return true
  }
  // search filter logic end

  /** Whether all the descendants of the node are selected. */
  descendantsAllSelected(node: TodoItemFlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node);
    const descAllSelected = descendants.length > 0 && descendants.every(child => {
      return this.checklistSelection.isSelected(child);
    });
    return descAllSelected;
  }

  /** Whether part of the descendants are selected */
  descendantsPartiallySelected(node: TodoItemFlatNode): boolean {
    const descendants = this.treeControl.getDescendants(node);
    const result = descendants.some(child => this.checklistSelection.isSelected(child));
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
    descendants.forEach(child => this.checklistSelection.isSelected(child));
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
    const descAllSelected = descendants.length > 0 && descendants.every(child => {
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
}
