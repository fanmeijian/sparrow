import { Component } from '@angular/core';
import { TREE_SERVICE } from '@sparrowmini/common-ui-nm';
import { MenuTreeService } from './services/menu-tree.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [{ provide: TREE_SERVICE, useClass: MenuTreeService }]
})
export class AppComponent {
  title = 'sparrow-rule-admin';
  sessionStorage = sessionStorage
  logout() {
    this.keycloak.logout();
  }

  constructor(
    private keycloak: KeycloakService,
  ) {
  }



}
