import { Injectable, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class StyleService {
  constructor(@Inject(DOCUMENT) private document: Document) {}

  switchTheme(themeName: string) {
    const headElement = this.document.getElementsByTagName('head')[0];
    const existingLinkElement = this.document.getElementById('client-theme') as HTMLLinkElement;

    if (existingLinkElement) {
      // 这里的 href 必须对应 angular.json 里的 bundleName
      existingLinkElement.href = `${themeName}.css`;
    } else {
      const newLinkElement = this.document.createElement('link');
      newLinkElement.id = 'client-theme';
      newLinkElement.rel = 'stylesheet';
      newLinkElement.href = `${themeName}.css`;
      headElement.appendChild(newLinkElement);
    }
  }
}