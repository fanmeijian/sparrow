import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { ModelListComponent } from "./model-list/model-list.component";


const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'model-list' },
  {
    path: 'model-list', component: ModelListComponent,
    children: [
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModelRoutingModule { }
