import { Component, Input, OnInit } from '@angular/core';
export const baseOpLogColumns = ['created','modifited']

@Component({
  selector: 'app-base-op-log-column',
  templateUrl: './base-op-log-column.component.html',
  styleUrls: ['./base-op-log-column.component.scss']
})
export class BaseOpLogColumnComponent implements OnInit {

  @Input() element: any = {}

  constructor() { }

  ngOnInit(): void {
  }

}
