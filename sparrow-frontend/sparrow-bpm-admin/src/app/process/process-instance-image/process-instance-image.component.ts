import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'app-process-instance-image',
    templateUrl: './process-instance-image.component.html',
    styleUrls: ['./process-instance-image.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ProcessInstanceImageComponent implements OnChanges, OnDestroy {

  @Input() processInstanceId?: number
  @Input() containerId?: string

  svgXml?: string;
  url?: SafeUrl;
  private objectUrl?: string;
  private imageRequest?: Subscription;

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private changeDetectorRef: ChangeDetectorRef,
  ) { }
  ngOnChanges(): void {
    this.imageRequest?.unsubscribe();
    this.releaseImage();
    if (this.containerId && this.processInstanceId) {
      const headers = new HttpHeaders({ Accept: 'application/svg+xml' })
      this.imageRequest = this.http.get(`${environment.bpmApi}/rest/server/containers/${encodeURIComponent(this.containerId)}/images/processes/instances/${this.processInstanceId}`, { headers, responseType: 'text' }).subscribe(res => {
        this.svgXml = res
        const blob = new Blob([res], { type: 'image/svg+xml' });
        this.objectUrl = URL.createObjectURL(blob);
        this.url = this.sanitizer.bypassSecurityTrustUrl(this.objectUrl)
        this.changeDetectorRef.markForCheck()

      })
    };
  }

  ngOnDestroy(): void {
    this.imageRequest?.unsubscribe();
    this.releaseImage();
  }

  private releaseImage(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
    }
    this.objectUrl = undefined;
    this.url = undefined;
    this.svgXml = undefined;
  }
}
