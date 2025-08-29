import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import {
  AuditlogService,
  EmployeeService,
  RestApiServiceService,
} from '@sparrowmini/org-api';
import { map, switchMap, zip } from 'rxjs';
import { diff } from 'json-diff-ts';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-entity-log',
  templateUrl: './entity-log.component.html',
  styleUrls: ['./entity-log.component.scss'],
})
export class EntityLogComponent implements OnInit, OnChanges {
  @Input() id!: string;
  @Input() modelId!: string;

  opLogs: any[] | undefined = [];

  constructor(
    private auditLogService: AuditlogService,
    private employeeService: EmployeeService,
    private dialog: MatDialog,
  ) {}
  ngOnChanges(changes: SimpleChanges): void {
    // console.log('changes', changes);


  }

  ngOnInit(): void {}

  diffEntity(oldData:any|undefined, newData:any|undefined){
    const diffs = diff(oldData, newData);
    return diffs
  }


  @ViewChild("dialogTempl") dialogTempl!: TemplateRef<any>
  openDialog(){
    if(this.modelId){
      this.auditLogService
      .logs(this.modelId, this.id,0,10000)
      .pipe(
        map((res) =>
          res.content?.map((m) =>
            Object.assign({}, m[0], { entity: m[0],timestamp: m[1].timestamp, type: m[2] })
          )
        ),
        switchMap((res: any) =>
          zip(
            ...res.map((m: any) =>
              this.employeeService
                .employeeByUsername(m.modifiedBy?m.modifiedBy:'')
                .pipe(map((a) => Object.assign({}, m, { username: a?.name })))
            )
          )
        )
      )
      .subscribe((res) => {
        this.opLogs?.push(...res);
      });
    }
    this.dialog.open(this.dialogTempl,{width:'80%'})
  }
}
