import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { DrlTemplateListComponent } from "./drl-template-list/drl-template-list.component";
import { DrlTemplateFormComponent } from "./drl-template-form/drl-template-form.component";
import { DrlTemplateRuleListComponent } from "./drl-template-rule-list/drl-template-rule-list.component";
import { DrlTemplateRuleFormComponent } from "./drl-template-rule-form/drl-template-rule-form.component";


const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'drl-template-list'
  },
  { path: 'drl-template-list', component: DrlTemplateListComponent },
  { path: 'drl-template-form', component: DrlTemplateFormComponent },
  { path: 'drl-template-rule-list', component: DrlTemplateRuleListComponent },
  { path: 'drl-template-rule-form', component: DrlTemplateRuleFormComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DrlTemplateRoutingModule { }
