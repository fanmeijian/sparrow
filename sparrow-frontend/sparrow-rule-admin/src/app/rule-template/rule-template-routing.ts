import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { DrlFormComponent } from "../drl-form/drl-form.component";
import { DrlListComponent } from "../drl-list/drl-list.component";
import { RuleTemplateFormComponent } from "./rule-template-form/rule-template-form.component";
import { RuleTemplateListComponent } from "./rule-template-list/rule-template-list.component";
import { RuleFormComponent } from "./rule-form/rule-form.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'drl-list'
  },
  { path: 'templates', component: RuleTemplateListComponent },
  { path: 'template-form', component: RuleTemplateFormComponent },
  { path: 'rule-form', component: RuleFormComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RuleTemplateRoutingModule { }
