import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { DataPermissionNewComponent } from './data-permission/data-permission-new/data-permission-new.component';
import { DataPermissionsComponent } from './data-permission/data-permissions/data-permissions.component';
import { DictFormComponent } from './dict/dict-form/dict-form.component';
import { DictsComponent } from './dict/dicts/dicts.component';
import { FilesComponent } from './file/files/files.component';
import { DeleteLogComponent } from './log/delete-log/delete-log.component';
import { EditLogComponent } from './log/edit-log/edit-log.component';
import { RequestLogComponent } from './log/request-log/request-log.component';
import { PageElementsComponent } from './page-element/page-elements/page-elements.component';
import { PemgroupsComponent } from './pemgroup/pemgroups/pemgroups.component';
import { ScopesComponent } from './scope/scopes/scopes.component';
import { SprmodesComponent } from './sprmodel/sprmodes/sprmodes.component';
import { SysconfigDesignComponent } from './sysconfig/sysconfig-design/sysconfig-design.component';
import { SysconfigListComponent } from './sysconfig/sysconfig-list/sysconfig-list.component';
import { SysrolesComponent } from './sysrole/sysroles/sysroles.component';
import { UserListComponent } from './user/user-list/user-list.component';
import { MenuComponent } from './menu/menu/menu.component';
import { L2routeComponent } from './l2route/l2route.component';
const routes: Routes = [
  { path: 'rule', loadChildren: () => import('./rule/rule.module').then(m => m.RuleModule) },
  {
    path: 'permission',
    data: { title: '权限管理' },
    component: L2routeComponent,
    children: [

      {
        path: 'menu',
        data: { title: '菜单管理' },
        component: MenuComponent,
      },
      {
        path: 'sysrole',
        data: { title: '角色管理' },
        component: SysrolesComponent,
      },
      {
        path: 'scope',
        data: { title: '功能管理' },
        component: ScopesComponent,
      },
      {
        path: 'model',
        data: { title: '模型管理' },
        component: SprmodesComponent,
      },
      {
        path: 'pemgroup',
        data: { title: '群组管理' },
        component: PemgroupsComponent,
      },
      {
        path: 'data-permissions',
        data: { title: '数据权限' },
        component: DataPermissionsComponent,
      },
      {
        path: 'data-permission-new',
        data: { title: '新增数据权限' },
        component: DataPermissionNewComponent,
      },
      {
        path: 'users',
        data: { title: '用户管理' },
        component: UserListComponent,
      },
      {
        path: 'files',
        data: { title: '文件管理' },
        component: FilesComponent,
      },
      {
        path: 'page-elements',
        data: { title: '页面元素管理' },
        component: PageElementsComponent,
      },
    ],
  },

  {
    path: 'log',
    data: { title: '审计日志' },
    component: L2routeComponent,
    children: [
      {
        path: 'request-logs',
        data: { title: '请求日志' },
        component: RequestLogComponent,
      },
      {
        path: 'delete-logs',
        data: { title: '删除日志' },
        component: DeleteLogComponent,
      },
      {
        path: 'edit-logs',
        data: { title: '更新日志' },
        component: EditLogComponent,
      },
    ],
  },
  {
    path: 'setting',
    data: { title: '系统配置' },
    component: L2routeComponent,
    children: [
      {
        path: 'dicts',
        data: { title: '数据字典' },
        component: DictsComponent,
        children: [
          {
            path: ':id',
            component: DictFormComponent
          },
          {
            path: 'dict-form/new',
            component: DictFormComponent
          }
        ]
      },
      {
        path: 'sysconfig',
        data: { title: '配置文件' },
        component: SysconfigListComponent,
      },
      {
        path: 'sysconfig-design/:code',
        data: { title: '配置表单设计' },
        component: SysconfigDesignComponent,
      },
      {
        path: 'sysconfig-design',
        data: { title: '配置表单设计' },
        component: SysconfigDesignComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
