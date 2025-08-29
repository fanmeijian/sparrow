import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, finalize, Observable, throwError } from 'rxjs';

import { LoadingDialogComponent } from '../app/global/loading-dialog/loading-dialog.component'

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private dialogRef: MatDialogRef<LoadingDialogComponent>[] = []

  constructor(
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // if(this.dialogRef){
    //   this.dialogRef.close()
    // }
    if (!request.reportProgress) {
      const dialogRef = this.dialog.open(LoadingDialogComponent, {
        data: undefined,
        maxHeight: '100%',
        width: '400px',
        maxWidth: '100%',
        disableClose: true,
        hasBackdrop: true,
      });
      this.dialogRef?.push(dialogRef)

    }

    return next.handle(request).pipe(
      finalize(() => {

        const dialogRef1 = this.dialogRef[0]
        this.dialogRef = this.dialogRef.filter(f => f.id != dialogRef1.id)
        if (dialogRef1) {
          dialogRef1.close(dialogRef1.id)

        }
      })
    );
  }
}
