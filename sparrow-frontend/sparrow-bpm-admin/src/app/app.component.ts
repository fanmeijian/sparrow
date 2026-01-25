import { Component, EventEmitter, Inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';
import { ThemeService } from './service/theme.service';
import { StyleService } from './service/style.service';
import { MatRadioChange } from '@angular/material/radio';
import { BPM_API_TOKEN, BpmApi } from '@sparrowmini/bpm-api';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  onchane($event: MatRadioChange) {
    const theme = $event.value
    this.themeService.setThemeColor(theme)
  }
  isOpen = false;
  sessionStorage = sessionStorage
  seasons = [
    { name: '蓝色', code: 'theme-indigo' },
    { name: '粉色', code: 'theme-pink' },
  ]
  logout() {
    this.keycloak.logout();
  }
  title = 'sparrow-app-admin';

  constructor(
    private keycloak: KeycloakService,
    private translate: TranslateService,
    public themeService: ThemeService,
    private styleService: StyleService,
    @Inject(BPM_API_TOKEN) private bpmApi: BpmApi
  ) {
    translate.setDefaultLang('zh-CN');
    themeService.setThemeColor('theme-indigo')
    // console.log('bpm api', this.bpmApi);
    // this.bpmApi.todoTasks().subscribe(res => {
    //   console.log('todo tasks', res);
    // })
  }
  get isDark() {
    return this.themeService.isDarkMode;
  }

  switchLang(lang: string) {
    this.translate.use(lang);
  }
}
