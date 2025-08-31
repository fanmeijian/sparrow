import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModelListComponent } from './model-list/model-list.component';
import { ModelRoutingModule } from './model-routing.module';
import { AngularMaterialModule } from '../angular-material.module';
import { ModelPermissionComponent } from './model-permission/model-permission.component';
import { AuthModule } from '@sparrowmini/common-ui-nm';



@NgModule({
  declarations: [
    ModelListComponent,
    ModelPermissionComponent
  ],
  imports: [
    CommonModule,
    ModelRoutingModule,
    AngularMaterialModule,
    AuthModule,
  ]
})
export class ModelModule { }
