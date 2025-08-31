import { SelectionModel } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonApiService, CommonTreeService } from '@sparrowmini/common-api';
import { DynamicFlatNode, PermissionSelectionComponent, TREE_SERVICE, TreeDataSource, TreeService } from '@sparrowmini/common-ui-nm';
import { MenuClass } from '../menu.constant';
import { MatDialog } from '@angular/material/dialog';
import * as _ from 'lodash';
import { forkJoin } from 'rxjs';
export const UserMenuClass = 'cn.sparrowmini.common.model.pem.UserMenu'
export const SysroleMenuClass = 'cn.sparrowmini.common.model.pem.SysroleMenu'

@Component({
  selector: 'app-menu-list',
  templateUrl: './menu-list.component.html',
  styleUrls: ['./menu-list.component.css']
})
export class MenuListComponent {
  grantPermission() {
    this.dialog.open(PermissionSelectionComponent, { width: '80%', height: '600px' })
      .afterClosed()
      .subscribe(res => {
        console.log(this.checklistSelection.selected)
        const body = Array.from(this.checklistSelection.selected)

        const userMenus = _.flatMap(res.usernames, username =>
          body.map(menuId => ({ id: { username, menuId } }))
        );

        const sysroleMenus = _.flatMap(res.sysroleIds, sysroleId =>
          body.map(menuId => ({ id: { sysroleId, menuId } }))
        );

        const userMenu$ = this.commonApiService.upsert(UserMenuClass, userMenus)
        const sysroleMenu$ = this.commonApiService.upsert(SysroleMenuClass, sysroleMenus)

        forkJoin([userMenu$, sysroleMenu$]).subscribe()
      })

  }
  onNodeClick($event: any) {
    this.router.navigate([$event.id], { relativeTo: this.route });
  }
  onTreeSelect($event: any[]) {
    console.log($event);
    this.checklistSelection.clear()
    if ($event.length > 0) {
      this.checklistSelection.select(...$event)
    }
  }
  new() {
    throw new Error('Method not implemented.');
  }
  delete() {
    const body = this.checklistSelection.selected
    this.commonTreeService.delete(MenuClass, body).subscribe();
  }
  checklistSelection = new SelectionModel<any>(
    true /* multiple */
  );

  constructor(
    private commonTreeService: CommonTreeService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private commonApiService: CommonApiService,
  ) { }
}
