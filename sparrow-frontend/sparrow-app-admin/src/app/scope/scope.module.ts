import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScopeListComponent } from './scope-list/scope-list.component';
import { ScopeFormComponent } from './scope-form/scope-form.component';
import { ScopePermissionComponent } from './scope-permission/scope-permission.component';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { ScopeRoutingModule } from './scope-routing.module';
import { MatButtonModule } from '@angular/material/button';
import { AngularMaterialModule } from '../angular-material.module';
import { AuthModule } from '@sparrowmini/common-ui-nm';



@NgModule({
  declarations: [
    ScopeListComponent,
    ScopeFormComponent,
    ScopePermissionComponent
  ],
  imports: [
    CommonModule,
    ScopeRoutingModule,
    MatSnackBarModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatPaginatorModule,
    MatButtonModule,
    AngularMaterialModule,
    AuthModule
  ]
})
export class ScopeModule { }
