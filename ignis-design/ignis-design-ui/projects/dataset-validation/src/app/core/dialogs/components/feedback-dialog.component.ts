import { Component } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { ErrorDialogComponent } from './error-dialog.component';
import { FeedbackDialogService } from './feedback-dialog.service';
import { InfoDialogComponent } from './info-dialog.component';

@Component({
  selector: 'dv-feedback-dialog',
  templateUrl: './feedback-dialog.component.html',
  styleUrls: ['./feedback-dialog.component.scss']
})
export class FeedbackDialogComponent {
  textAreaValue = '';
  loading = false;
  success = false;

  constructor(
    public dialogRef: MatDialogRef<FeedbackDialogComponent>,
    private service: FeedbackDialogService,
    private dialog: MatDialog
  ) {
    dialogRef.disableClose = true;
  }

  submitFeedback(): void {
    this.loading = true;
    this.service
      .post(this.textAreaValue)
      .subscribe(
        val => this.successfulFeedbackRequest(),
        error => this.errorFeedbackRequest(),
        () => this.dialogRef.close()
      );
  }

  private successfulFeedbackRequest() {
    this.openInfoDialog('Thank you for your feedback');
    this.loading = false;
    this.success = true;
  }

  private errorFeedbackRequest() {
    this.openErrorDialog('We were unable to submit your feedback');
    this.loading = false;
    this.success = false;
  }

  cancel(): void {
    if (this.textAreaValue !== '') {
      this.openConfirmDialog();
    } else {
      this.dialogRef.close();
    }
  }

  private openConfirmDialog(): void {
    let confirmDialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {
        title: 'Are you sure?',
        message: "Your feedback won't be submitted",
        confirmMessage: 'Yes'
      }
    });

    confirmDialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dialogRef.close();
      }
      confirmDialogRef = undefined;
    });
  }

  private openErrorDialog(message: string): void {
    let errorDialogRef = this.dialog.open(ErrorDialogComponent, {
      data: { message: message }
    });

    errorDialogRef.afterClosed().subscribe(() => {
      errorDialogRef = undefined;
    });
  }

  private openInfoDialog(message: string): void {
    let infoDialogRef = this.dialog.open(InfoDialogComponent, {
      data: { message: message }
    });

    infoDialogRef.afterClosed().subscribe(() => {
      infoDialogRef = undefined;
    });
  }
}
