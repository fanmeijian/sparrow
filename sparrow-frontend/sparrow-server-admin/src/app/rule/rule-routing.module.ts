import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { DrlListComponent } from "./drl-list/drl-list.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'templates'
  },
  { path: 'templates', component: DrlListComponent }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RuleRoutingModule { }
