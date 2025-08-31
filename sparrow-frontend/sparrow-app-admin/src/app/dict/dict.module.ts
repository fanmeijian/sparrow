import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DictListComponent } from './dict-list/dict-list.component';
import { DictRoutingModule } from './dict-routing.module';
import { SprTreeModule, TREE_SERVICE } from "@sparrowmini/common-ui-nm";
import { DictService } from './dict.service';
import { AngularMaterialModule } from '../angular-material.module';
import { DictFormComponent } from './dict-form/dict-form.component';



@NgModule({
  declarations: [
    DictListComponent,
    DictFormComponent
  ],
  imports: [
    CommonModule,
    DictRoutingModule,
    SprTreeModule,
    AngularMaterialModule,
  ],
  providers:[
    { provide: TREE_SERVICE, useClass: DictService },
  ]
})
export class DictModule { }
