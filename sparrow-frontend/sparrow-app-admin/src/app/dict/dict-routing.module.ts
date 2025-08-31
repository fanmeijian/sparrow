import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { DictListComponent } from "./dict-list/dict-list.component";
import { DictFormComponent } from "./dict-form/dict-form.component";


const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dict-list' },
  {
    path: 'dict-list', component: DictListComponent,
    children: [
      { path: ':id', component: DictFormComponent },
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DictRoutingModule { }
