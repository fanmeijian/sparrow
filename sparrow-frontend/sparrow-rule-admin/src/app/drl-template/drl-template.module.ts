import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrlTemplateListComponent } from './drl-template-list/drl-template-list.component';
import { DrlTemplateRoutingModule } from './drl-template-routing';
import { EntiyListModule } from "@sparrowmini/common-ui-nm";
import { AngularMaterialModule } from "src/app/angular-material.module";
import { MatButtonModule } from '@angular/material/button';
import { DrlTemplateFormComponent } from './drl-template-form/drl-template-form.component';
import { MonacoEditorModule } from "ngx-monaco-editor-v2";
import { DrlTemplateRuleListComponent } from './drl-template-rule-list/drl-template-rule-list.component';
import { DrlTemplateRuleFormComponent } from './drl-template-rule-form/drl-template-rule-form.component';



@NgModule({
  declarations: [
    DrlTemplateListComponent,
    DrlTemplateFormComponent,
    DrlTemplateRuleListComponent,
    DrlTemplateRuleFormComponent
  ],
  imports: [
    CommonModule,
    DrlTemplateRoutingModule,
    EntiyListModule,
    AngularMaterialModule,
    MatButtonModule,
    MonacoEditorModule
]
})
export class DrlTemplateModule { }
