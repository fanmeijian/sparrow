import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-dsl-preview',
  templateUrl: './dsl-preview.component.html',
  styleUrls: ['./dsl-preview.component.css']
})
export class DslPreviewComponent implements OnInit {
  constructor(
    @Inject(MAT_DIALOG_DATA) public dsl: any,
    private http: HttpClient,
  ) { }
  ngOnInit(): void {
    this.getDrl();
  }
  editorOptions = { language: 'drools', theme: 'vs-dark' };
  code: string = ''

  getDrl() {
    const dslId = this.dsl.id
    this.http.get(`${environment.apiBase}/dsls/${dslId}/to-drl`).subscribe((res:any)=>{
      this.code = res.drl
    });
  }
}
