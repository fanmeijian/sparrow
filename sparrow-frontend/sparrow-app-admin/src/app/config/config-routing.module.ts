import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { ConfigListComponent } from "./config-list/config-list.component";


const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'config-list' },
  {
    path: 'config-list', component: ConfigListComponent,
    children: [
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigRoutingModule { }
