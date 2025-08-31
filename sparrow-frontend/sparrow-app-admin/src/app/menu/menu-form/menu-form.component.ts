import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CommonApiService, CommonTreeService } from '@sparrowmini/common-api';
import { MenuClass } from '../menu.constant';
import { SysroleMenuClass, UserMenuClass } from '../menu-list/menu-list.component';

@Component({
  selector: 'app-menu-form',
  templateUrl: './menu-form.component.html',
  styleUrls: ['./menu-form.component.css']
})
export class MenuFormComponent implements OnInit {
  removeSysrole(sysroleMenu: any) {
    this.commonApiService.delete(SysroleMenuClass,[sysroleMenu.id]).subscribe(()=>{
      this.ngOnInit()
    });
  }
  removeUser(userMenu: any) {
    console.log(userMenu)
    this.commonApiService.delete(UserMenuClass,[userMenu.id])
    .subscribe(()=>{
      this.ngOnInit()
    });
  }
  onTreeSelect($event: any[]) {
    this.formGroup.patchValue({ parentId: $event[0] });
  }
  treeNode: any
  submit() {
    this.commonTreeService.upsert(MenuClass, [this.formGroup.value]).subscribe();
  }

  userMenus: any[] = []
  sysroleMenus: any[] = []

  formGroup: UntypedFormGroup = new FormGroup({
    name: new FormControl(null, Validators.required),
    code: new FormControl(null, Validators.required),
    icon: new FormControl(),
    url: new FormControl(null, Validators.required),
    target: new FormControl(),
    type: new FormControl(null, Validators.required),
    queryParams: new FormControl(),
    parentId: new FormControl(),
    id: new FormControl(),
  });

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private commonTreeService: CommonTreeService,
    private commonApiService: CommonApiService,
  ) { }
  ngOnInit(): void {
    this.route.params.subscribe((params: any) => {
      const id = params.id
      if (id && id !== 'new') {
        this.formGroup.disable()
        this.commonTreeService.get(MenuClass, id).subscribe(res => {
          this.formGroup.patchValue(res)
          this.treeNode = res
        });
        //获取权限
        this.commonApiService.filter(UserMenuClass,undefined,`id.menuId='${id}'`).subscribe(res => {
          this.userMenus = res.content
        })
        this.commonApiService.filter(SysroleMenuClass, undefined,`id.menuId='${id}'`).subscribe(res => {
          this.sysroleMenus = res.content
        })
      } else {
        this.formGroup.enable()
        this.formGroup.reset()
      }
    });

  }
}
