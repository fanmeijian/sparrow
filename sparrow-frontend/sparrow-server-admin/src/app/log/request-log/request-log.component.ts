import { Component, OnInit } from '@angular/core';
import { AuditlogService } from '@sparrowmini/org-api';

@Component({
  selector: 'app-request-log',
  templateUrl: './request-log.component.html',
  styleUrls: ['./request-log.component.scss'],
})
export class RequestLogComponent implements OnInit {
  logs?: any[] = [];
  pageable: any = { pageSize: 10, pageIndex: 0, length: 0 };

  constructor(private auditLogService: AuditlogService) {}

  ngOnInit(): void {
    this.onPageChange(this.pageable);
  }

  onPageChange(event: any) {
    this.auditLogService
      .getAllRequestLogs(undefined, event.pageIndex, event.pageSize, [
        'createdDate,desc',
      ])
      .subscribe((res) => {
        this.logs = res.content;
        this.pageable.length = res.totalElements;
      });
  }
}
