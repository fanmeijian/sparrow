import {
  HttpClient,
  HttpEvent,
  HttpEventType,
  HttpResponse,
} from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { FileService } from '@sparrowmini/org-api';
import { last, map, tap } from 'rxjs';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
})
export class FileUploadComponent implements OnInit {
  loaded = 0;
  selectedFiles!: FileList;
  uploadedFiles: any[] = [];
  showProgress = false;

  constructor(
    private http: HttpClient,
    private fileService: FileService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {}

  selectFile(event: any) {
    this.selectedFiles = event.target.files;
  }

  upload() {
    this.showProgress = true;
    this.uploadedFiles = [];
    Array.from(this.selectedFiles).forEach((file) => {
      const fileDetails: any = {};
      fileDetails.name = file.name;
      this.uploadedFiles.push(fileDetails);
      this.fileService
        .uploadFileForm(file, 'events', true)
        .pipe(
          tap((event: any) => {
            if (event.type === HttpEventType.UploadProgress) {
              this.loaded = Math.round((100 * event.loaded) / event.total);
              fileDetails.progress = this.loaded;
            }
          })
          // tap((message) => this.showProgress(message)),
          // last(), // return last (completed) message to caller
          // catchError(this.handleError(file))
        )
        .subscribe((event:any) => {
          if (event instanceof HttpResponse) {
            if (
              this.selectedFiles.item(this.selectedFiles.length - 1) === file
            ) {
              // Invokes fetchFileNames() when last file in the list is uploaded.
            }
          }
        });
    });
  }


  private getEventMessage(event: HttpEvent<any>, file: File) {
    switch (event.type) {
      case HttpEventType.Sent:
        return `Uploading file "${file.name}" of size ${file.size}.`;

      case HttpEventType.UploadProgress:
        // Compute and show the % done:
        const percentDone = event.total
          ? Math.round((100 * event.loaded) / event.total)
          : 0;
        return `File "${file.name}" is ${percentDone}% uploaded.`;

      case HttpEventType.Response:
        return `File "${file.name}" was completely uploaded!`;

      default:
        return `File "${file.name}" surprising upload event: ${event.type}.`;
    }
  }
}
