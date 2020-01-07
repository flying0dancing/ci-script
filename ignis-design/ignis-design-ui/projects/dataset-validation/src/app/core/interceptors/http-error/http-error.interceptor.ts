import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { take, tap } from 'rxjs/operators';

import { HttpErrorDialogComponent } from './http-error-dialog.component';

export const QUIET_ERROR_HEADER = 'design-studio-quiet';

export function quietHeaders(): {} {
  const headers = {};
  headers[QUIET_ERROR_HEADER] = 'true';
  return headers;
}

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  private ref: MatDialogRef<HttpErrorDialogComponent>;

  constructor(private dialog: MatDialog) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(
        () => {},
        err => {
          if (
            !this.ref &&
            err instanceof HttpErrorResponse &&
            err.status !== 401
          ) {
            const { error } = err;
            const correlationId = err.headers.get('Correlation-Id');

            const isQuiet = err.headers.get(QUIET_ERROR_HEADER);
            if (isQuiet) {
              return;
            }

            this.ref = this.dialog.open(HttpErrorDialogComponent, {
              data: {
                errors: error,
                status: err.status,
                correlationId
              }
            });

            this.ref
              .afterClosed()
              .pipe(take(1))
              .subscribe(() => (this.ref = undefined));
          }
        }
      )
    );
  }
}
