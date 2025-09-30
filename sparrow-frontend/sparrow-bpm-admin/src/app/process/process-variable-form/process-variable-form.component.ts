import { ProcessInstancesService } from './../../../lib/api/processInstances.service';
import { Component, Inject, Input } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-process-variable-form',
  templateUrl: './process-variable-form.component.html',
  styleUrls: ['./process-variable-form.component.css']
})
export class ProcessVariableFormComponent {
  name!: string;
  save() {
    console.log(this.name);
    this.processInstanceService.setProcessVariable(this.data.containerId,this.data.processInstanceId,this.name ,JSON.parse(this.code))
    .subscribe(res => {});
  }
  onChange($event: any) {
    console.log($event);
    this.code = JSON.stringify(this.data.variables[$event], null, 2);
    this.name = $event;
    console.log(this.code);
  }
  variableNames: any[] = []
  editorOptions = { language: 'json', theme: 'vs-dark' };
  code: string = `{}`;
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {containerId: string ,processInstanceId: number, variables: Record<string, any> },
    private processInstanceService: ProcessInstancesService,
  ) { }

  ngOnInit(): void {
    this.variableNames = Object.keys(this.data.variables)
  }
}
