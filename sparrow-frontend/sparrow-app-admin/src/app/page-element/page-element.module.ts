import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageElementListComponent } from './page-element-list/page-element-list.component';
import { PageElementRoutingModule } from './page-element-routing.module';
import { AngularMaterialModule } from '../angular-material.module';
import { PageElementFormComponent } from './page-element-form/page-element-form.component';
import { CommonApiModule } from '@sparrowmini/common-api';



@NgModule({
  declarations: [
    PageElementListComponent,
    PageElementFormComponent,
  ],
  imports: [
    CommonModule,
    PageElementRoutingModule,
    AngularMaterialModule,
    CommonApiModule,

  ]
})
export class PageElementModule { }
