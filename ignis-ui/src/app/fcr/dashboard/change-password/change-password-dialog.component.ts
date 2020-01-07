import { ChangePasswordFormComponent } from "@/fcr/dashboard/change-password/change-password-form.component";
import { Component, Inject, OnDestroy, ViewChild } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-change-password-dialog",
  templateUrl: "./change-password-dialog.component.html"
})
export class ChangePasswordDialogComponent implements OnDestroy {
  @ViewChild("changePasswordForm", { static: true })
  changePasswordForm: ChangePasswordFormComponent;

  constructor(
    public dialogRef: MatDialogRef<ChangePasswordDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnDestroy(): void {
    this.changePasswordForm.clearErrors();
  }
}
