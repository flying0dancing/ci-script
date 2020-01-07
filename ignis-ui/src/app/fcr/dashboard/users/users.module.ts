import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatListModule } from "@angular/material/list";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { ChangePasswordFormModule } from "../change-password";
import { CreateUserDialogComponent } from "./create-user/create-user-dialog.component";
import { CreateUserFormComponent } from "./create-user/create-user-form.component";
import { UsersDialogComponent } from "./users-dialog.component";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDialogModule,
    MatListModule,
    MatIconModule,
    MatTooltipModule,
    LayoutModule,
    ChangePasswordFormModule,
    LoadersModule
  ],
  exports: [
    UsersDialogComponent,
    CreateUserFormComponent,
    CreateUserDialogComponent
  ],
  declarations: [
    UsersDialogComponent,
    CreateUserFormComponent,
    CreateUserDialogComponent
  ],
  entryComponents: [CreateUserDialogComponent]
})
export class CreateUserFormModule {}
