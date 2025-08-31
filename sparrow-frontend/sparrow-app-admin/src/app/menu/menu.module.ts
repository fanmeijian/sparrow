import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatTreeModule } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { MenuRoutingModule } from './menu-routing.module';
import { MenuListComponent } from './menu-list/menu-list.component';
import { SprTreeModule, TREE_SERVICE } from '@sparrowmini/common-ui-nm';
import { MatMenuModule } from '@angular/material/menu';
import { MenuFormComponent } from './menu-form/menu-form.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MenuService } from './menu.service';
import { CommonApiModule } from "@sparrowmini/common-api";
import { MatChipsModule } from "@angular/material/chips";

@NgModule({
  declarations: [
    MenuListComponent,
    MenuFormComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    MenuRoutingModule,
    MatCheckboxModule,
    MatTreeModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    SprTreeModule,
    MatMenuModule,
    MatDialogModule,
    MatCheckboxModule,
    MatRadioModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatGridListModule,
    MatCardModule,
    CommonApiModule,
    MatChipsModule
],
  providers: [
    { provide: TREE_SERVICE, useClass: MenuService },
  ]
})
export class MenuModule { }
