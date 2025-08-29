import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EntityListComponent } from './entity-list/entity-list.component';
import { MatLegacyPaginatorModule as MatPaginatorModule } from "@angular/material/legacy-paginator";
import { MatLegacyTableModule as MatTableModule } from '@angular/material/legacy-table';



@NgModule({
  declarations: [
    EntityListComponent
  ],
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatTableModule,
  ],
  exports: [
    EntityListComponent
  ]
})
export class SharedModule { }
