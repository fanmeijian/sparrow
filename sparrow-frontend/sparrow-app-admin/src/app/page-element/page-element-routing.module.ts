import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { PageElementListComponent } from "./page-element-list/page-element-list.component";


const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'page-element-list' },
  {
    path: 'page-element-list', component: PageElementListComponent,
    children: [
    ]
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PageElementRoutingModule { }
