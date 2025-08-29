import { Injectable } from "@angular/core";
import {
  EmployeeService,
  JoblevelService,
  OrganizationService,
  RoleService,
} from "@sparrowmini/org-api";
import { Observable, map, switchMap, zip, combineLatest, of, tap } from "rxjs";
import { DynamicFlatNode } from "./dynamic-flat-node";
import { DynamicDatabase } from "./dynamic-database";

/**
 * Database for dynamic data. When expanding a node in the tree, the data source will need to fetch
 * the descendants data from the database.
 */
@Injectable({ providedIn: "root" })
export class EmployeeDynamicDatabase implements DynamicDatabase {
  constructor(
    private orgService: OrganizationService,
    private employeeService: EmployeeService
  ) {}

  /** Initial data from database */
  initialData(): Observable<DynamicFlatNode[]> {
    return this.orgService.orgChildren("ROOT", "ORGANIZATION").pipe(
      map((res: any) => res.content),
      switchMap((s: any[]) =>
        zip(
          ...s.map((m) => {
            const $org = this.orgService.org(m.id);
            const $childCount = this.orgService.orgChildCount(
              m.id,
              "ORGANIZATION"
            );
            return combineLatest($org, $childCount).pipe(
              map((a: any) => Object.assign({}, a[0], { childCount: a[1] }))
            );
          })
        )
      ),
      map((res: any) =>
        res.map((a: any) => new DynamicFlatNode(a, 0, a.childCount > 0))
      )
    );
  }

  getChildren(node: any): Observable<any[]> | undefined {
    const $employees = this.orgService.orgChildren(node.id, "EMPLOYEE").pipe(
      map((res) => res.content!),
      switchMap((roles: any[]) =>
        roles.length === 0
          ? of([])
          : zip(...roles?.map((m) => this.employeeService.employee(m.id)))
      ),
      map((res: any) =>
        res.map(
          (a: any) =>
            new DynamicFlatNode(
              Object.assign({}, a, { type: "EMPLOYEE" }),
              0,
              true
            )
        )
      )
    );
    const $orgs = this.orgService.orgChildren(node.id, "ORGANIZATION").pipe(
      map((res: any) => res.content),
      switchMap((s: any[]) =>
        s.length > 0
          ? zip(
              ...s.map((m) => {
                const $org = this.orgService.org(m.id.organizationId);
                const $childCount = this.orgService
                  .orgChildCount(m.id.organizationId, "ORGANIZATION")
                  .pipe(
                    switchMap((s) =>
                      this.orgService
                        .orgChildCount(m.id.organizationId, "EMPLOYEE")
                        .pipe(map((m) => m + s))
                    )
                  );

                return combineLatest($org, $childCount).pipe(
                  map((a: any) => Object.assign({}, a[0], { childCount: a[1] }))
                );
              })
            )
          : of([])
      ),
      map((res: any) => res.map((a: any) => new DynamicFlatNode(a, 0, true))),
      // tap((res) => console.log(res))
    );

    return combineLatest($orgs, $employees).pipe(map((m) => m[0].concat(m[1])));
  }

  isExpandable(node: any): boolean {
    return node.item.childCount > 0;
  }
}
