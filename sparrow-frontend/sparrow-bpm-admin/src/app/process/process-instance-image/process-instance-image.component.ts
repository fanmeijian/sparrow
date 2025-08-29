import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-instance-image',
  templateUrl: './process-instance-image.component.html',
  styleUrls: ['./process-instance-image.component.css']
})
export class ProcessInstanceImageComponent implements OnInit, OnChanges {

  @Input() processInstanceId?: number

  svgXml?: string;
  url: any;

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
  ) { }
  ngOnChanges(changes: SimpleChanges): void {
    if (this.processInstanceId) {
      const headers = new HttpHeaders({ 'Content-Type': 'application/svg+xml' })
      this.http.get(`${environment.bpmApi}/rest/server/containers/{containerId}/images/processes/instances/${this.processInstanceId}`, { headers: headers, responseType: 'text' }).subscribe(res => {
        this.svgXml = res
        const blob = new Blob([res], { type: 'image/svg+xml' });
        const url = URL.createObjectURL(blob);
        this.url = this.sanitizer.bypassSecurityTrustUrl(url)

      })
    };
  }

  ngOnInit(): void {


  }

}
