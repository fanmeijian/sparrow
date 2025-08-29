import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RuleFormComponent } from './rule-form/rule-form.component';
import { RuleListComponent } from './rule-list/rule-list.component';
import { RuleTemplateFormComponent } from './rule-template-form/rule-template-form.component';
import { RuleTemplateListComponent } from './rule-template-list/rule-template-list.component';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { monacoConfig } from '../config/monaco-config';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EntiyListModule, MenuModule } from '@sparrowmini/common-ui-nm';
import { KeycloakAngularModule } from 'keycloak-angular';
import { AngularMaterialModule } from '../angular-material.module';
import { AppRoutingModule } from '../app-routing.module';
import { RuleTemplateRoutingModule } from './rule-template-routing';



@NgModule({
  declarations: [
    RuleTemplateListComponent,
    RuleTemplateFormComponent,
    RuleFormComponent,
    RuleListComponent,

  ],
  imports: [
    RuleTemplateRoutingModule,
    HttpClientModule,
    EntiyListModule,
    KeycloakAngularModule,
    AngularMaterialModule,
    CommonModule,
    MatPaginatorModule,
    MatTableModule,
    MatButtonModule,
    MenuModule,
    FormsModule,
    MonacoEditorModule,
  ]
})
export class RuleTemplateModule { }
