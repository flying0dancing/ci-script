import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  templateUrl: './info-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InfoDialogComponent {
  message: string;

  constructor(
    public dialogRef: MatDialogRef<InfoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.message = data.message;
  }

  confirm(): void {
    this.dialogRef.close();
  }
}
