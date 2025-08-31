import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigListComponent } from './config-list/config-list.component';
import { ConfigRoutingModule } from './config-routing.module';
import { AngularMaterialModule } from '../angular-material.module';



@NgModule({
  declarations: [
    ConfigListComponent
  ],
  imports: [
    CommonModule,
    ConfigRoutingModule,
    AngularMaterialModule,
  ]
})
export class ConfigModule { }
