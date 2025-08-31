import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService } from '@sparrowmini/common-api';
import { environment } from 'src/environments/environment';
import { DslrClass } from '../dslr-list/dslr-list.component';


@Component({
  selector: 'app-dslr-form',
  templateUrl: './dslr-form.component.html',
  styleUrls: ['./dslr-form.component.css']
})
export class DslrFormComponent implements OnInit {

  dslr: any ={}

  submit() {
    console.log(this.dslr);
    this.commonApiService.upsert(DslrClass,[this.dslr]).subscribe()
  }
  conditions: any[] = []
  actions: any [] = []
  form: any = {};

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private commonApiService: CommonApiService
  ) { }
  ngOnInit(): void {
    const dslId = this.data.id
    this.dslr.dslId = dslId
    this.http.get(`${environment.apiBase}/dsls/${dslId}/conditions`).subscribe((res: any) => {
      this.conditions = res.filter((f: any)=>f.section=='CONDITION')
      this.actions = res.filter((f: any)=>f.section=='CONSEQUENCE')
    });
  }

  getVariables(condition: any) {
    if (condition)
      return Object.keys(condition.variables)
    else
      return []
  }
  
  evalCondition(condition: any){
    if(!condition?.mappingKey) return
    let text: string = condition.mappingKey
    Object.keys(this.form).forEach(v=>{
      text = text.replace(`{${v}}`,this.form[v])
    })
    this.dslr.name = text
    return text
  }

    evalAction(action: any){
    if(!action?.mappingKey) return
    let text: string = action.mappingKey
    Object.keys(this.form).forEach(v=>{
      text = text.replace(`{${v}}`,this.form[v])
    })
    this.dslr.content = text
    return text
  }
}
