import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";

import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { LayoutModule } from "../layout/layout.module";
import { ConfirmDialogComponent } from "./confirm-dialog.component";
import { ConfirmDialogService } from "./confirm-dialog.service";

@NgModule({
  imports: [CommonModule, MatButtonModule, MatDialogModule, LayoutModule],
  declarations: [ConfirmDialogComponent],
  entryComponents: [ConfirmDialogComponent],
  providers: [ConfirmDialogService]
})
export class ConfirmDialogModule {}
