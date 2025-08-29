import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProcessDesignComponent } from './process/process-design/process-design.component';
import { ProcessDesignListComponent } from './process/process-design-list/process-design-list.component';
import { FormDesignComponent } from './form/form-design/form-design.component';
import { FormDesignListComponent } from './form/form-design-list/form-design-list.component';
import { ProcessInstanceListComponent } from './process/process-instance-list/process-instance-list.component';
import { ProcessInstanceComponent } from './process/process-instance/process-instance.component';
import { ProcessContainerListComponent } from './process/process-container-list/process-container-list.component';
import { ProcessDeployedListComponent } from './process/process-deployed-list/process-deployed-list.component';
import { GlobalVariableClass, ProcessGlobalComponent } from './process/process-global/process-global.component';

const routes: Routes = [
  {
    path: 'process-deployed-list',
    component: ProcessDeployedListComponent,
  },
  {
    path: 'process-container-list',
    component: ProcessContainerListComponent,
    children: [
      {
        path: 'process-design-list',
        component: ProcessDesignListComponent,
        children: [
          {
            path: 'view',
            component: ProcessDesignComponent
          },
          {
            path: 'new',
            component: ProcessDesignComponent
          }
        ]
      },
      {
        path: 'global-variable', component: ProcessGlobalComponent
      }
    ]
  },
  {
    path: 'form-design-list',
    component: FormDesignListComponent,
    children: [
      {
        path: 'view',
        component: FormDesignComponent,
      },
      {
        path: 'new',
        component: FormDesignComponent,
      },
      
    ]
  },

  {
    path: 'process-instance-list',
    component: ProcessInstanceListComponent,
    children: [
      {
        path: 'view',
        component: ProcessInstanceComponent,
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
