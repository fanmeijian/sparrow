import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  tabs = []
  selectedApp: any;
  openApp(app: any) {
    this.selectedApp=app
    this.selectedTabIndex = this.appList.findIndex(f => f === app);
    console.log(app)
    this.tabs.push(app)
    this.route.navigate(['app',app.id])
  }
  appList: any[] = []
  selectedTabIndex = 0;
  selectedAppIndex=0
  selectedTab: any;
  constructor(
    private translate: TranslateService,
    private http: HttpClient,
    private route: Router,
    private keycloak: KeycloakService,
  ) {
    translate.setDefaultLang('zh-CN');
  }
  ngOnInit(): void {
    const className = 'cn.sparrowmini.permission.app.App'
    this.http.post(`${environment.apiBase}/common-jpa-controller/${className}/filter`, []).subscribe((res: any) => {
      this.appList = res.content
    });

    this.keycloak.loadUserProfile(true).then(user=>{
      const name = (user.lastName + user.firstName)||user.username
      const username = user.username
      sessionStorage.setItem('username',username)
      sessionStorage.setItem('name', name)
    })
  }

  switchLang(lang: string) {
    this.translate.use(lang);
  }
}
