import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { isApiErrors } from '../../utilities';
import {
  ApiError,
  UnhandledError
} from '../../utilities/interfaces/errors.interface';

@Component({
  selector: 'dv-http-error-dialog',
  templateUrl: './http-error-dialog.component.html',
  styleUrls: ['./http-error-dialog.component.scss']
})
export class HttpErrorDialogComponent implements OnInit {
  errors: ApiError[] = [];
  unhandledError: UnhandledError;
  correlationId: string;

  constructor(
    @Inject('Window') private window: Window,
    @Inject(MAT_DIALOG_DATA) private data: any
  ) {}

  ngOnInit() {
    const dataErrors = this.data.errors;
    this.correlationId = this.data.correlationId;

    if (isApiErrors(dataErrors)) {
      this.errors = dataErrors;
    }
    if (this.isUnexpectedError()) {
      this.unhandledError = dataErrors;
    }
  }

  reload(): void {
    this.window.location.reload();
  }

  private isUnexpectedError(): boolean {
    return this.data.status === 500;
  }
}
