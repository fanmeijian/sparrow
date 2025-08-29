import { HttpClient, HttpParams } from "@angular/common/http";
import { TreeService } from "../../common/dynamic-tree-view/dynamic-tree-constant";
import { BASE_PATH } from "@sparrowmini/org-api";
import { Inject, Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class DictTreeService implements TreeService{

  constructor(
    private http: HttpClient,
    @Inject(BASE_PATH) private apiBase:string,
  ){

  }

  getChildren(parentId: string) {
    return this.http.get<any>(this.apiBase + '/dicts/' + parentId + '/children', {params: new HttpParams({fromObject:  {page: 0,size:1000000}})})
  }
}
