import { HttpClient, HttpParams } from "@angular/common/http"
import { Injectable } from "@angular/core"
import { DynamicFlatNode, TreeService } from "@sparrowmini/common-ui-nm"
import { map, Observable } from "rxjs"
import { environment } from "src/environments/environment"

@Injectable({
  providedIn: 'root',
})
export class MenuTreeService implements TreeService {
  apiBase = environment.apiBase


  constructor(
    private http: HttpClient,
  ) {

  }
  move(nodeId: any, nextNodeId: any): Observable<void> {
    throw new Error("Method not implemented.")
  }
  getChildren(node: any): Observable<DynamicFlatNode[]> {
    const parentId = node?.id||node?.code
    const httpParams = parentId ? new HttpParams({ fromObject: { parentId: parentId, appId: 'system' } }) : { appId: 'system' }
    return this.http.get(`${environment.apiBase}/menus/my`, { params: httpParams }).pipe(
      map((res: any) => res.content.map((m: any) => {
        return { ...m }
      })))
  }
  initialData(): Observable<DynamicFlatNode[]> {
    return this.getChildren({code:'RULE_MGT'})
  }

  // getChildren(parentId: string) {
  //   const httpParams = parentId? new HttpParams({fromObject: {parentId: parentId, appId: 'system'}}): {appId: 'system'}
  //   return this.http.get(`${environment.apiBase}/menus/my`, {params: httpParams})
  // }

}
