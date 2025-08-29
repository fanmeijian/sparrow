import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { environment } from 'src/environments/environment';
import { EntiyListModule, MenuModule } from '@sparrowmini/common-ui-nm';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { AngularMaterialModule } from './angular-material.module';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { DrlListComponent } from './drl-list/drl-list.component';
import { HttpClientModule } from '@angular/common/http';
import { DrlFormComponent } from './drl-form/drl-form.component';
import { FormsModule } from '@angular/forms';
import { MonacoEditorModule, NgxMonacoEditorConfig } from 'ngx-monaco-editor-v2';
import { RuleTemplateListComponent } from './rule-template/rule-template-list/rule-template-list.component';
import { RuleTemplateFormComponent } from './rule-template/rule-template-form/rule-template-form.component';
import { RuleFormComponent } from './rule-template/rule-form/rule-form.component';
import { RuleListComponent } from './rule-template/rule-list/rule-list.component';
import { RuleTemplateModule } from './rule-template/rule-template.module';
import { monacoConfig } from './config/monaco-config';
import { BASE_PATH as COMMON_API_BASE } from '@sparrowmini/common-api';



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

@NgModule({
  declarations: [
    AppComponent,
    DrlListComponent,
    DrlFormComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    EntiyListModule,
    KeycloakAngularModule,
    BrowserAnimationsModule,
    AngularMaterialModule,
    CommonModule,
    MatPaginatorModule,
    MatTableModule,
    MatButtonModule,
    MenuModule,
    FormsModule,
    MonacoEditorModule.forRoot(monacoConfig)
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
    {provide: COMMON_API_BASE, useValue: environment.ruleApi}
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }


