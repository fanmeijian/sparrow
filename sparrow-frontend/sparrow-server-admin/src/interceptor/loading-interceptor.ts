import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { catchError, finalize, Observable, throwError } from 'rxjs';

import { LoadingDialogComponent } from '../app/global/loading-dialog/loading-dialog.component'

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private dialogRef: MatDialogRef<LoadingDialogComponent>
  count: number = 0;

  constructor(
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    if (!request.reportProgress) {
      this.count++;
      if (!this.dialogRef) {
        this.dialogRef = this.dialog.open(LoadingDialogComponent, {
          data: undefined,
          maxHeight: '100%',
          width: '400px',
          maxWidth: '100%',
          disableClose: true,
          hasBackdrop: true,
        });
      }
    }

    return next.handle(request).pipe(
      finalize(() => {
        this.count--;
        if (this.count == 0 && this.dialogRef) {
          this.dialogRef.close()
          this.dialogRef = null!;
        }
      })
    );
  }
}
