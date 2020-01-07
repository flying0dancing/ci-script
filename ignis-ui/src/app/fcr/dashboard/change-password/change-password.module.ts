import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatInputModule } from "@angular/material/input";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { ChangePasswordDialogComponent } from "./change-password-dialog.component";
import { ChangePasswordFormComponent } from "./change-password-form.component";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDialogModule,
    LayoutModule
  ],
  exports: [ChangePasswordFormComponent, ChangePasswordDialogComponent],
  declarations: [ChangePasswordFormComponent, ChangePasswordDialogComponent]
})
export class ChangePasswordFormModule {}
