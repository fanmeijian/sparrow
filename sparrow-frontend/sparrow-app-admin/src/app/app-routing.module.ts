import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MenuListComponent } from './menu/menu-list/menu-list.component';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'menu' },
  { path: 'menu', loadChildren: () => import('./menu/menu.module').then(m => m.MenuModule) },
  { path: 'scope', loadChildren: () => import('./scope/scope.module').then(m => m.ScopeModule) },
  { path: 'page', loadChildren: () => import('./page-element/page-element.module').then(m => m.PageElementModule) },
  { path: 'model', loadChildren: () => import('./model/model.module').then(m => m.ModelModule) },
  { path: 'dict', loadChildren: () => import('./dict/dict.module').then(m => m.DictModule) },
  { path: 'config', loadChildren: () => import('./config/config.module').then(m => m.ConfigModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
