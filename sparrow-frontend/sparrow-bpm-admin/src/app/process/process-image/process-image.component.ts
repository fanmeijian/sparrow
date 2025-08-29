import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, Inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-process-image',
  templateUrl: './process-image.component.html',
  styleUrls: ['./process-image.component.css']
})
export class ProcessImageComponent implements OnInit {

  svgXml?: string;
  url: any;

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    @Inject(MAT_DIALOG_DATA) private process: { containerId: string, processId: string }
  ) { }


  ngOnInit(): void {
    const headers = new HttpHeaders({ 'Content-Type': 'application/svg+xml' })
    this.http.get(`${environment.bpmApi}/rest/server/containers/${this.process.containerId}/images/processes/${this.process.processId}`, { headers: headers, responseType: 'text' }).subscribe(res => {
      this.svgXml = res
      const blob = new Blob([res], { type: 'image/svg+xml' });
      const url = URL.createObjectURL(blob);
      this.url = this.sanitizer.bypassSecurityTrustUrl(url)

    })
  }

}
