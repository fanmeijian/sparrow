import { HttpClient, HttpParams } from "@angular/common/http"
import { Injectable } from "@angular/core"
import { MenuService } from "@sparrowmini/org-api"
import { TreeService } from "src/app/service/dynamic-tree-datasource"
import { environment } from "src/environments/environment"

@Injectable({
  providedIn: 'root',
})
export class MenuTreeService implements TreeService{
  apiBase = environment.apiBase


  constructor(
    private http: HttpClient,
    private menuService: MenuService,
  ) {

  }

  getChildren(parentId: string) {
    const httpParams = parentId? new HttpParams({fromObject: {parentId: parentId, appId: 'system'}}): {appId: 'system'}
    return this.http.get(`${environment.apiBase}/menus/my`, {params: httpParams})
  }

}
