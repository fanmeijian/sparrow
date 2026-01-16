import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private renderer: Renderer2;
  private currentSelection = 'theme-indigo';
  private isDark = false;

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);
  }

  // 切换色系
  setThemeColor(colorClass: string) {
    this.renderer.removeClass(document.body, this.currentSelection);
    this.currentSelection = colorClass;
    this.renderer.addClass(document.body, this.currentSelection);
  }

 get isDarkMode(){
    return this.isDark
  }

  get theme(){
    return this.currentSelection
  }
  
  // 切换亮暗
  toggleDarkMode() {
    this.isDark = !this.isDark;
    if (this.isDark) {
      this.renderer.addClass(document.body, 'dark-mode');
    } else {
      this.renderer.removeClass(document.body, 'dark-mode');
    }
  }
}