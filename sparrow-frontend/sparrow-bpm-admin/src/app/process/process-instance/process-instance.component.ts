import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProcessAndTaskDefinitionsService, ProcessInstanceAdministrationService, ProcessInstancesService, ProcessQueriesService, TaskInstanceAdministrationService, TaskInstancesService } from '../../../lib';
import { combineLatest, map, ObservableInput, of, switchMap } from 'rxjs';
import { environment } from 'src/environments/environment';
import _ from 'lodash'
import { MatDialog } from '@angular/material/dialog';
import { ProcessVariableFormComponent } from '../process-variable-form/process-variable-form.component';
import { UsersComponent } from 'src/app/global/users/users.component';

@Component({
  selector: 'app-process-instance',
  templateUrl: './process-instance.component.html',
  styleUrls: ['./process-instance.component.css'],
  providers: [ProcessAndTaskDefinitionsService, TaskInstancesService, TaskInstanceAdministrationService, ProcessInstanceAdministrationService]
})
export class ProcessInstanceComponent implements OnInit {
  deleteAssignment(taskId: number, reassignmentId: any) {
    this.taskInstanceAdminService.cancelReassignment(this.process.externalId, taskId, reassignmentId).subscribe();
  }
  onChange($event: any) {
    throw new Error('Method not implemented.');
  }
  editorOptions: any;
  code: any;

  taskPotOwners: any = {}
  taskAssignments: any[] = []
  activeTasksByWorkItemId: any = {}

  updateVaraible() {
    this.dialog.open(ProcessVariableFormComponent, {
      width: '100%',
      data: {
        processInstanceId: this.processInstanceId,
        variables: this.processVariables,
        containerId: this.process.externalId
      }
    });
  }

  displayedColumns = ['id', 'name', 'actions']
  retrigger(nodeInstance: any) {
    const $retrigger = this.processInstancesAdminService.retriggerNodeInstance(nodeInstance.externalId, nodeInstance.processInstanceId, nodeInstance.nodeInstanceId);
    const $cancelNodes = this.cancelNode(nodeInstance)
    combineLatest({ $cancelNodes, $retrigger }).subscribe((res) => {

    })
  }
  skip(_t10: any) {
    throw new Error('Method not implemented.');
  }
  delegate(_t10: any) {
    throw new Error('Method not implemented.');
  }
  forward(_t10: any) {
    throw new Error('Method not implemented.');
  }
  activeNodes: any[] = []
  process: any = {};
  nodeInstances: any[] = []
  processInstanceId?: any
  processNodes: any[] = []
  processDefinition: any = {}
  variableHistory: any[] = []
  processVariables: Record<string, any> = {}
  /**
   * 任务只有在Created状态下才允许nominate
   * @param nodeInstancetask-id

   */
  nominate(nodeInstance: any) {
    this.processService.getTaskByWorkItemId(nodeInstance['work-item-id']).subscribe((res: any) => {
      this.taskInstanceService.nominate(this.process.externalId, res['task-id'], ['yunying']).subscribe()
    })
  }

  addPotOwners(taskId: number) {
    this.dialog.open(UsersComponent).afterClosed().subscribe((res: any) => {
      console.log(res)
      if (res) {
        const potOwners = JSON.stringify(res)
        const containerId = this.process.externalId
        this.taskInstanceAdminService.addPotentialOwners(containerId, taskId, potOwners, undefined, false).subscribe()
      }
    })
    // const potOwners = JSON.stringify({
    //   "users": ['jully'],
    //   "groups": ['HQ_GM']
    // })
    // const containerId = this.process.externalId
    // this.taskInstanceAdminService.addPotentialOwners(containerId, taskId, potOwners, undefined, false).subscribe()

  }

  removePotOwners(taskId: number, potOwnerId: any) {
    this.taskInstanceAdminService.removePotentialOwnersGroups(this.process.externalId, taskId, potOwnerId).subscribe()
    this.taskInstanceAdminService.removeExcludedOwnersUsers(this.process.externalId, taskId, potOwnerId).subscribe()
  }

  getReassignments(nodeInstance: any) {
    const containerId = this.process.externalId
    this.processService.getTaskByWorkItemId(nodeInstance['work-item-id']).subscribe((res: any) => {
      const taskInstanceId = res['task-id']
      this.taskInstanceAdminService.getTaskReassignments(containerId, taskInstanceId).subscribe()
    })
  }

  /**
   * 当任务为Reserved 和InProgress 才可以
   */
  reassign(nodeInstance: any) {
    const containerId = this.process.externalId
    this.processService.getTaskByWorkItemId(nodeInstance['work-item-id']).subscribe((res: any) => {
      const taskInstanceId = res['task-id']
      const potOwners = JSON.stringify({
        "users": ['jully'],
        "groups": ['HQ_GM']
      })
      this.taskInstanceAdminService.reassign(containerId, taskInstanceId, "2h", potOwners, true).subscribe()

    })
  }

  canAbort(node: any) {
    return !node['node-completed']
  }

  canTrigger(node: any) {
    const found = this.activeNodes.find(a => node['unique-id'] === a['node-id'])
    return this.process.status === 1 && !found
  }

  canTriggerNodeType = ['HumanTaskNode']
  triggerNode(node: any) {
    const containerId = this.process.externalId
    const processInstanceId = this.processInstanceId
    const nodeInstanceId = node.nodeInstanceId
    const $ativeNodes = this.processInstancesAdminService.getActiveNodeInstances(containerId, processInstanceId)
    $ativeNodes.pipe(
      switchMap((s: any) => {
        const activeNodeIds = s['node-instance'].map((m: any) => m['node-instance-id'])
        const $cancelNodes: any[] = []
        activeNodeIds.forEach((a: any) => {
          $cancelNodes.push(this.processInstancesAdminService.cancelNodeInstance(containerId, processInstanceId, a))
        })
        return activeNodeIds.length > 0 ? combineLatest($cancelNodes) : of([])
      }),
      // switchMap(m => this.processInstancesAdminService.triggerNode(containerId, processInstanceId, nodeInstanceId))
    ).subscribe()
    const $triggerNode = this.processInstancesAdminService.triggerNode(containerId, processInstanceId, node.id)
    combineLatest({ $ativeNodes, $triggerNode }).subscribe((res) => {

    })
  }

  cancelNode(nodeInstance: any) {
    const containerId = this.process.externalId
    const processInstanceId = this.processInstanceId
    const nodeInstanceId = nodeInstance.nodeInstanceId
    const $ativeNodes = this.processInstancesAdminService.getActiveNodeInstances(containerId, processInstanceId)
    $ativeNodes.pipe(
      switchMap((s: any) => {
        const activeNodeIds = s['node-instance'].map((m: any) => m['node-instance-id'])
        const $cancelNodes: any[] = []
        activeNodeIds.forEach((a: any) => {
          $cancelNodes.push(this.processInstancesAdminService.cancelNodeInstance(containerId, processInstanceId, a))
        })
        return activeNodeIds.length > 0 ? combineLatest($cancelNodes) : of([])
      }),
    )
    return $ativeNodes
  }

  abortNode(node: any) {
    const nodeInstanceId = node['node-instance-id']
    this.processInstancesAdminService.cancelNodeInstance(this.process.externalId, this.processInstanceId, nodeInstanceId).subscribe(() => {

    })
  }

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private processQueryService: ProcessQueriesService,
    private processInstancesAdminService: ProcessInstanceAdministrationService,
    private processInstanceService: ProcessInstancesService,
    private processService: ProcessQueriesService,
    private processDefinitionService: ProcessAndTaskDefinitionsService,
    private taskInstanceService: TaskInstancesService,
    private taskInstanceAdminService: TaskInstanceAdministrationService,
    private dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    const processInstanceId: any = this.route.snapshot.queryParamMap.get("id")
    this.processInstanceId = processInstanceId


    this.http.get(`${environment.bpmApi}/process-instances/${processInstanceId}`).subscribe((process: any) => {
      this.process = process
      const containerId = process.externalId
      this.http.get(`${environment.bpmApi}/jbpm-ext/process-variables`, {
        params: {
          "processInstanceId": processInstanceId,
          "processId": this.process['processId'],
          "deploymentId": containerId
        }
      }).subscribe((res) => {
        this.processVariables = res
      })
      // this.processInstancesAdminService.getNodes(process.externalId, process.processInstanceId).subscribe((res: any) => {
      //   this.processNodes = res['process-node']

      // })

      this.processDefinitionService.getProcessDefinition(containerId, process.processId).subscribe((res: any) => {
        this.processNodes = res['nodes']
        this.processDefinition = res
        // const $varaibleRequest = Object.keys(this.processDefinition.processVariables).map(m => this.processService.getVariableHistory1(processInstanceId, m).pipe(
        //   map((v: any) => v['variable-instance'])
        // ))
        // combineLatest($varaibleRequest).subscribe((r: any[]) => {
        //   this.processVariables = _.groupBy(r.flat(), 'name')
        //   console.log(this.processVariables)
        // })
      })

      this.processInstancesAdminService.getActiveNodeInstances(containerId, processInstanceId).subscribe((res: any) => {
        this.activeNodes = res['node-instance']
        this.activeNodes.forEach(n => {
          const containerId = this.process.externalId
          this.processService.getTaskByWorkItemId(n['work-item-id']).subscribe((res: any) => {
            // this.activeTasks[n['work-item-id']] = res
            const taskId = res['task-id']

            this.taskInstanceService.getTask(containerId, taskId, false, false, true).subscribe(task => {
              this.activeTasksByWorkItemId[n['work-item-id']] = task
            })

            //初始化各项任务的可分配人列表
            this.taskInstanceAdminService.getTaskReassignments(containerId, taskId).subscribe((res: any) => {
              this.taskAssignments[n['work-item-id']] = res['task-reassignment']
            })


            this.processQueryService.getTasksAssignedAsPotentialOwner(['Created', 'Ready', 'Reserved', 'InProgress', 'Suspended', 'Failed', 'Error', 'Obsolete']).subscribe(res => {

            })


          })


        })
      })

      const httpParams = new HttpParams({ fromObject: { page: 0, size: 1000, length: 0 } })
      // const processInstanceId = process?.processInstanceId
      this.http.get(`${environment.bpmApi}/process-instances/${processInstanceId}/node-instances`, { params: httpParams }).subscribe((res: any) => {
        const nodeInstances = res.content
        nodeInstances.forEach((node: any) => {
          if (!nodeInstances.find((f: any) => (f.type == 1 || f.type == 2) && f.nodeInstanceId == node.nodeInstanceId)) {
            node.active = true
          }
        })
        this.nodeInstances = nodeInstances
      })
    })
  }

  getKeys(o: any) {
    return Object.keys(o)
  }
}


