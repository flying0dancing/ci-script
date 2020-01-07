import { HttpErrorResponse } from '@angular/common/http';
import { Action } from '@ngrx/store';
import { isApiErrors } from '../array.utilities';

export interface UnhandledError {
  timestamp?: Date;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}

export interface ApiError {
  errorCode: string;
  errorMessage: string;
}

export class ErrorResponse {
  readonly errors: ApiError[];

  constructor(private _errors: ApiError[]) {
    this.errors = [..._errors];
  }

  static empty(): ErrorResponse {
    return new ErrorResponse([]);
  }

  hasErrors(): boolean {
    return this.errors && this.errors.length > 0;
  }

  hasNoErrors(): boolean {
    return !this.hasErrors();
  }
}

export abstract class FailedAction implements Action {
  abstract readonly type: string;

  readonly errorResponse: ErrorResponse = ErrorResponse.empty();

  constructor(private httpErrorResponse: HttpErrorResponse) {
    const errors = httpErrorResponse.error;

    if (isApiErrors(errors)) {
      this.errorResponse = new ErrorResponse(errors);
    }
  }
}
