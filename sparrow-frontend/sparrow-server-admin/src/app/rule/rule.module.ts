import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrlListComponent } from './drl-list/drl-list.component';
import { RuleRoutingModule } from './rule-routing.module';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [
    DrlListComponent
  ],
  imports: [
    CommonModule,
    RuleRoutingModule,
    SharedModule,
  ]
})
export class RuleModule { }
