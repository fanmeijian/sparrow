import { APP_INITIALIZER, ErrorHandler, LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { environment } from 'src/environments/environment';
import { AngularMaterialModule } from './angular-material.module';
import { LoadingDialogComponent } from './global/loading-dialog/loading-dialog.component';
import { ErrorDialogComponent } from './global/error-dialog/error-dialog.component';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { LoadingInterceptor } from 'src/interceptor/loading-interceptor';
import { GlobalErrorHandlerService } from './service/global-error-handler.service';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { HttpClient } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import zh from '@angular/common/locales/zh';
import { AuditlogService, DatamodelService, DataPermissionService, DictService, EmployeeService, FileService, MenuService, BASE_PATH as ORG_BASE_PATH, OrgApiModule, PageElementService, PemgroupService, ScopeService, SysconfigService, SysroleService, UserService } from '@sparrowmini/org-api';
import { UserCreateComponent } from './user/user-create/user-create.component';
import { UserListComponent } from './user/user-list/user-list.component';
import { UserPasswordResetComponent } from './user/user-password-reset/user-password-reset.component';
import { UserSelectionComponent } from './user/user-selection/user-selection.component';
import { SearchFilterComponent } from './common/search-filter/search-filter.component';
import { SysrolesComponent } from './sysrole/sysroles/sysroles.component';
import { SysroleSelectionComponent } from './sysrole/sysrole-selection/sysrole-selection.component';
import { SysrolePermissionComponent } from './sysrole/sysrole-permission/sysrole-permission.component';
import { SysroleCreateComponent } from './sysrole/sysrole-create/sysrole-create.component';
import { BaseOpLogColumnComponent } from './common/base-op-log-column/base-op-log-column.component';
import { EntityLogComponent } from './common/entity-log/entity-log.component';
import { DataPermissionGrantComponent } from './data-permission/data-permission-grant/data-permission-grant.component';
import { DataPermissionNewComponent } from './data-permission/data-permission-new/data-permission-new.component';
import { DataPermissionViewComponent } from './data-permission/data-permission-view/data-permission-view.component';
import { DataPermissionsComponent } from './data-permission/data-permissions/data-permissions.component';
import { SprmodesComponent } from './sprmodel/sprmodes/sprmodes.component';
import { SprmodelPermisssionComponent } from './sprmodel/sprmodel-permisssion/sprmodel-permisssion.component';
import { ModelPermissionRulesComponent } from './sprmodel/model-permission-rules/model-permission-rules.component';
import { ModelPermissionViewComponent } from './sprmodel/model-permission-view/model-permission-view.component';
import { AttributePermisssionComponent } from './sprmodel/attribute-permisssion/attribute-permisssion.component';
import { FilterTreeComponent } from './common/filter-tree/filter-tree.component';
import { ScopesComponent } from './scope/scopes/scopes.component';
import { ScopeCreateComponent } from './scope/scope-create/scope-create.component';
import { ScopePermissionComponent } from './scope/scope-permission/scope-permission.component';
import { SysconfigDesignComponent } from './sysconfig/sysconfig-design/sysconfig-design.component';
import { SysconfigFormComponent } from './sysconfig/sysconfig-form/sysconfig-form.component';
import { SysconfigListComponent } from './sysconfig/sysconfig-list/sysconfig-list.component';
import { PageElementCreateComponent } from './page-element/page-element-create/page-element-create.component';
import { PageElementPermissionComponent } from './page-element/page-element-permission/page-element-permission.component';
import { PageElementsComponent } from './page-element/page-elements/page-elements.component';
import { DeleteLogComponent } from './log/delete-log/delete-log.component';
import { EditLogComponent } from './log/edit-log/edit-log.component';
import { LoginLogComponent } from './log/login-log/login-log.component';
import { RequestLogComponent } from './log/request-log/request-log.component';
import { MenuCreateComponent } from './menu/menu-create/menu-create.component';
import { MenuPermissionComponent } from './menu/menu-permission/menu-permission.component';
import { MenuTreeComponent } from './menu/menu-tree/menu-tree.component';
import { MenuTreeSelectionComponent } from './menu/menu-tree-selection/menu-tree-selection.component';
import { FilesComponent } from './file/files/files.component';
import { FileUploadComponent } from './file/file-upload/file-upload.component';
import { DictsComponent } from './dict/dicts/dicts.component';
import { DictCatalogCreateComponent } from './dict/dict-catalog-create/dict-catalog-create.component';
import { DictCatalogSelectionComponent } from './dict/dict-catalog-selection/dict-catalog-selection.component';
import { DictCreateComponent } from './dict/dict-create/dict-create.component';
import { DictFormComponent } from './dict/dict-form/dict-form.component';
import { DictSelectionComponent } from './dict/dict-selection/dict-selection.component';
import { DynamicTreeViewComponent } from './common/dynamic-tree-view/dynamic-tree-view.component';
import { TreeViewComponent } from './common/tree-view/tree-view.component';
import { PemgroupCreateComponent } from './pemgroup/pemgroup-create/pemgroup-create.component';
import { PemgroupMemberComponent } from './pemgroup/pemgroup-member/pemgroup-member.component';
import { PemgroupsComponent } from './pemgroup/pemgroups/pemgroups.component';
import { MenuComponent } from './menu/menu/menu.component';
import { L2routeComponent } from './l2route/l2route.component';
import { PgelPermissionDirective } from './directives/pgel-permission.directive';
import { SprappListComponent } from './sprapp/sprapp-list/sprapp-list.component';
import { JsonPathPipe } from 'src/pipe/json-path.pipe';
import { MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS as MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/legacy-snack-bar';


@NgModule({
  declarations: [
    AppComponent,
    JsonPathPipe,
    LoadingDialogComponent,
    ErrorDialogComponent,
    BaseOpLogColumnComponent,
    EntityLogComponent,
    SearchFilterComponent,
    FilterTreeComponent,
    DynamicTreeViewComponent,
    TreeViewComponent,
    UserListComponent,
    UserCreateComponent,
    UserSelectionComponent,
    UserPasswordResetComponent,
    SysrolesComponent,
    SysroleSelectionComponent,
    SysrolePermissionComponent,
    SysroleCreateComponent,
    SprmodesComponent,
    SprmodelPermisssionComponent,
    ModelPermissionRulesComponent,
    ModelPermissionViewComponent,
    AttributePermisssionComponent,
    DataPermissionGrantComponent,
    DataPermissionNewComponent,
    DataPermissionViewComponent,
    DataPermissionsComponent,
    ScopesComponent,
    ScopeCreateComponent,
    ScopePermissionComponent,
    SysconfigDesignComponent,
    SysconfigFormComponent,
    SysconfigListComponent,
    PageElementCreateComponent,
    PageElementPermissionComponent,
    PageElementsComponent,
    DeleteLogComponent,
    EditLogComponent,
    LoginLogComponent,
    RequestLogComponent,
    MenuCreateComponent,
    MenuPermissionComponent,
    MenuTreeComponent,
    MenuTreeSelectionComponent,
    MenuComponent,
    FilesComponent,
    FileUploadComponent,
    DictsComponent,
    DictCatalogCreateComponent,
    DictCatalogSelectionComponent,
    DictCreateComponent,
    DictFormComponent,
    DictSelectionComponent,
    DictsComponent,
    PemgroupCreateComponent,
    PemgroupMemberComponent,
    PemgroupsComponent,
    L2routeComponent,
    PgelPermissionDirective,
    SprappListComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    KeycloakAngularModule,
    AngularMaterialModule,
    HttpClientModule,
    OrgApiModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (http: HttpClient) => new TranslateHttpLoader(http, './assets/', '.json'),
        deps: [HttpClient]
      }
    }),
  ],
  providers: [
    [
      MenuService,
      SysconfigService,
      AuditlogService,
      SysroleService,
      EmployeeService,
      UserService,
      PemgroupService,
      ScopeService,
      DatamodelService,
      DataPermissionService,
      FileService,
      PageElementService,
      DictService
    ],
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
    { provide: ORG_BASE_PATH, useValue: environment.apiBase },
    { provide: LOCALE_ID, useValue: 'zh' },
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoadingInterceptor,
      multi: true,
    },
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandlerService,
    },
    { provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: { duration: 2500 } },

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }


function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: {
        url: environment.keycloak.authServerUrl,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      },
      initOptions: {
        onLoad: 'login-required',
      },
      bearerExcludedUrls: ['/assets'],
    });
}

registerLocaleData(zh);
