import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'dv-error-dialog',
  templateUrl: './error-dialog.component.html'
})
export class ErrorDialogComponent {
  message: string;

  constructor(
    public dialogRef: MatDialogRef<ErrorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.message = data.message;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
