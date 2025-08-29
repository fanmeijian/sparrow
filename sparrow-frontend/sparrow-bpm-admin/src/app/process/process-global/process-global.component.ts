import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService } from '@sparrowmini/common-api';

export const GlobalVariableClass = 'cn.sparrowmini.bpm.server.process.model.GlobalVariable'

@Component({
  selector: 'app-process-global',
  templateUrl: './process-global.component.html',
  styleUrls: ['./process-global.component.css']
})
export class ProcessGlobalComponent implements OnInit {

  constructor(
    private commonApiService: CommonApiService,
    private route: ActivatedRoute,
  ) { }
  ngOnInit(): void {
    this.commonApiService.filter(GlobalVariableClass).subscribe(res => {
      this.dataSource = new MatTableDataSource(res.content)
    });
  }
  add() {
    this.selected = { code: 'code', value: 'value' }
    const data: any[] = this.dataSource.data
    data.unshift(this.selected)
    this.dataSource = new MatTableDataSource(data);
    // this.dataSource.push(this.selected)
  }

  selected?: any

  delete(_t33: any) {
    throw new Error('Method not implemented.');
  }
  save(_t33: any) {
    this.route.queryParams.subscribe((params: any) => {
      console.log(params)
      const body = [{ ..._t33, containerId: [params.artifactId,params.groupId,params.version].join('-') }]
      this.commonApiService.upsert(GlobalVariableClass, body).subscribe()
    })

  }
  edit(element: any) {
    this.selected = element;
  }
  dataSource: any = new MatTableDataSource([]);
  displayedColumns = ['code', 'value', 'action'];

}
