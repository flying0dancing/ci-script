import { Component, Inject } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-create-user-dialog",
  templateUrl: "./create-user-dialog.component.html"
})
export class CreateUserDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CreateUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}
}
