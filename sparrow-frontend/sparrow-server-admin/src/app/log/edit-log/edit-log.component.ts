import { Component, OnInit } from '@angular/core';
import { AuditlogService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-edit-log',
  templateUrl: './edit-log.component.html',
  styleUrls: ['./edit-log.component.scss'],
})
export class EditLogComponent implements OnInit {
  logs?: any[] = [];
  pageable: any = { pageSize: 10, pageIndex: 0, length: 0 };

  id: any = '';
  modelId: string = 'cn.sparrowmini.pem.model.Sysrole';

  constructor(private auditLogService: AuditlogService) {}

  ngOnInit(): void {
    this.onPageChange(this.pageable);
  }

  onPageChange(event: any) {
    this.auditLogService
      .logs(this.modelId, this.id, event.pageIndex, event.pageSize, [
        'createdDate,desc',
      ])
      .subscribe((res) => {
        this.logs = res.content;
        this.pageable.length = res.totalElements;
      });
  }
}
