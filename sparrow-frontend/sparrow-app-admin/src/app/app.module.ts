import { APP_INITIALIZER, ErrorHandler, InjectionToken, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MenuRoutingModule } from './menu/menu-routing.module';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { environment } from 'src/environments/environment';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTreeModule } from '@angular/material/tree';
import { MenuService } from './menu/menu.service';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { TREE_SERVICE, SprTreeModule, DialogService, AuthModule, AuthService, GlobalErrorHandlerService } from '@sparrowmini/common-ui-nm';
import { CommonApiModule, BASE_PATH as COMMON_API_BASE_PATH, CommonTreeService, PEM_BASE_PATH, PgelPermissionDirective, CommonApiService } from '@sparrowmini/common-api';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonPipeModule } from '@sparrowmini/common';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';

export const BASE_PATH: InjectionToken<string> = new InjectionToken('apiBase')

function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: {
        url: environment.keycloak.authServerUrl,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      },
      initOptions: {
        onLoad: environment.keycloak.login,
      },
      bearerExcludedUrls: ['/assets'],
    }).then(async res => {
      const profile: any = await keycloak.loadUserProfile();
      sessionStorage.setItem('username', profile.username || profile.id);
    });
}

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MenuRoutingModule,
    HttpClientModule,
    KeycloakAngularModule,
    MatToolbarModule,
    MatSidenavModule,
    MatCheckboxModule,
    MatTreeModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    DragDropModule,
    MatDialogModule,
    SprTreeModule,
    CommonPipeModule,
    CommonApiModule,
    AuthModule,
  ],
  providers: [
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
    { provide: BASE_PATH, useValue: environment.apiBase },
    { provide: COMMON_API_BASE_PATH, useValue: environment.apiBase },
    { provide: PEM_BASE_PATH, useValue: environment.pemBase },
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandlerService,
    },
    { provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: { duration: 2500 } },
    [CommonTreeService, DialogService, AuthService, CommonApiService]
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
