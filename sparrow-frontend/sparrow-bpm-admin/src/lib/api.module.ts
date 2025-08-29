import { NgModule, ModuleWithProviders, SkipSelf, Optional } from '@angular/core';
import { Configuration } from './configuration';
import { HttpClient } from '@angular/common/http';


import { CaseInstanceAdministrationService } from './api/caseInstanceAdministration.service';
import { CaseInstancesService } from './api/caseInstances.service';
import { CaseQueriesService } from './api/caseQueries.service';
import { CustomQueriesService } from './api/customQueries.service';
import { DMNModelsService } from './api/dMNModels.service';
import { DocumentsService } from './api/documents.service';
import { JobsService } from './api/jobs.service';
import { KIEServerAndKIEContainersService } from './api/kIEServerAndKIEContainers.service';
import { KIESessionAssetsService } from './api/kIESessionAssets.service';
import { ProcessAndTaskDefinitionsService } from './api/processAndTaskDefinitions.service';
import { ProcessAndTaskFormsService } from './api/processAndTaskForms.service';
import { ProcessImagesService } from './api/processImages.service';
import { ProcessInstanceAdministrationService } from './api/processInstanceAdministration.service';
import { ProcessInstancesService } from './api/processInstances.service';
import { ProcessQueriesService } from './api/processQueries.service';
import { StaticFilesEndpointBPMService } from './api/staticFilesEndpointBPM.service';
import { TaskInstanceAdministrationService } from './api/taskInstanceAdministration.service';
import { TaskInstancesService } from './api/taskInstances.service';
import { JbpmExtService } from './api/jbpmExt.service';

@NgModule({
  imports:      [],
  declarations: [],
  exports:      [],
  providers: [
    CaseInstanceAdministrationService,
    CaseInstancesService,
    CaseQueriesService,
    CustomQueriesService,
    DMNModelsService,
    DocumentsService,
    JobsService,
    KIEServerAndKIEContainersService,
    KIESessionAssetsService,
    ProcessAndTaskDefinitionsService,
    ProcessAndTaskFormsService,
    ProcessImagesService,
    ProcessInstanceAdministrationService,
    ProcessInstancesService,
    ProcessQueriesService,
    StaticFilesEndpointBPMService,
    TaskInstanceAdministrationService,
    TaskInstancesService,
    JbpmExtService ]
})
export class ApiModule {
    public static forRoot(configurationFactory: () => Configuration): ModuleWithProviders<ApiModule> {
        return {
            ngModule: ApiModule,
            providers: [ { provide: Configuration, useFactory: configurationFactory } ]
        };
    }

    constructor( @Optional() @SkipSelf() parentModule: ApiModule,
                 @Optional() http: HttpClient) {
        if (parentModule) {
            throw new Error('ApiModule is already loaded. Import in your base AppModule only.');
        }
        if (!http) {
            throw new Error('You need to import the HttpClientModule in your AppModule! \n' +
            'See also https://github.com/angular/angular/issues/20575');
        }
    }
}
