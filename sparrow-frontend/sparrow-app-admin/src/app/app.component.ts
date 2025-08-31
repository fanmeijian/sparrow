import { Component, OnInit } from '@angular/core';
import { ComponentRegistryService } from '@sparrowmini/common-api';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  sessionStorage = sessionStorage
  logout() {
    this.keycloak.logout();
  }
  title = 'sparrow-app-admin';

  constructor(
    private keycloak: KeycloakService,
    private registry: ComponentRegistryService
  ){}

  ngOnInit(): void {
    const usages = this.registry.getDirectiveUsages('appPgelPermission');
    console.log('所有 appPgelPermission 使用:', usages);
  }
}
