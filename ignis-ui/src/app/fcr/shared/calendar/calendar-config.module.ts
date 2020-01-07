import { ButtonModule } from "@/fcr/shared/button/button.module";
import { CalendarDialogComponent } from "@/fcr/shared/calendar/calendar-dialog.component";
import { CalendarFormComponent } from "@/fcr/shared/calendar/calendar-form.component";
import { WorkingDaysFormComponent } from "@/fcr/shared/calendar/working-days-form.component";
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import {
  MatCheckboxModule,
  MatDatepickerModule,
  MatDividerModule,
  MatIconModule,
  MatNativeDateModule,
  MatSelectModule
} from "@angular/material";
import { MatMomentDateModule } from "@angular/material-moment-adapter";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatInputModule } from "@angular/material/input";
import { MatSnackBarModule } from "@angular/material/snack-bar";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDialogModule,
    MatDividerModule,
    MatSelectModule,
    MatCheckboxModule,
    MatNativeDateModule,
    MatMomentDateModule,
    MatDatepickerModule,
    MatIconModule,
    ButtonModule,
    LayoutModule
  ],
  exports: [
    CalendarDialogComponent,
    CalendarFormComponent,
    WorkingDaysFormComponent
  ],
  declarations: [
    CalendarDialogComponent,
    CalendarFormComponent,
    WorkingDaysFormComponent
  ],
  entryComponents: [CalendarDialogComponent]
})
export class CalendarConfigModule {}
