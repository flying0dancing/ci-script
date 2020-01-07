import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA } from "@angular/material/dialog";
import { ErrorResponse } from "./http-error-dialog.interfaces";

@Component({
  selector: "app-http-error-dialog",
  templateUrl: "./http-error-dialog.component.html",
  styleUrls: ["./http-error-dialog.component.scss"]
})
export class HttpErrorDialogComponent implements OnInit {
  errors: ErrorResponse[];
  correlationId: string;

  constructor(
    @Inject("Window") private window: Window,
    @Inject(MAT_DIALOG_DATA) private data: any
  ) {
    this.errors = [
      {
        errorCode: "",
        errorMessage: "We're having trouble with your request right now."
      }
    ];
  }

  ngOnInit() {
    let dataErrors = this.data.errors;
    this.correlationId = this.data.correlationId;
    if (typeof dataErrors === 'string') {
      dataErrors = JSON.parse(dataErrors);
    }
    if (
      Array.isArray(dataErrors) &&
      (dataErrors as ErrorResponse[]).length > 0
    ) {
      this.errors = dataErrors;
    }
  }

  reload(): void {
    this.window.location.reload();
  }
}
