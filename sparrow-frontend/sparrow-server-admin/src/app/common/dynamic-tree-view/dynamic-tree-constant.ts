import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface TreeNode {
  hidden: any;
  disabled: any;
  code: any;
  expandable: boolean;
  name: string;
  id: string;
  childCount: number,
  index: number,
  children?: TreeNode[];
  parentId: string
}


/** Flat node with expandable and level information */
export interface FlatTreeNode {
  expandable: boolean;
  name: string;
  id: string;
  level: number;
  childCount: number,
  parentId: string,
}

export interface TreeService {
  getChildren(parentId: string): Observable<any>;
}

