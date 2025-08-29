import { Injectable } from "@angular/core";
import { OrganizationService } from "@sparrowmini/org-api";
import { Observable, map, switchMap, zip, combineLatest } from "rxjs";
import { DynamicFlatNode } from "./dynamic-flat-node";
import { DynamicDatabase } from "./dynamic-database";

/**
 * Database for dynamic data. When expanding a node in the tree, the data source will need to fetch
 * the descendants data from the database.
 */
@Injectable({ providedIn: "root" })
export class OrgDynamicDatabase implements DynamicDatabase {
  constructor(private orgService: OrganizationService) {}

  /** Initial data from database */
  initialData(): Observable<DynamicFlatNode[]> {
    return this.orgService.orgChildren("ROOT", "ORGANIZATION").pipe(
      map((res: any) => res.content),
      switchMap((s: any[]) =>
        zip(
          ...s.map((m) => {
            const $org = this.orgService.org(m.id);
            const $childCount = this.orgService.orgChildCount(
              m.id,'ORGANIZATION'
            );
            return combineLatest($org, $childCount).pipe(
              map((a: any) => Object.assign({}, a[0], { childCount: a[1] }))
            );
          })
        )
      ),
      map((res: any) => res.map((a: any) => new DynamicFlatNode(a, 0, a.childCount>0)))
    );
  }

  getChildren(node: any): Observable<any[]> | undefined {
    return this.orgService.orgChildren(node.id, "ORGANIZATION").pipe(
      map((res: any) => res.content),
      switchMap((s: any[]) =>
        zip(
          ...s.map((m) => {
            const $org = this.orgService.org(m.id.organizationId);
            const $childCount = this.orgService.orgChildCount(
              m.id.organizationId,'ORGANIZATION'
            );
            return combineLatest($org, $childCount).pipe(
              map((a: any) => Object.assign({}, a[0], { childCount: a[1] }))
            );
          })
        )
      ),
      map((res: any) => res.map((a: any) => new DynamicFlatNode(a, 0, true)))
    );
  }

  isExpandable(node: any): boolean {
    return node.item.childCount > 0;
  }
}
