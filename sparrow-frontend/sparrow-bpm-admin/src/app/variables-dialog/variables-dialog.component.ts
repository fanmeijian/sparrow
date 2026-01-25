import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProcessInstancesService } from 'src/lib';

@Component({
  selector: 'app-variables-dialog',
  templateUrl: './variables-dialog.component.html',
  styleUrls: ['./variables-dialog.component.scss']
})
export class VariablesDialogComponent {
name!: string;
  save() {
    // console.log(this.name);
    // this.processInstanceService.setProcessVariable(this.data.containerId,this.data.processInstanceId,this.name ,JSON.parse(this.code))
    // .subscribe(res => {});
    this.dialogRef.close({name: this.name, value: JSON.parse(this.code)});
  }
  onChange($event: any) {
    console.log($event);
    this.code = JSON.stringify(this.data[$event], null, 2);
    this.name = $event;
    console.log(this.code);
  }
  variableNames: any[] = []
  editorOptions = { language: 'json', theme: 'vs-dark' };
  code: string = `{}`;
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Record<string, any>,
    private processInstanceService: ProcessInstancesService,
    private dialogRef: MatDialogRef<VariablesDialogComponent>
  ) { }

  ngOnInit(): void {
    this.variableNames = Object.keys(this.data)
  }
}
