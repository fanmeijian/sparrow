import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "src/environments/environment";

@Injectable({
  providedIn: 'root'
})
export class RuleService {

  apiBase = environment.ruleApi + '/rules'
  constructor(
    private http: HttpClient,
  ) { }

  listDrl(pageable: any, filter: string) {
    return this.http.get(`${this.apiBase}/drl`)
  }

  saveDrl(drls: any[]) {
    return this.http.post(`${this.apiBase}/drl`, drls)
  }

  deleteDrl(drlIds: string[]) {
    return this.http.delete(`${this.apiBase}/drl`, { params: { drlId: drlIds } })
  }

  getDrl(id: string){
    return this.http.get(`${this.apiBase}/drl/${id}`)
  }
}
