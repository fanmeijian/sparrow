import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService } from '@sparrowmini/common-api';
import { environment } from 'src/environments/environment';
import { DslrClass } from '../dslr-list/dslr-list.component';

interface Condition {
  mappingKey?: string;
  mappingValue?: string;
  keyPattern?: string;
  variables?: Record<string, any>;
}

@Component({
  selector: 'app-dslr-form',
  templateUrl: './dslr-form.component.html',
  styleUrls: ['./dslr-form.component.css']
})
export class DslrFormComponent implements OnInit {

  dslr: any = {}

  submit() {
    console.log(this.dslr);
    this.commonApiService.upsert(DslrClass, [this.dslr]).subscribe()
  }
  conditions: any[] = []
  actions: any[] = []
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
      this.conditions = res.filter((f: any) => f.section == 'CONDITION')
      this.actions = res.filter((f: any) => f.section == 'CONSEQUENCE')
    });
  }

  getVariables(condition: any) {
    if (condition)
      return Object.keys(condition.variables)
    else
      return []
  }

  // evalCondition(condition: any){
  //   if(!condition?.mappingKey) return
  //   let text: string = condition.mappingKey
  //   Object.keys(this.form).forEach(v=>{
  //     text = text.replace(`{${v}}`,this.form[v])
  //   })
  //   this.dslr.name = text
  //   return text
  // }

  evalAction(action: any) {
    if (!action?.mappingKey) return
    let text: string = action.mappingKey
    Object.keys(this.form).forEach(v => {
      text = text.replace(`{${v}}`, this.form[v])
    })
    this.dslr.content = text
    return text
  }

  /**
  * 根据 condition 和用户输入生成 DSLR 名称
  * @param condition DSL/DSLR 条件对象
  * @param inputText 用户输入文本（例如 "- age > 18"）
  */
  evalCondition(condition: Condition) {
    if (!condition || !condition.variables || !condition.mappingKey) return;
    const userInput = this.form
    if (!userInput) return
    // 1️⃣ 先使用 mappingKey 替换占位符
    let text = condition.mappingKey;
    const sortedKeys = Object.keys(condition.variables)
      .sort((a, b) => condition.variables![a] - condition.variables![b]);
    sortedKeys.forEach((varName) => {
      const userValue = userInput[varName] ?? '';
      console.log(varName, userValue);

      // 匹配 {变量} 或 {变量:任意内容}
      const pattern = new RegExp(`\\{${varName}(?::[^}]*)?\\}`, 'g');
      text = text.replace(pattern, userValue);
    });


    console.log(text)
    // 2️⃣ 如果 keyPattern 存在，可以做正则匹配校验（可选）
    if (condition.keyPattern) {
      const regex = new RegExp(condition.keyPattern);
      const match = regex.exec(text);
      // if (!match && ) {
      //   console.warn(`输入文本不匹配 keyPattern: ${text}`);
      // }
      // 这里只做校验，不替换 mappingValue
    }

    this.dslr.name = text;
    return text; // 返回 DSL/DSLR 可读文本
  }

}
