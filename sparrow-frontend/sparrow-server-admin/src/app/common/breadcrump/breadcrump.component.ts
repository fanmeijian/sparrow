import { E } from '@angular/cdk/keycodes';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Router, RoutesRecognized, ActivatedRouteSnapshot, UrlSegment } from '@angular/router';

@Component({
  selector: 'app-breadcrump',
  templateUrl: './breadcrump.component.html',
  styleUrls: ['./breadcrump.component.scss']
})
export class BreadcrumpComponent implements OnInit, OnChanges {
  @Input() public color = 'primary'

  breadCrump: any[] = [];
  constructor(private router: Router) {
    this.router.events.subscribe((event) => {
      if (event instanceof RoutesRecognized) {
        this.breadCrump = []
        this.toBreadcrumb(event.state['_root']);
        localStorage.setItem('_breadCrump', JSON.stringify(this.breadCrump));
      }
    });

  }
  ngOnChanges(changes: SimpleChanges): void {
    this.breadCrump = JSON.parse(localStorage.getItem('_breadCrump') || '[]');
  }

  toBreadcrumb(node: any) {
    if (node && node.value && node.value.data && node.value.url[0]?.path) {
      this.breadCrump.push({ label: node.value.data['title'], url: this.getUrl(node.value.url[0]?.path) });
    }
    if (node && node.children) {
      this.toBreadcrumb(node.children[0]);
    }
  }

  getUrl(path: string) {
    if (this.breadCrump?.length > 0) {
      return this.breadCrump[this.breadCrump.length - 1].url + '/' + path;
    } else {
      return '/' + path;
    }
  }

  ngOnInit(): void { }

}
