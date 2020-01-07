import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'dv-rule-help',
  templateUrl: 'rule-help-dialog.component.html',
  styleUrls: ['rule-help-dialog.component.scss']
})
export class RuleHelpDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<RuleHelpDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {}

  close(): void {
    this.dialogRef.close();
  }
}
