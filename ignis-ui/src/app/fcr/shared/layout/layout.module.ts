import { ChangePasswordDialogComponent } from "@/fcr/dashboard/change-password/change-password-dialog.component";
import { CreateUserDialogComponent } from "@/fcr/dashboard/users/create-user/create-user-dialog.component";
import { UsersDialogComponent } from "@/fcr/dashboard/users/users-dialog.component";
import { CalendarConfigModule } from "@/fcr/shared/calendar/calendar-config.module";
import { CalendarDialogComponent } from "@/fcr/shared/calendar/calendar-dialog.component";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatToolbarModule } from "@angular/material/toolbar";
import { RouterModule } from "@angular/router";
import { ContainerComponent } from "./container.component";
import { HeaderNavigationComponent } from "./header-navigation.component";

@NgModule({
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatToolbarModule,
    MatSnackBarModule,
    MatDividerModule,
    CalendarConfigModule,
    RouterModule
  ],
  exports: [HeaderNavigationComponent, ContainerComponent],
  declarations: [HeaderNavigationComponent, ContainerComponent],
  entryComponents: [
    UsersDialogComponent,
    CreateUserDialogComponent,
    ChangePasswordDialogComponent,
    CalendarDialogComponent
  ]
})
export class LayoutModule {}
