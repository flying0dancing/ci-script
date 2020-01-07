import { ComponentHelpers, DialogHelpers } from "@/test-helpers";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MatDialog } from "@angular/material/dialog";
import { LayoutModule } from "../layout/layout.module";

import { ConfirmDialogComponent } from "./confirm-dialog.component";

describe("ConfirmDialogComponent", () => {
  let fixture: ComponentFixture<ComponentHelpers.ShellComponent>;
  let dialog: MatDialog;
  let overlayContainerElement: HTMLElement;

  beforeEach(() => {
    overlayContainerElement = document.createElement("div");

    TestBed.configureTestingModule({
      imports: [
        DialogHelpers.createDialogTestingModule({
          imports: [LayoutModule],
          declarations: [ConfirmDialogComponent],
          entryComponents: [ConfirmDialogComponent],
          overlayContainerElement
        })
      ],
      declarations: [ComponentHelpers.ShellComponent]
    });

    dialog = TestBed.get(MatDialog);

    fixture = TestBed.createComponent(ComponentHelpers.ShellComponent);
    fixture.detectChanges();
  });

  it("should set the title", () => {
    const dialogRef = dialog.open(ConfirmDialogComponent, {
      data: { title: "Some title" }
    });

    fixture.detectChanges();

    const h1 = overlayContainerElement.querySelector("app-dialog-header");

    expect(h1.textContent).toContain("Some title");
  });

  it("should set the content", () => {
    const dialogRef = dialog.open(ConfirmDialogComponent, {
      data: { content: "Some content" }
    });

    fixture.detectChanges();

    const content = overlayContainerElement.querySelector(
      ".mat-dialog-content"
    );

    expect(content.textContent).toContain("Some content");
  });

  it("should set the confirm and cancel button text", () => {
    const dialogRef = dialog.open(ConfirmDialogComponent, {
      data: { confirmButtonText: "Confirm", cancelButtonText: "Cancel" }
    });

    fixture.detectChanges();

    const actions = overlayContainerElement.querySelector("mat-dialog-actions");
    const buttons = actions.querySelectorAll("button");

    expect(buttons[0].textContent).toContain("Confirm");
    expect(buttons[1].textContent).toContain("Cancel");
  });

  it("should trigger the `confirm` method when the confirm button is clicked", () => {
    const dialogRef = dialog.open(ConfirmDialogComponent, { data: {} });

    spyOn(dialogRef.componentInstance, "confirm").and.callThrough();
    spyOn(dialogRef, "close");

    fixture.detectChanges();

    const actions = overlayContainerElement.querySelector("mat-dialog-actions");
    const buttons = actions.querySelectorAll("button");
    const confirm = buttons[0];

    confirm.click();

    fixture.detectChanges();

    expect(dialogRef.componentInstance.confirm).toHaveBeenCalledTimes(1);
    expect(dialogRef.close).toHaveBeenCalledTimes(1);
  });

  it("should trigger the `cancel` method and close the dialog when the cancel button is clicked", () => {
    const dialogRef = dialog.open(ConfirmDialogComponent, { data: {} });

    spyOn(dialogRef.componentInstance, "cancel").and.callThrough();
    spyOn(dialogRef, "close");

    fixture.detectChanges();

    const actions = overlayContainerElement.querySelector("mat-dialog-actions");
    const buttons = actions.querySelectorAll("button");
    const cancel = buttons[1];

    cancel.click();

    fixture.detectChanges();

    expect(dialogRef.componentInstance.cancel).toHaveBeenCalledTimes(1);
    expect(dialogRef.close).toHaveBeenCalledTimes(1);
  });
});
