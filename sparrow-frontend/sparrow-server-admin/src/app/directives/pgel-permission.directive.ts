import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  Directive,
  ElementRef,
  Input,
  Renderer2,
  ViewContainerRef,
} from '@angular/core';
import { PageElementService } from '@sparrowmini/org-api';
import { BehaviorSubject } from 'rxjs';

export const pagePermissions: BehaviorSubject<Record<string, any>> = new BehaviorSubject({});
@Directive({
  selector: '[appPgelPermission]',
})
export class PgelPermissionDirective implements AfterViewInit {
  elementTypes = ['input', 'select', 'button'];

  constructor(
    private el: ElementRef<any>,
    private renderer: Renderer2,
    private pageElementService: PageElementService,
    private http: HttpClient,
    private viewContainerRef: ViewContainerRef,
  ) {

  }
  ngAfterViewInit(): void {
    const injector: any = this.viewContainerRef.injector;
    const pageId = injector._lView?.[8]?.constructor?.name;
    console.log(pageId)
    this.el.nativeElement.style.display = 'none';
    pagePermissions.subscribe(pglePermission=>{
      const pagePermissions = pglePermission[pageId]
      if(pagePermissions && pagePermissions[this.appPgelPermission]==='DENY'){
        this.el.nativeElement.style.display = 'none';
      }else{
        this.el.nativeElement.style.display = 'block';
      }
    })
    // this.pageElementService
    //   .hasPageElementPermission(this.appPgelPermission!)
    //   .subscribe(
    //     (res) => {
    //       if (res) {
    //         this.el.nativeElement.style.display = 'block';
    //       }
    //     },
    //     (err) => {
    //       this.el.nativeElement.style.display = 'none';
    //     }
    //   );
  }

  @Input() appPgelPermission?: string;
}
