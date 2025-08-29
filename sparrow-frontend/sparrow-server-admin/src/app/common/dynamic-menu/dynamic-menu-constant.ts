
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
interface FlatTreeNode {
  id: string;
  name: string;
  code: string;
  disabled: boolean;
  hidden: boolean;
  expandable: boolean;
  level: number;
}
