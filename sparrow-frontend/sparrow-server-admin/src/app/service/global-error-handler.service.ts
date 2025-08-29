import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { ErrorDialogComponent } from '../global/error-dialog/error-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class GlobalErrorHandlerService {
  dialogRef?: MatDialogRef<ErrorDialogComponent>
  constructor(
    private zone: NgZone,
    private dialog: MatDialog
  ) { }
  handleError(error: any): void {

    console.error('Error from global error handler', error);

    this.zone.run(() => {
      // Check if it's an error from an HTTP response
      if (error instanceof HttpErrorResponse) {
        const message = ((typeof error.error) === 'object' ? error.error?.message : error.error) || 'Undefined client error<br />' + error?.message
       this.dialogRef = this.dialog.open(ErrorDialogComponent, {
          data: { message, status },
          maxHeight: '100%',
          width: '540px',
          maxWidth: '100%',
          disableClose: true,
          hasBackdrop: true,
        });
      }
    });
  }
}
