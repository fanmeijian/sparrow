import { Injectable } from "@angular/core";
import { OrganizationService } from "@sparrowmini/org-api";
import { Observable, map, switchMap, zip, combineLatest } from "rxjs";
import { DynamicFlatNode } from "./dynamic-flat-node";

/**
 * Database for dynamic data. When expanding a node in the tree, the data source will need to fetch
 * the descendants data from the database.
 */
export declare interface DynamicDatabase {


  /** Initial data from database */
  initialData(): Observable<DynamicFlatNode[]> ;

  getChildren(node: any): Observable<any[]> | undefined

  isExpandable(node: any): boolean
}
