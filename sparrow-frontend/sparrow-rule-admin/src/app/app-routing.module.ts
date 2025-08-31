import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DrlListComponent } from './drl-list/drl-list.component';
import { DrlFormComponent } from './drl-form/drl-form.component';
import { RuleTemplateListComponent } from './rule-template/rule-template-list/rule-template-list.component';
import { RuleTemplateFormComponent } from './rule-template/rule-template-form/rule-template-form.component';
import { DslListComponent } from './dsl/dsl-list/dsl-list.component';
import { DslFormComponent } from './dsl/dsl-form/dsl-form.component';

const routes: Routes = [
  { path: 'dsl-list', component: DslListComponent },
  { path: 'dsl-form', component: DslFormComponent },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'drl-list'
  },
  { path: 'drl-list', component: DrlListComponent },
  { path: 'drl-form', component: DrlFormComponent },
  { path: 'rule', loadChildren: () => import('./rule-template/rule-template.module').then(m => m.RuleTemplateModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
