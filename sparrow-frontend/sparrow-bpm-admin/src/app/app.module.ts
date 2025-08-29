import { APP_INITIALIZER, ErrorHandler, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { environment } from 'src/environments/environment';
import { AngularMaterialModule } from './angular-material.module';
import { LoadingDialogComponent } from './global/loading-dialog/loading-dialog.component';
import { ErrorDialogComponent } from './global/error-dialog/error-dialog.component';
import { ProcessDesignComponent } from './process/process-design/process-design.component';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClient, HttpClientModule } from '@angular/common/http';
import { ProcessDesignListComponent } from './process/process-design-list/process-design-list.component';
import { GlobalErrorHandlerService } from './service/global-error-handler.service';
import { LoadingInterceptor } from 'src/interceptor/loading-interceptor';
import { FormDesignComponent } from './form/form-design/form-design.component';
import { FormlyModule } from '@ngx-formly/core';
import { FormlyMaterialModule } from '@ngx-formly/material';
import { ReactiveFormsModule } from '@angular/forms';
import { FormDesignListComponent } from './form/form-design-list/form-design-list.component';
import { ProcessSelectionComponent } from './process/process-selection/process-selection.component';
import { TaskSelectionComponent } from './task/task-selection/task-selection.component';
import { ProcessInstanceListComponent } from './process/process-instance-list/process-instance-list.component';
import { BASE_PATH as BPM_BASE_PATH, JbpmApiModule, ProcessInstanceAdministrationService, ProcessInstancesService, ProcessQueriesService } from '@sparrowmini/jbpm-api';
import { ProcessInstanceComponent } from './process/process-instance/process-instance.component';
import { ProcessImageComponent } from './process/process-image/process-image.component';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ProcessContainerListComponent } from './process/process-container-list/process-container-list.component';
import { ProcessDeployedListComponent } from './process/process-deployed-list/process-deployed-list.component';
import { ProcessInstanceImageComponent } from './process/process-instance-image/process-instance-image.component';
import { GridsterModule } from 'angular-gridster2';
import { GridsterTypeComponent } from './form/form-design/formly-gridster-type';
import { GristerTestComponent } from './form/grister-test/grister-test.component';
import { FormPreviewComponent } from './form/form-preview/form-preview.component';
import { MonacoEditorModule } from 'ngx-monaco-editor-v2';
import { GristerFieldTypeComponent } from './form/grister-field-type/grister-field-type.component';

@NgModule({
  declarations: [
    AppComponent,
    LoadingDialogComponent,
    ErrorDialogComponent,
    ProcessDesignComponent,
    ProcessDesignListComponent,
    FormDesignComponent,
    FormDesignListComponent,
    ProcessSelectionComponent,
    TaskSelectionComponent,
    ProcessInstanceListComponent,
    ProcessInstanceComponent,
    ProcessImageComponent,
    ProcessContainerListComponent,
    ProcessDeployedListComponent,
    ProcessInstanceImageComponent,
    GridsterTypeComponent,
    GristerTestComponent,
    FormPreviewComponent,
    GristerFieldTypeComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    KeycloakAngularModule,
    AngularMaterialModule,
    HttpClientModule,
    MonacoEditorModule.forRoot(),
    FormlyModule.forRoot({
      types: [
        { name: 'gridster', component: GristerFieldTypeComponent }
      ]
    }),
    // FormlyModule.forRoot(),
    FormlyMaterialModule,
    ReactiveFormsModule,
    JbpmApiModule,
    GridsterModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (http: HttpClient) => new TranslateHttpLoader(http, './assets/', '.json'),
        deps: [HttpClient]
      }
    }),
  ],
  providers: [
    ProcessQueriesService, ProcessInstancesService, ProcessInstanceAdministrationService,
    { provide: BPM_BASE_PATH, useValue: environment.bpmApi + '/rest' },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
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
