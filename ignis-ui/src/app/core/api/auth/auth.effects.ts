import * as UsersActions from '@/core/api/users/users.actions';
import { NAMESPACE as USERS_NAMESPACE } from '@/core/api/users/users.constants';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { catchError, filter, mergeMap, switchMap, takeUntil, tap } from 'rxjs/operators';

import { AuthService } from '../auth/auth.service';
import * as AuthActions from './auth.actions';

@Injectable()
export class AuthEffects {

  @Effect() login$: Observable<Action> = this.actions$.pipe(
    ofType<AuthActions.Login>(AuthActions.LOGIN),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<AuthActions.Empty>(AuthActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.authService
        .login(action.payload.username, action.payload.password)
        .pipe(
          mergeMap(() => {
            if (action.payload.refererUrl !== null) {
              window.location.href = this.removeTrailingSlash(document.baseURI) + action.payload.refererUrl;
            }

            return [
              new AuthActions.LoginSuccess({
                reducerMapKey: action.payload.reducerMapKey
              }),
              new UsersActions.GetCurrentUser({ reducerMapKey: USERS_NAMESPACE })
            ];
          }),
          catchError(error =>
            of(
              new AuthActions.LoginFail({
                reducerMapKey: action.payload.reducerMapKey,
                error
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  @Effect() logout$: Observable<Action> = this.actions$.pipe(
    ofType<AuthActions.Logout>(AuthActions.LOGOUT),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<AuthActions.Empty>(AuthActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.authService.logout().pipe(
        mergeMap(() => [
          new AuthActions.LogoutSuccess({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new UsersActions.Empty({ reducerMapKey: USERS_NAMESPACE })
        ]),
        tap(() => this.router.navigate(['/login'])),
        catchError(() =>
          of(
            new AuthActions.LogoutFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  constructor(
    private authService: AuthService,
    private actions$: Actions,
    public router: Router
  ) {}

  private removeTrailingSlash(url: string): string {
    if (url.endsWith('/')) {
      return url.slice(0, url.length - 1);
    }
    return url;
  }
}
