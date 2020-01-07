import { NO_ERRORS_SCHEMA } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { MatDialog } from "@angular/material/dialog";
import { DialogHelpers } from "../../../test-helpers";
import { ConfirmDialogComponent } from "./confirm-dialog.component";

import { ConfirmDialogService } from "./confirm-dialog.service";

describe("ConfirmDialogService", () => {
  let service: ConfirmDialogService;
  let dialog: MatDialog;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        DialogHelpers.createDialogTestingModule({
          declarations: [ConfirmDialogComponent],
          entryComponents: [ConfirmDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      providers: [ConfirmDialogService]
    });

    service = TestBed.get(ConfirmDialogService);
    dialog = TestBed.get(MatDialog);

    spyOn(dialog, "open").and.callThrough();
  });

  describe("confirm method", () => {
    it("should open a dialog using the `ConfirmDialogComponent` component", () => {
      const serviceNoType: any = service;
      const dialogRef = serviceNoType.confirm({});

      expect(dialog.open).toHaveBeenCalledTimes(1);
      expect(
        dialogRef.componentInstance instanceof ConfirmDialogComponent
      ).toBe(true);
    });
  });
});
