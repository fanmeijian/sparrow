import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { ScopeListComponent } from "./scope-list/scope-list.component";
import { ScopeFormComponent } from "./scope-form/scope-form.component";


const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'scope-list'},
  {
    path: 'scope-list', component: ScopeListComponent,
    children: [
      {path: ':id', component: ScopeFormComponent}
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ScopeRoutingModule { }
