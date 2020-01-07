import { Injectable } from "@angular/core";
import {
  MatDialog,
  MatDialogConfig,
  MatDialogRef
} from "@angular/material/dialog";
import { WIDTHS } from "../dialogs.constants";
import { ConfirmDialogData } from "./confirm-dialog-data.interface";

import { ConfirmDialogComponent } from "./confirm-dialog.component";

@Injectable()
export class ConfirmDialogService {
  private config: MatDialogConfig = {
    width: WIDTHS.SMALL,
    disableClose: true
  };

  constructor(private dialog: MatDialog) {}

  confirm(data: ConfirmDialogData): MatDialogRef<ConfirmDialogComponent> {
    return this.dialog.open(ConfirmDialogComponent, { ...this.config, data });
  }
}
