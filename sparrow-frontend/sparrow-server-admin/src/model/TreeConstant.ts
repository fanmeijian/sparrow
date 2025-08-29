
/**
 * Food data with nested structure.
 * Each node has a name and an optional list of children.
 */
export interface TreeNode {
  expandable: boolean;
  name: string;
  code?:string;
  id: string;
  childCount: number,
  index?: number,
  children: TreeNode[];
  parentId?: string
}

/** Flat node with expandable and level information */
export interface FlatNode {
  expandable: boolean;
  name: string;
  id: string;
  code: string;
  level: number;
}
