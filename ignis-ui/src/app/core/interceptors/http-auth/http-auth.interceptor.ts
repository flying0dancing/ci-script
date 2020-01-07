import { LoginDialogComponent } from '@/fcr/shared/login-dialog/login-dialog.component';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { take, tap } from 'rxjs/operators';
import { HttpAuthDialogComponent } from './http-auth-dialog.component';

@Injectable()
export class HttpAuthInterceptor implements HttpInterceptor {
  private ref: MatDialogRef<HttpAuthDialogComponent | LoginDialogComponent>;

  constructor(private dialog: MatDialog, private router: Router) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(
        event => {},
        err => {
          if (
            !this.ref &&
            err instanceof HttpErrorResponse &&
            err.status === 401 &&
            this.router.url !== '/login'
          ) {
            if (req.method !== 'GET') {
              this.ref = this.dialog.open(LoginDialogComponent, {
                disableClose: true,
                data: {
                  title: 'Login to continue'
                }
              });
            } else {
              this.ref = this.dialog.open(HttpAuthDialogComponent, {
                disableClose: true
              });
            }

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
