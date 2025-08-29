import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  sessionStorage = sessionStorage
  logout() {
    this.keycloak.logout();
  }
  title = 'sparrow-app-admin';

  constructor(
    private keycloak: KeycloakService,
    private translate: TranslateService
  ){
    translate.setDefaultLang('zh-CN');
  }

  switchLang(lang: string) {
    this.translate.use(lang);
  }
}
