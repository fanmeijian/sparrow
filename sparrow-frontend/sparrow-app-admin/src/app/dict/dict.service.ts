import { Inject, Injectable } from '@angular/core';
import { BASE_PATH } from '../app.module';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import * as _ from 'lodash'
import { TreeService, DynamicFlatNode } from '@sparrowmini/common-ui-nm';
import { CommonTreeService } from '@sparrowmini/common-api';
export const DictClass = 'cn.sparrowmini.common.model.Dict'

@Injectable({
  providedIn: 'root'
})
export class DictService implements TreeService {

  constructor(
    @Inject(BASE_PATH) public apiBase: string,
    private http: HttpClient,
    private commonTreeService: CommonTreeService,
  ) { }
  treeClass = DictClass
  move(nodeId: any, nextNodeId: any): Observable<void> {
    return this.commonTreeService.move(nodeId, nextNodeId, this.treeClass) as any
  }
  getChildren(node: any): Observable<DynamicFlatNode[]> {
    // return this.http.get<any>(`${this.apiBase}/menu`, { params: { parentId: node.id } }).pipe(map(m => _.sortBy(m.content, 'seq')));
    return this.commonTreeService.children(this.treeClass, {
      page: 0,
      size: 1000,
      sort: ['seq,asc']
    }, node.id).pipe(map((m: any) => _.sortBy(m.content, 'seq'))) as any
  }
  initialData(): Observable<DynamicFlatNode[]> {
    // return this.http.get<any>(`${this.apiBase}/menu`,{}).pipe(map(m=>_.sortBy(m.content,'seq')));
    return this.commonTreeService.children(this.treeClass, {
      page: 0,
      size: 1000,
      sort: ['seq,asc']
    }, undefined).pipe(map((m: any) => _.sortBy(m.content, 'seq'))) as any
  }


}
