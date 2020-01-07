import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
  selector: 'dv-confirm-download',
  templateUrl: 'confirm-download-dialog.component.html'
})
export class ConfirmDownloadDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<ConfirmDownloadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data) {}

  public download(): void {
    window.open(this.data.downloadUrl);
    this.dialogRef.close();
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}
