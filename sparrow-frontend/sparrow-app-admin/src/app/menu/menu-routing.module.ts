import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { MenuListComponent } from "./menu-list/menu-list.component";
import { MenuFormComponent } from "./menu-form/menu-form.component";

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'menu-list' },
  {
    path: 'menu-list', component: MenuListComponent,
    children: [
      { path: ':id', component: MenuFormComponent },
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MenuRoutingModule { }
